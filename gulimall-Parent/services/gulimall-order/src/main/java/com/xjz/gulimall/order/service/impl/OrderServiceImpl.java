package com.xjz.gulimall.order.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xjz.gulimall.order.config.AlipayConfigProperties;
import com.xjz.gulimall.order.config.AlipayTemplate;
import com.xjz.gulimall.order.constants.OrderMqConstants;
import com.xjz.gulimall.order.dto.OrderSubmitDto;
import com.xjz.gulimall.order.entity.OrderItemEntity;
import com.xjz.gulimall.order.entity.PaymentInfoEntity;
import com.xjz.gulimall.order.feign.CartFeign;
import com.xjz.gulimall.order.feign.MemberFeign;
import com.xjz.gulimall.order.feign.ProductFeign;
import com.xjz.gulimall.order.feign.WareFeign;
import com.xjz.gulimall.order.interceptor.LoginUserInterceptor;
import com.xjz.gulimall.order.service.OrderItemService;
import com.xjz.gulimall.order.service.PaymentInfoService;
import com.xjz.gulimall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.function.Try;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import to.OrderStockTo;
import to.SkuStockLockedTo;
import to.SkuStockTo;
import to.SkuWeightTo;
import utils.PageUtils;
import utils.Query;

import com.xjz.gulimall.order.dao.OrderDao;
import com.xjz.gulimall.order.entity.OrderEntity;
import com.xjz.gulimall.order.service.OrderService;
import utils.R;
import vo.MemberVo;
import vo.SkuHasStockVo;


@Service("orderService")
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private CartFeign cartFeign;
    @Autowired
    private WareFeign wareFeign;
    @Autowired
    private ProductFeign productFeign;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private AlipayConfigProperties alipayConfigProperties;
    @Autowired
    private PaymentInfoService paymentInfoService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        // 1. 主线程捕获请求上下文与当前登录用户
        RequestAttributes mainThreadRequestAttributes = RequestContextHolder.getRequestAttributes();
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        //生成防重令牌
        String uuidToken = UUID.randomUUID().toString().replace("-","");
        String redisKey="order:token:"+memberVo.getUserId().toString();
        redisTemplate.opsForValue().set(redisKey,uuidToken,30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(uuidToken);
        orderConfirmVo.setIntegration(memberVo.getIntegration());

        // 任务 1：远程异步查询当前用户的收货地址列表
        CompletableFuture<Void> getAddressTask = CompletableFuture.runAsync(() -> {
            try {
                // 【关键】子线程绑定主线程上下文
                RequestContextHolder.setRequestAttributes(mainThreadRequestAttributes);

                List<MemberAddressVo> memberAddressVos = memberFeign.addressList(memberVo.getUserId());
                orderConfirmVo.setAddresses(memberAddressVos);
            } finally {
                // 【规范】清理子线程 ThreadLocal，防止线程池污染
                RequestContextHolder.resetRequestAttributes();
            }
        }, threadPoolExecutor);

        // 任务 2：远程异步查询购物车项 + 查库存 + 查重量
        // (说明：这三步是强顺序依赖的，合并在一个异步任务里顺序执行，性能更好且逻辑更清晰)
        CompletableFuture<Void> getCartItemsTask = CompletableFuture.runAsync(() -> {
            try {
                // 【关键】子线程绑定主线程上下文
                RequestContextHolder.setRequestAttributes(mainThreadRequestAttributes);

                // 2.1 查询购物车勾选项
                List<OrderItemVo> orderItemVos = cartFeign.getCartItems(memberVo.getUserId());

                if (orderItemVos != null && !orderItemVos.isEmpty()) {
                    List<Long> skuIds = orderItemVos.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());

                    // 2.2 查询商品项库存
                    SkuStockTo skuStockTo = new SkuStockTo();
                    skuStockTo.setSkuId(skuIds);
                    List<SkuHasStockVo> skuHasStockVoList = wareFeign.getSkuStockBySpuId(skuStockTo);
                    if (skuHasStockVoList != null) {
                        Map<Long, Boolean> stockMap = skuHasStockVoList.stream()
                                .collect(Collectors.toMap(
                                        SkuHasStockVo::getSkuId,
                                        vo -> vo.getHasStock() != null ? vo.getHasStock() : false
                                ));
                        orderConfirmVo.setStocks(stockMap);
                        orderItemVos.forEach(item -> item.setHasStock(stockMap.getOrDefault(item.getSkuId(), false)));
                    }

                    // 2.3 查询商品项重量
                    SkuWeightTo skuWeightTo = new SkuWeightTo();
                    skuWeightTo.setSkuIds(skuIds);
                    Map<Long, BigDecimal> skuWeight = productFeign.getSkuWeight(skuWeightTo);
                    if (skuWeight != null && !skuWeight.isEmpty()) {
                        orderItemVos.forEach(item -> item.setWeight(skuWeight.getOrDefault(item.getSkuId(), BigDecimal.ZERO)));
                    }
                    orderConfirmVo.setItems(orderItemVos);
                }
            } finally {
                // 【规范】清理子线程 ThreadLocal
                RequestContextHolder.resetRequestAttributes();
            }
        }, threadPoolExecutor);

        // 等待地址查询任务与购物车大任务并行完成
        CompletableFuture.allOf(getAddressTask, getCartItemsTask).join();

        return orderConfirmVo;
    }

    @Override
    public OrderSubmitResVo submitOrder(OrderSubmitDto orderSubmitDto) throws ExecutionException, InterruptedException {
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();
        OrderSubmitResVo resVo = new OrderSubmitResVo();

        // =========================================================================
        // 阶段一：防重令牌校验（纯 Redis 操作，无 DB 事务）
        // =========================================================================
        String orderToken = orderSubmitDto.getOrderToken();
        String redisKey = "order:token:" + memberVo.getUserId().toString();
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "    return redis.call('del', KEYS[1]) " +
                        "else " +
                        "    return 0 " +
                        "end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(redisKey), orderToken);
        if (result == null || result != 1L) {
            resVo.setCode(1);
            resVo.setMsg("订单令牌无效或已过期，请刷新页面重新下单");
            return resVo;
        }

        // =========================================================================
        // 阶段二：RPC 远程数据准备与内存验算（无 DB 事务，绝不霸占数据库连接池）
        // =========================================================================
        // 1. 远程查询购物车勾选项
        List<OrderItemVo> orderItemVos = cartFeign.getCartItems(memberVo.getUserId());
        if (orderItemVos == null || orderItemVos.isEmpty()) {
            throw new RuntimeException("购物车为空，无法下单");
        }

        // 2. 验价与金额计算
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemVo orderItemVo : orderItemVos) {
            totalAmount = totalAmount.add(orderItemVo.getTotalPrice());
        }
        BigDecimal freightAmount = new BigDecimal("10.00"); // TODO: 后续接仓储计算真实运费
        BigDecimal payAmount = totalAmount.add(freightAmount);

        if (orderSubmitDto.getPayPrice().compareTo(payAmount) != 0) {
            throw new RuntimeException("订单商品价格发生变化，请刷新页面后重新提交");
        }

        // 3. 生成订单号与主表对象装配
        String orderSn = memberVo.getUserId().toString()
                + System.currentTimeMillis()
                + (int)(Math.random() * 1000);

        OrderEntity order = new OrderEntity();
        order.setOrderSn(orderSn);
        order.setMemberId(memberVo.getUserId());
        order.setMemberUsername(memberVo.getUsername());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setFreightAmount(freightAmount);
        order.setPromotionAmount(BigDecimal.ZERO);
        order.setUseIntegration(0);
        order.setCreateTime(new Date());
        order.setAutoConfirmDay(7);
        order.setConfirmStatus(0);
        order.setDeleteStatus(0);
        order.setStatus(0); // 待付款
        order.setSourceType(0);
        order.setPayType(orderSubmitDto.getPayType());

        // 4. 远程查询收货地址
        List<MemberAddressVo> addresses = memberFeign.addressList(memberVo.getUserId());
        if (addresses != null) {
            MemberAddressVo address = addresses.stream()
                    .filter(a -> a.getId().equals(orderSubmitDto.getAddrId()))
                    .findFirst()
                    .orElse(null);
            if (address != null) {
                order.setReceiverName(address.getName());
                order.setReceiverPhone(address.getPhone());
                order.setReceiverPostCode(address.getPostCode());
                order.setReceiverProvince(address.getProvince());
                order.setReceiverCity(address.getCity());
                order.setReceiverRegion(address.getRegion());
                order.setReceiverDetailAddress(address.getDetailAddress());
            }
        }

        // 5. 构建订单项实体列表
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        for (OrderItemVo orderItemVo : orderItemVos) {
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setOrderSn(orderSn);
            orderItem.setSkuId(orderItemVo.getSkuId());
            orderItem.setRealAmount(orderItemVo.getTotalPrice());
            orderItem.setSkuAttrsVals(
                    orderItemVo.getSkuAttr() != null ? String.join(",", orderItemVo.getSkuAttr()) : null);
            orderItem.setGiftIntegration(0);
            orderItem.setGiftGrowth(0);
            orderItem.setSkuPic(orderItemVo.getImage());
            orderItem.setSkuName(orderItemVo.getTitle());
            orderItem.setSkuQuantity(orderItemVo.getCount());
            orderItemEntities.add(orderItem);
        }

        // 6. 构造库存锁定传输对象
        OrderStockTo orderStockTo = new OrderStockTo();
        orderStockTo.setOrderSn(orderSn);
        orderStockTo.setLocks(orderItemVos.stream().map(item -> {
            SkuStockLockedTo locked = new SkuStockLockedTo();
            locked.setSkuId(item.getSkuId());
            locked.setSkuNum(item.getCount());
            return locked;
        }).collect(Collectors.toList()));

        // =========================================================================
        // 阶段三：核心落库与锁库存（小事务控制，毫秒级释放 DB 连接）
        // =========================================================================
        transactionTemplate.executeWithoutResult(status -> {
            // 1. 本地 DB 保存订单主表与订单项表
            this.save(order);
            for (OrderItemEntity orderItem : orderItemEntities) {
                orderItem.setOrderId(order.getId());
            }
            orderItemService.saveBatch(orderItemEntities);

            // 2. 远程锁定库存
            orderStockTo.setOrderId(order.getId());
            R lockResult = wareFeign.lockStock(orderStockTo);
            if (lockResult == null || !Integer.valueOf(0).equals(lockResult.get("code"))) {
                String msg = (lockResult != null && lockResult.get("msg") != null)
                        ? lockResult.get("msg").toString() : "锁库存远程调用失败";
                // 抛出 RuntimeException 触发 TransactionTemplate 自动回滚本地 DB
                throw new RuntimeException(msg);
            }

            // 3. 注册本地事务提交成功后的回调：发送 MQ 延时关单与解锁消息
            final String finalOrderSn = orderSn;
            final OrderStockTo finalStockMsg = orderStockTo;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 发送库存解锁延时消息
                        rabbitTemplate.convertAndSend(OrderMqConstants.ORDER_EXCHANGE,
                                OrderMqConstants.STOCK_LOCK_ROUTING_KEY, finalStockMsg);
                        // 发送订单超时关闭延时消息
                        rabbitTemplate.convertAndSend(OrderMqConstants.ORDER_EXCHANGE,
                                OrderMqConstants.ORDER_CREATE_ROUTING_KEY, finalOrderSn);
                    } catch (Exception e) {
                        log.error("【重大异常】订单[{}]事务已提交但 MQ 延时消息发送失败！依赖定时任务兜底对账", finalOrderSn, e);
                    }
                }
            });
        });

        // =========================================================================
        // 阶段四：后置次要业务（事务外异步/非阻塞清理购物车，绝不影响下单主流程）
        // =========================================================================
        try {
            String skuIdsStr = orderItemVos.stream()
                    .map(item -> item.getSkuId().toString())
                    .collect(Collectors.joining(","));
            R deleteResult = cartFeign.deleteCartItems(skuIdsStr);
            if (deleteResult == null || !Integer.valueOf(0).equals(deleteResult.get("code"))) {
                log.warn("订单[{}]删除购物车失败，等待用户手动清理或后台异步补偿", orderSn);
            }
        } catch (Exception e) {
            log.warn("订单[{}]清理购物车发生网络异常，跳过，不影响下单结果: {}", orderSn, e.getMessage());
        }

        resVo.setOrder(order);
        resVo.setCode(0);
        resVo.setMsg("下单成功");
        return resVo;
    }

    @Override
    public Integer getOrderStatus(String orderSn) {
        QueryWrapper<OrderEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("order_sn",orderSn);
        OrderEntity order = this.getOne(queryWrapper);
        if(order!=null)
        {
            return order.getStatus();
        }
        else
        {
            return -1;
        }
    }

    @Override
    public void releaseOrder(String orderSn) {
        //抢分布式锁
        String lockKey="order:lock:"+orderSn;
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked=false;
        try {
            isLocked = lock.tryLock(0, TimeUnit.SECONDS);
            if(!isLocked)
            {
                //没抢到
                return;
            }
            //核心业务
            OrderEntity order = getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
            if (order == null || !order.getStatus().equals(0)) {
                return; // 订单不存在或非待付款状态，跳过
            }
            //关单前主动查询支付宝状态
            String alipayTradeStatus = checkAlipayPayStatus(orderSn);
            if ("TRADE_SUCCESS".equals(alipayTradeStatus) || "TRADE_FINISHED".equals(alipayTradeStatus)) {
                log.info("关单反查：订单[{}]在支付宝端已支付成功！更新状态为已付款，取消关单", orderSn);
                this.update(new UpdateWrapper<OrderEntity>()
                        .set("status", 1)
                        .set("payment_time", new Date())
                        .eq("order_sn", orderSn)
                        .eq("status", 0));
                return;
            }
            // 2. 否则，主动关闭支付宝预创建订单，防止关单后，用户还继续进行付款。
            closeAlipayTrade(orderSn);
            //关单
            OrderEntity updateOrder = new OrderEntity();
            updateOrder.setStatus(4);
            boolean update = update(updateOrder, new QueryWrapper<OrderEntity>().eq("order_sn", orderSn).eq("status", 0));
            if (update) {
                log.info("关单处理：订单[{}]超时未支付，已成功关闭", orderSn);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void closeAlipayTrade(String orderSn) throws AlipayApiException {
        try {
            AlipayConfig alipayConfig=new AlipayConfig();
            alipayConfig.setAppId(alipayConfigProperties.getAppId());
            alipayConfig.setPrivateKey(alipayConfigProperties.getMerchantprivatekey());
            alipayConfig.setAlipayPublicKey(alipayConfigProperties.getAlipayPublicKey());
            alipayConfig.setServerUrl(alipayConfigProperties.getGatewayUrl());
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(orderSn);
            request.setBizModel(model);
            AlipayTradeCloseResponse response = alipayClient.execute(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 主动反查支付宝端订单支付状态 (alipay.trade.query)
     * @param orderSn
     * @return
     */
    private String checkAlipayPayStatus(String orderSn) {
        try {
            AlipayConfig alipayConfig=new AlipayConfig();
            alipayConfig.setAppId(alipayConfigProperties.getAppId());
            alipayConfig.setPrivateKey(alipayConfigProperties.getMerchantprivatekey());
            alipayConfig.setAlipayPublicKey(alipayConfigProperties.getAlipayPublicKey());
            alipayConfig.setServerUrl(alipayConfigProperties.getGatewayUrl());
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
            AlipayTradeQueryRequest request=new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(orderSn);
            request.setBizModel(model);
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if(response.isSuccess())
            {
                return response.getTradeStatus();
            }
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        return "UNKNOWN";
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo=new PayVo();
        OrderEntity order = getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn).eq("status", 0));
        if (order == null) {
            throw new RuntimeException("订单不存在或已关闭，无法支付");
        }
        BigDecimal payAmount = order.getPayAmount().setScale(2, RoundingMode.HALF_UP);
        payVo.setTotalAmount(payAmount.toString());
        payVo.setOutTradeNo(order.getOrderSn());
        // 4. 查询订单项名称，设置标题与备注
        List<OrderItemEntity> orderItems = orderItemService.list(
                new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn)
        );
        //比如说有两个商品
        if (orderItems != null && orderItems.size() > 0) {
            OrderItemEntity item = orderItems.get(0);
            // 设置订单标题，例如："谷粒商城-华为Mate60"
            payVo.setSubject("谷粒商城-" + item.getSkuName());
            payVo.setBody("谷粒商城订单商品明细");
        } else {
            payVo.setSubject("谷粒商城订单-" + order.getOrderSn());
            payVo.setBody("谷粒商城订单");
        }
        return payVo;
    }

    @Override
    public String payOrder(PayVo payVo) throws AlipayApiException {
        return alipayTemplate.Pay(payVo);
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberVo memberVo = LoginUserInterceptor.loginUser.get();
        if (memberVo == null) {
            throw new RuntimeException("用户未登录，无法查询订单列表");
        }
        //查询当前用户的订单，按照创建时间倒序排序
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
                        .eq("member_id", memberVo.getUserId())
                        .orderByDesc("create_time")
        );
        List<OrderEntity> orderEntities = page.getRecords();
        if (orderEntities == null || orderEntities.isEmpty()) {
            return new PageUtils(page);
        }
        //批量查询当前页所有订单的订单项，按订单号分组，避免循环内逐条查询（N+1 问题）
        List<String> orderSns = orderEntities.stream()
                .map(OrderEntity::getOrderSn)
                .collect(Collectors.toList());
        Map<String, List<OrderItemEntity>> itemMap = orderItemService.list(
                        new QueryWrapper<OrderItemEntity>().in("order_sn", orderSns))
                .stream()
                .collect(Collectors.groupingBy(OrderItemEntity::getOrderSn));
        //挂载订单项到对应订单
        orderEntities.forEach(order ->
                order.setItemEntities(itemMap.getOrDefault(order.getOrderSn(), Collections.emptyList()))
        );
        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo asyncVo) throws InterruptedException {
        String orderSn = asyncVo.getOutTradeNo();
        String lockKey = "order:lock:" + orderSn;
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                log.error("订单[{}]支付回调获取分布式锁超时，返回 failure 触发支付宝后续重试", orderSn);
                return "failure";
            }
            //校验appId
            if(!alipayConfigProperties.getAppId().equals(asyncVo.getAppId()))
            {
                log.error("【支付回调校验失败】AppID 不匹配，收到：{}", asyncVo.getAppId());
                return "failure";
            }
            //【防线二：校验交易状态】状态如果不是 TRADE_SUCCESS 或 TRADE_FINISHED ，就返回success，让支付宝停止异步通知
            String tradeStatus = asyncVo.getTradeStatus();
            if(!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus))
            {
                return "success";
            }
            //到这里，代表用户已经付过钱了
            //校验订单存在性
            OrderEntity order=this.getOrderByOrderSn(orderSn);
            if(order==null)
            {
                log.error("【支付回调校验失败】订单不存在，订单号：{}", orderSn);
                return "failure";
            }
            //校验订单金额
            BigDecimal payAmountFromAlipay = new BigDecimal(asyncVo.getTotalAmount());
            if (order.getPayAmount().compareTo(payAmountFromAlipay) != 0) {
                log.error("【支付回调校验失败】订单金额不一致！数据库：{}，支付宝通知：{}",
                        order.getPayAmount(), payAmountFromAlipay);
                return "failure";
            }
            // 4.【防线三：状态机幂等性检查】
            // 订单状态：0->待付款；1->已付款；2->已发货；3->已完成；4->已关闭
            if (order.getStatus().equals(1)) {
                return "success";
            }
            if (order.getStatus().equals(4)) {
                // 订单已被关单 -> 自动原路退款！
                log.warn("【关单临界点支付异常】订单[{}]已关闭但用户完成支付，触发自动退款...", orderSn);
                boolean refundSuccess = refundAlipayTrade(orderSn, asyncVo.getTradeNo(), payAmountFromAlipay, "订单超时关单，系统自动退款");
                if (refundSuccess) {
                    asyncVo.setTradeStatus("REFUNDED");
                    savePaymentInfo(asyncVo);
                    return "success"; // 退款成功，停止支付宝通知
                } else {
                    return "failure"; // 退款失败，让支付宝重试通知
                }
            }
            //更新订单状态
            Boolean updateSuccess = transactionTemplate.execute(status -> {
                boolean updated = this.update(
                        new UpdateWrapper<OrderEntity>()
                                .set("status", 1)
                                .set("payment_time", asyncVo.getGmtPayment())
                                .eq("order_sn", orderSn)
                                .eq("status", 0)
                );
                if (updated) {
                    savePaymentInfo(asyncVo);
                }
                return updated;
            });
            if (Boolean.TRUE.equals(updateSuccess)) {
                log.info("【支付成功】订单 {} 状态更新为 [已付款]！流水号：{}", orderSn, asyncVo.getTradeNo());
            } else {
                log.error("【支付异常】订单[{}]状态更新失败，当前状态：{}，支付宝流水号：{}",
                        orderSn, order.getStatus(), asyncVo.getTradeNo());
                return "failure";
            }

            return "success";

        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean refundAlipayTrade(String orderSn, String tradeNo, BigDecimal payAmountFromAlipay, String s) {
        try {
            AlipayConfig alipayConfig=new AlipayConfig();
            alipayConfig.setAppId(alipayConfigProperties.getAppId());
            alipayConfig.setPrivateKey(alipayConfigProperties.getMerchantprivatekey());
            alipayConfig.setAlipayPublicKey(alipayConfigProperties.getAlipayPublicKey());
            alipayConfig.setServerUrl(alipayConfigProperties.getGatewayUrl());
            AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model=new AlipayTradeRefundModel();
            model.setRefundAmount(String.valueOf(payAmountFromAlipay));
            model.setOutTradeNo(orderSn);
            request.setBizModel(model);
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            return response.isSuccess();
        } catch (Exception e) {
            log.error("订单[{}]原路退款调用异常", orderSn, e);
            return false;
        }
    }

    private void savePaymentInfo(PayAsyncVo asyncVo) {
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setOrderSn(asyncVo.getOutTradeNo());
        paymentInfo.setAlipayTradeNo(asyncVo.getTradeNo());
        paymentInfo.setTotalAmount(new BigDecimal(asyncVo.getTotalAmount()));
        paymentInfo.setSubject(asyncVo.getSubject());
        paymentInfo.setPaymentStatus(asyncVo.getTradeStatus());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setCallbackTime(new Date());

        paymentInfoService.save(paymentInfo);
    }

    private OrderEntity getOrderByOrderSn(String orderSn) {
        QueryWrapper<OrderEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("order_sn",orderSn);
        return this.getOne(queryWrapper);
    }
}
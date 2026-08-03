package com.xjz.gulimall.order.service.impl;

import com.xjz.gulimall.order.feign.CartFeign;
import com.xjz.gulimall.order.feign.MemberFeign;
import com.xjz.gulimall.order.feign.ProductFeign;
import com.xjz.gulimall.order.feign.WareFeign;
import com.xjz.gulimall.order.interceptor.LoginUserInterceptor;
import com.xjz.gulimall.order.vo.MemberAddressVo;
import com.xjz.gulimall.order.vo.OrderConfirmVo;
import com.xjz.gulimall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import to.SkuStockTo;
import to.SkuWeightTo;
import utils.PageUtils;
import utils.Query;

import com.xjz.gulimall.order.dao.OrderDao;
import com.xjz.gulimall.order.entity.OrderEntity;
import com.xjz.gulimall.order.service.OrderService;
import vo.MemberVo;
import vo.SkuHasStockVo;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
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
}
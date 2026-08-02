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
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        ThreadLocal<MemberVo> threadLocal = LoginUserInterceptor.loginUser;
        MemberVo memberVo = threadLocal.get();
        orderConfirmVo.setIntegration(memberVo.getIntegration());
        //远程异步查询当前用户的收货地址列表
        CompletableFuture<Void> getAddressTask = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                List<MemberAddressVo> memberAddressVos = memberFeign.addressList(memberVo.getUserId());
                orderConfirmVo.setAddresses(memberAddressVos);
            }
        }, threadPoolExecutor);

        //远程异步查询购物车中勾选的商品项
        CompletableFuture<Void> getCartItemsTask = CompletableFuture.supplyAsync(new Supplier<List<OrderItemVo>>() {
            @Override
            public List<OrderItemVo> get() {
                return cartFeign.getCartItems(memberVo.getUserId());
            }
        }, threadPoolExecutor).thenApplyAsync(orderItemVos -> {
            if (orderItemVos == null || orderItemVos.isEmpty()) {
                return orderItemVos;
            }
            orderConfirmVo.setItems(orderItemVos);
            //远程查询商品项的hasStock
            List<Long> skuIds = orderItemVos.stream().map(orderItemVo -> orderItemVo.getSkuId()).collect(Collectors.toList());
            SkuStockTo skuStockTo = new SkuStockTo();
            skuStockTo.setSkuId(skuIds);
            List<SkuHasStockVo> skuHasStockVoList = wareFeign.getSkuStockBySpuId(skuStockTo);
            if (skuHasStockVoList != null) {
                Map<Long, Boolean> stockMap = skuHasStockVoList.stream()
                        .collect(Collectors.toMap(SkuHasStockVo::getSkuId,
                                vo -> vo.getHasStock() != null ? vo.getHasStock() : false));
                orderConfirmVo.setStocks(stockMap);
                orderItemVos.forEach(item -> item.setHasStock(stockMap.getOrDefault(item.getSkuId(), false)));
            }
            return orderItemVos;
        }, threadPoolExecutor).thenAcceptAsync(new Consumer<List<OrderItemVo>>() {
            @Override
            public void accept(List<OrderItemVo> orderItemVos) {
                //远程查询商品项的weight
                SkuWeightTo skuWeightTo = new SkuWeightTo();
                List<Long> skuIds = orderItemVos.stream().map(orderItemVo -> orderItemVo.getSkuId()).collect(Collectors.toList());
                skuWeightTo.setSkuIds(skuIds);
                Map<Long, BigDecimal> skuWeight = productFeign.getSkuWeight(skuWeightTo);
                if (skuWeight != null && !skuWeight.isEmpty()) {
                    orderItemVos.forEach(item -> item.setWeight(skuWeight.getOrDefault(item.getSkuId(), BigDecimal.ZERO)));
                }
                orderConfirmVo.setItems(orderItemVos);
            }
        }, threadPoolExecutor);
        CompletableFuture.allOf(getAddressTask, getCartItemsTask).join();
        return orderConfirmVo;
    }
}
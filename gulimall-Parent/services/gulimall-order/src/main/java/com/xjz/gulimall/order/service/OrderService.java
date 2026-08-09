package com.xjz.gulimall.order.service;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.order.dto.OrderSubmitDto;
import com.xjz.gulimall.order.vo.OrderConfirmVo;
import com.xjz.gulimall.order.vo.OrderSubmitResVo;
import com.xjz.gulimall.order.vo.PayAsyncVo;
import com.xjz.gulimall.order.vo.PayVo;
import to.SeckillOrderTo;
import utils.PageUtils;
import com.xjz.gulimall.order.entity.OrderEntity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:09:40
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    OrderSubmitResVo submitOrder(OrderSubmitDto orderSubmitDto) throws ExecutionException, InterruptedException;

    Integer getOrderStatus(String orderSn);

    void releaseOrder(String orderSn);

    PayVo getOrderPay(String orderSn);

    String payOrder(PayVo payVo) throws AlipayApiException;

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo asyncVo) throws InterruptedException;
    void createSeckillOrder(SeckillOrderTo seckillOrderTo);
}


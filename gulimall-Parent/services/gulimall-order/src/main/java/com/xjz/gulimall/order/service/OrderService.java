package com.xjz.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.order.dto.OrderSubmitDto;
import com.xjz.gulimall.order.vo.OrderConfirmVo;
import com.xjz.gulimall.order.vo.OrderSubmitResVo;
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
}


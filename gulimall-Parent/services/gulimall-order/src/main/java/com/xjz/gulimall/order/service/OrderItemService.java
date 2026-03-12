package com.xjz.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.order.entity.OrderItemEntity;
import utils.PageUtils;

import java.util.Map;

/**
 * 订单项信息
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:09:40
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


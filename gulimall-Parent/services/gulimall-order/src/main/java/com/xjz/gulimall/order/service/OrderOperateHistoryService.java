package com.xjz.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import utils.PageUtils;
import com.xjz.gulimall.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:09:40
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


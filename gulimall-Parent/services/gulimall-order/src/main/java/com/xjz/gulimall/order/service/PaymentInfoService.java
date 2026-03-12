package com.xjz.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import utils.PageUtils;
import com.xjz.gulimall.order.entity.PaymentInfoEntity;

import java.util.Map;

/**
 * 支付信息表
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:09:40
 */
public interface PaymentInfoService extends IService<PaymentInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


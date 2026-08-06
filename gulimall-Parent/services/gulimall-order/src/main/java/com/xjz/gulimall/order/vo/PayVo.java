package com.xjz.gulimall.order.vo;

import lombok.Data;

/**
 * 传递给支付宝页面的支付参数模型
 */
@Data
public class PayVo {
    /**
     * 商户订单号（系统内部唯一订单号，如：2026080612345678）
     */
    private String outTradeNo;

    /**
     * 订单标题/主商品名称（如："谷粒商城-订单2026080612345678"）
     */
    private String subject;

    /**
     * 订单总金额（字符串格式，必须保留两位小数，如 "88.00"）
     */
    private String totalAmount;

    /**
     * 订单备注/商品描述（可空，如："包含1件手机，1件耳机"）
     */
    private String body;
}

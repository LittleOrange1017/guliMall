package com.xjz.gulimall.order.vo;

import lombok.Data;

@Data
public class PayAsyncVo {
    private String gmtCreate;        // 交易创建时间
    private String charset;           // 编码格式
    private String gmtPayment;       // 交易付款时间
    private String notifyTime;       // 通知时间
    private String subject;           // 订单标题
    private String sign;              // 签名字符串（用于验签）
    private String buyerId;          // 买家支付宝用户号
    private String body;              // 商品描述
    private String invoiceAmount;    // 开票金额
    private String version;           // 接口版本
    private String notifyId;         // 通知校验ID
    private String fundBillList;    // 支付渠道列表
    private String notifyType;       // 通知类型
    private String outTradeNo;      // 商户订单号（商城系统的订单号）
    private String totalAmount;      // 交易金额
    private String tradeStatus;      // 交易状态：TRADE_SUCCESS / TRADE_FINISHED
    private String tradeNo;          // 支付宝交易流水号（支付宝系统内唯一）
    private String authAppId;
    private String receiptAmount;    // 实收金额
    private String pointAmount;      // 集分宝金额
    private String appId;            // 开发者的AppId
    private String buyerPayAmount;  // 买家付款金额
    private String signType;         // 签名类型（RSA2）
    private String sellerId;         // 卖家支付宝用户号
}

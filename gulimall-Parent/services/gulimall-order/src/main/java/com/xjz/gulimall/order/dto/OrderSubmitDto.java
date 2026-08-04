package com.xjz.gulimall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitDto {
    //收货地址id
    private Long addrId;
    //支付方式
    private Integer payType;
    //防重令牌
    private String orderToken;
    //实付价格
    private BigDecimal payPrice;
}

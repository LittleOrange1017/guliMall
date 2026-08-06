package com.xjz.gulimall.order.vo;

import com.xjz.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class OrderListVo {
    private String orderSn;
    private String memberId;
    private BigDecimal payPrice;
    private Integer status;
    private List<OrderItemEntity> orderItemVoList;
}

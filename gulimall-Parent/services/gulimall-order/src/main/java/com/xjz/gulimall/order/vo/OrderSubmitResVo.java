package com.xjz.gulimall.order.vo;

import com.xjz.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class OrderSubmitResVo {
    private OrderEntity order;
    private Integer code;
    private String msg;

}

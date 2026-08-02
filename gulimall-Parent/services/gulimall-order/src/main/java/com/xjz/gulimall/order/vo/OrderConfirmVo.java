package com.xjz.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderConfirmVo {
    //所有的收货地址
    List<MemberAddressVo> addresses;
    //购物车中所有购物项
    List<OrderItemVo> items;
    //积分信息
    private Integer integration;
    //各商品的库存状态
    private Map<Long,Boolean> stocks;
    //防重令牌
    private String orderToken;
    /**
     * 商品总件数
     */
    private Integer count;
    /**
     * 商品总金额
     */
    private BigDecimal total;
    /**
     * 应付总额（商品总额 + 运费 - 优惠/积分抵扣）
     */
    private BigDecimal payPrice;
    public BigDecimal  getTotal(){
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                sum = sum.add(item.getTotalPrice());
            }
        }
        return sum;
    }
    public Integer getCount(){
        Integer count = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }


}

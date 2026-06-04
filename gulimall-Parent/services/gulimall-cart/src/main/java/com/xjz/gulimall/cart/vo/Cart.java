package com.xjz.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class Cart {
    /**
     * 购物车所有商品项目集合
     */
    private List<CartItem> items;
    /**
     * 商品的总数量，也就是第一个商品假如是3件，第二个商品是2件，总数就是5件
     */
    private Integer countNum;
    /**
     * 商品类型数量，也就是购物车内选购的商品种类数
     */
    private Integer countType;
    /**
     * 全部的商品总价，计算方式就是拿每种商品的数量乘以那种商品的单价，循环的限制就是商品类型数量
     */
    private BigDecimal totalAmout;
    /**
     * 减免价格
     */
    private BigDecimal reduce = new BigDecimal("0");

    public Integer getCountNum() {
        int count = 0;
        if (items != null && this.items.size() > 0) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                countNum += 1;
            }
        }
        return count;
    }
    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if(items!=null&&items.size()>0)
        {
            for(CartItem item : items)
            {
                BigDecimal price = item.getTotalPrice();
                amount=amount.add(price);
            }
        }
        BigDecimal subtract=amount.subtract(reduce);
        return subtract;
    }
}

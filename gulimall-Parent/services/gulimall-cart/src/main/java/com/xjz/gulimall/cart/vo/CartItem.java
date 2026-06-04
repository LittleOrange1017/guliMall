package com.xjz.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class CartItem {
    /**
     * 商品Id
     */
    private Long skuId;
    /**
     * 商品标题
     */
    private String title;
    /**
     * 商品是否被选中（默认为被选中）
     */
    private Boolean check=true;
    /**
     * 商品图片
     */
    private String image;
    /**
     * 商品销售属性组合
     */
    private List<String> skuAttr;
    /**
     * 商品单价
     */
    private BigDecimal price;
    /**
     * 商品数量
     */
    private Integer count;
    /**
     * 总价格，商品单价*商品数量
     */
    private BigDecimal totalPrice;
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal("" + this.count));
    }
}

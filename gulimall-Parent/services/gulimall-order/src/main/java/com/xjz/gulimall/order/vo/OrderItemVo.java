package com.xjz.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    /**
     * 商品Id
     */
    private Long skuId;
    /**
     * 商品标题
     */
    private String title;
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
    /**
     * 是否有货
     */
    private Boolean hasStock;
    /**
     * 重量（用于计算运费）
     */
    private BigDecimal weight;

    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal("" + this.count));
    }
}

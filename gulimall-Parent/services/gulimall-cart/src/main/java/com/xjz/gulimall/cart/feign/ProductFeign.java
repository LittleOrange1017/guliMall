package com.xjz.gulimall.cart.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import utils.R;

import java.util.List;

@FeignClient("service-product")
public interface ProductFeign {
    /**
     * 根据skuId获取skuInfo
     */
    @GetMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
    /**
     * 根据 skuId 查询销售属性组合列表
     */
    @GetMapping("/product/skusaleattrvalue/skuAttr/{skuId}")
    public R getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);
}

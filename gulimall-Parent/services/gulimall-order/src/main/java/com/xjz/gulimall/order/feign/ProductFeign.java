package com.xjz.gulimall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import to.SkuInfoTo;
import to.SkuWeightTo;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient("service-product")
public interface ProductFeign {
    @PostMapping("/product/skuinfo/skuWeight")
    public Map<Long, BigDecimal> getSkuWeight(@RequestBody SkuWeightTo skuWeightTo);
    @PostMapping("/product/skuinfo/feign/getSkuInfo/{skuId}")
    public SkuInfoTo getFeignSkuInfo(@PathVariable("skuId") Long skuId);
}

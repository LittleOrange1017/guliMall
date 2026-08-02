package com.xjz.gulimall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import to.SkuWeightTo;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient("service-product")
public interface ProductFeign {
    @PostMapping("/product/skuinfo/skuWeight")
    public Map<Long, BigDecimal> getSkuWeight(@RequestBody SkuWeightTo skuWeightTo);
}

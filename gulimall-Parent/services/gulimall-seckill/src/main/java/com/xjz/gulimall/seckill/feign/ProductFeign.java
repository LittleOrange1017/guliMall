package com.xjz.gulimall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import to.SkuInfoTo;
import utils.R;
@FeignClient("service-product")
public interface ProductFeign {
    @PostMapping("/product/skuinfo/feign/getSkuInfo/{skuId}")
    public SkuInfoTo getFeignSkuInfo(@PathVariable("skuId") Long skuId);
}

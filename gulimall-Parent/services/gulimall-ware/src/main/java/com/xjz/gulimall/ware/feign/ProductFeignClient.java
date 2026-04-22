package com.xjz.gulimall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import utils.R;

import java.math.BigDecimal;

/**
 * ClassName: ProductFeignClient
 * Package:com.xjz.gulimall.ware.feign
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/20 16:01
 * @Version 1.0
 */
@FeignClient("service-product")
public interface ProductFeignClient {
    /**
     * 调用service-product的接口，获取sku信息
     */
    @PostMapping("/product/skuinfo/getSkuInfo")
    BigDecimal getSkuInfo(@RequestParam("skuId") Long skuId);
}

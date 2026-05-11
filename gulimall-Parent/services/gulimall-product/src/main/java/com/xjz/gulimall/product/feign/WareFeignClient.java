package com.xjz.gulimall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import to.SkuStockTo;
import vo.SkuHasStockVo;

import java.util.List;

@FeignClient("service-ware")
public interface WareFeignClient {
    @PostMapping("/ware/waresku/skuStock")
    List<SkuHasStockVo> getSkuStockBySpuId(@RequestBody SkuStockTo skuStockTo);
}

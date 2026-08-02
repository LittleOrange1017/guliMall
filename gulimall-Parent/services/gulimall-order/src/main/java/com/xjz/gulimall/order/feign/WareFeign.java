package com.xjz.gulimall.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import to.SkuStockTo;
import vo.SkuHasStockVo;

import java.util.List;
import java.util.Map;

@FeignClient("service-ware")
public interface WareFeign {
    @PostMapping("/ware/waresku/skuStock")
    public List<SkuHasStockVo> getSkuStockBySpuId(@RequestBody SkuStockTo skuStockTo);
}

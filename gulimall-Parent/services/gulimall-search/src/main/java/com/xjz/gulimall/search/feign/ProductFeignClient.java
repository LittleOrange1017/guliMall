package com.xjz.gulimall.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("service-product")
public interface ProductFeignClient {
    @PostMapping("/product/attr/getAttrName")
    Map<Long, String> getAttrNamesByIds(@RequestBody List<Long> attrIds);
}

package com.xjz.gulimall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import to.SkuEsModel;
import utils.R;

import java.util.List;

@FeignClient("service-search")
public interface SearchFeignClient {
    @PostMapping("/search/save")
    R saveUp(@RequestBody List<SkuEsModel> skuEsModels);
}

package com.xjz.gulimall.productB.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import utils.R;

@FeignClient("service-sso")
public interface SSOFeignClient {
    @PostMapping("/verify")
    R verify(@RequestParam String token);
}

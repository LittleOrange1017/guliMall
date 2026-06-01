package com.xjz.gulimall.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import utils.R;

@FeignClient("service-thirdparty")
public interface ThirdPartyFeignClient {
    @GetMapping("/sms/sendcode")
    R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}

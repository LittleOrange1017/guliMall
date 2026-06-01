package com.xjz.gulimall.auth.feign;

import com.xjz.gulimall.auth.dto.RegFeignDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import utils.R;

@FeignClient("service-member")

public interface MemberFeignClient {
    @PostMapping("member/member/regist")
    public R regist(RegFeignDto regFeignDto);
}

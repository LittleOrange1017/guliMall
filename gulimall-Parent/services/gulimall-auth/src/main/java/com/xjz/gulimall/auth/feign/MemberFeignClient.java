package com.xjz.gulimall.auth.feign;

import com.xjz.gulimall.auth.dto.GiteeUserDto;
import com.xjz.gulimall.auth.dto.LoginDto;
import com.xjz.gulimall.auth.dto.RegFeignDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import utils.R;

@FeignClient("service-member")

public interface MemberFeignClient {
    @PostMapping("member/member/regist")
    public R regist(RegFeignDto regFeignDto);
    @PostMapping("member/member/login")
    public R login(LoginDto dto);
    @PostMapping("member/member/loginOrRegist")
    public R loginOrRegist(GiteeUserDto giteeUserDto);
}

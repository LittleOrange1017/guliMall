package com.xjz.gulimall.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import utils.R;

/**
 * ClassName: CouponFeignClient
 * Package:com.xjz.gulimall.member.feign
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/3/13 8:54
 * @Version 1.0
 */
@FeignClient("service-coupon")
public interface CouponFeignClient{
    @GetMapping("coupon/coupon/member/list")
    public R memberCoupons();
}

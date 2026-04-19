package com.xjz.gulimall.product.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import to.SpuBoundTo;
import utils.R;

/**
 * ClassName: CouponFeginClient
 * Package:com.xjz.gulimall.product.feign
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/19 11:21
 * @Version 1.0
 */
@FeignClient("service-coupon")
public interface CouponFeginClient {
    /**
     * 调用service-coupon的接口保存积分信息
     */
    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);
}

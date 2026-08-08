package com.xjz.gulimall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import to.Laste3DaysSessionTo;
import to.SeckillSkuRelationTo;
import utils.R;

import java.util.List;

@FeignClient("service-coupon")
public interface CouponFeign {
    @GetMapping("/coupon/seckillsession//getLaste3DaysSession")
    public List<Laste3DaysSessionTo> getLaste3DaysSession();
}

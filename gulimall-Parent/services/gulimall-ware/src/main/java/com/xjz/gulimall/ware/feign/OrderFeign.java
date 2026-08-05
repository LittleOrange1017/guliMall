package com.xjz.gulimall.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import utils.R;

@FeignClient("service-order")
public interface OrderFeign {
    @GetMapping("/order/order/orderSn/{orderSn}")
    public R getOrderStatus(@PathVariable("orderSn") String orderSn);
}

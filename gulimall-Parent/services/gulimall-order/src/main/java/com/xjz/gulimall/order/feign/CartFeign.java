package com.xjz.gulimall.order.feign;

import com.xjz.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("service-cart")
public interface CartFeign {
    @GetMapping("/getUserCartItems/{id}")
    public List<OrderItemVo> getCartItems(@PathVariable("id") Long id);
}

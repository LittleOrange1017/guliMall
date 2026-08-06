package com.xjz.gulimall.order.feign;

import com.xjz.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import utils.R;

import java.util.List;

@FeignClient("service-cart")
public interface CartFeign {
    @GetMapping("/getUserCartItems/{id}")
    public List<OrderItemVo> getCartItems(@PathVariable("id") Long id);
    @PostMapping("/deleteCartItems")
    public R deleteCartItems(@RequestParam("skuIds") String skuIds);

}

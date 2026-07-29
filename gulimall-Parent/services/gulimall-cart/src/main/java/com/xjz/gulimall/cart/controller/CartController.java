package com.xjz.gulimall.cart.controller;

import com.xjz.gulimall.cart.interceptor.CartInterceptor;
import com.xjz.gulimall.cart.service.CartService;
import com.xjz.gulimall.cart.vo.Cart;
import com.xjz.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import utils.R;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class CartController {
    @Autowired
    private CartService cartService;
    @GetMapping("/cart.html")
    public String cartListPage(Model model)
    {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        model.addAttribute("userInfo",userInfoTo);
        Cart cart=cartService.listCart(userInfoTo);
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @ResponseBody
    @PostMapping("/checkCart")
    public R checkCart(@RequestParam("skuId") String skuId, @RequestParam("isChecked") Integer isChecked) {
        cartService.checkCartItem(skuId, isChecked);
        return R.ok();
    }

    @ResponseBody
    @PostMapping("/changeItemCount")
    public R changeItemCount(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return R.ok();
    }

    @ResponseBody
    @PostMapping("/deleteCartItem")
    public R deleteCartItem(@RequestParam("skuId") String skuId) {
        cartService.deleteCartItem(skuId);
        return R.ok();
    }

    @ResponseBody
    @PostMapping("/deleteCartItems")
    public R deleteCartItems(@RequestParam("skuIds") String skuIds) {
        List<String> skuIdList = Arrays.stream(skuIds.split(","))
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.toList());
        cartService.deleteCartItems(skuIdList);
        return R.ok();
    }
}

package com.xjz.gulimall.cart.controller;

import com.xjz.gulimall.cart.interceptor.CartInterceptor;
import com.xjz.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
@Slf4j
public class CartController {
    @GetMapping("/cart.html")
    public String cartListPage(Model model)
    {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        model.addAttribute("userInfo",userInfoTo);

        return "cartList";
    }
}

package com.xjz.gulimall.coupon.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping({"/login.html","/","/login"})
    public String loginPage() {
        return "login";
    }
}

package com.xjz.gulimall.cart.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartWeb {
    @GetMapping("/")
    public String cartPage(Model model){
        return "cartlist";
    }
}

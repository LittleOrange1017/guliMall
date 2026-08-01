package com.xjz.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderWeb {
    @GetMapping("/{page}")
    public String listPage(Model model, @PathVariable("page") String page){
        return page;
    }

}

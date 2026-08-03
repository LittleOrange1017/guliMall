package com.xjz.gulimall.order.web;

import com.alibaba.fastjson.JSON;
import com.xjz.gulimall.order.service.OrderService;
import com.xjz.gulimall.order.vo.OrderConfirmVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vo.MemberVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@Slf4j
public class OrderWeb {
    @Autowired
    private OrderService orderService;
    @GetMapping("/{page:[^\\.]+}")
    public String listPage(HttpServletRequest request,Model model, @PathVariable("page") String page){
        return page;
    }
    @GetMapping("/toTrade")
    public String toTrade(Model model){
        OrderConfirmVo confirmVo=orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

}

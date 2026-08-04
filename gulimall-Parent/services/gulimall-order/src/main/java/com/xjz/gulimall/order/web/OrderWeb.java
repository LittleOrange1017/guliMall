package com.xjz.gulimall.order.web;

import com.alibaba.fastjson.JSON;
import com.xjz.gulimall.order.dto.OrderSubmitDto;
import com.xjz.gulimall.order.service.OrderService;
import com.xjz.gulimall.order.vo.OrderConfirmVo;
import com.xjz.gulimall.order.vo.OrderSubmitResVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utils.R;
import vo.MemberVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

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

    @ResponseBody
    @PostMapping("/submitOrder")
    public OrderSubmitResVo submitOrder(@RequestBody OrderSubmitDto orderSubmitDto) {
        try {
            return orderService.submitOrder(orderSubmitDto);
        } catch (Exception e) {
            log.error("订单创建失败", e);
            OrderSubmitResVo resVo = new OrderSubmitResVo();
            resVo.setCode(3);
            resVo.setMsg("订单创建失败：" + e.getMessage());
            return resVo;
        }
    }

    @GetMapping("/pay")
    public String payPage(@RequestParam("orderSn") String orderSn,
                          @RequestParam("payAmount") String payAmount,
                          Model model) {
        if (orderSn == null || payAmount == null) {
            return "redirect:/toTrade";
        }
        model.addAttribute("orderSn", orderSn);
        model.addAttribute("payAmount", new BigDecimal(payAmount));
        return "pay";
    }

}

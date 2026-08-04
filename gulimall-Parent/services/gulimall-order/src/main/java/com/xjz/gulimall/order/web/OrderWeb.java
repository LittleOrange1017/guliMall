package com.xjz.gulimall.order.web;

import com.alibaba.fastjson.JSON;
import com.xjz.gulimall.order.dto.OrderSubmitDto;
import com.xjz.gulimall.order.service.OrderService;
import com.xjz.gulimall.order.vo.OrderConfirmVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utils.R;
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

    @PostMapping("/submitOrder")
    @ResponseBody
    public R submitOrder(@RequestBody OrderSubmitDto orderSubmitDto){
        //具体的下单操作：创建订单，验证令牌，锁库存
        //下单成功的话，跳转至支付页
        //下单失败的话，回到订单确认页，提示订单创建失败
        try {
            orderService.submitOrder(orderSubmitDto);
            return R.ok();
        } catch (Exception e) {
            log.error("提交订单失败", e);
            return R.error(e.getMessage());
        }
    }

}

package com.xjz.gulimall.order.web;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.xjz.gulimall.order.dto.OrderSubmitDto;
import com.xjz.gulimall.order.service.OrderService;
import com.xjz.gulimall.order.vo.OrderConfirmVo;
import com.xjz.gulimall.order.vo.OrderSubmitResVo;
import com.xjz.gulimall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utils.PageUtils;
import utils.R;
import vo.MemberVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
@Slf4j
public class OrderWeb {
    @Autowired
    private OrderService orderService;
    @GetMapping("/{page:(?!list$)[^\\.]+}")
    public String listPage(HttpServletRequest request,Model model, @PathVariable("page") String page){
        return page;
    }
    @GetMapping("/list")
    public String orderListPage(@RequestParam(value = "page", defaultValue = "1") String page,
                                Model model){
        Map<String,Object> params=new HashMap<>();
        params.put("page",page);
        params.put("limit","5");
        PageUtils pageUtils= orderService.queryPageWithItem(params);
        model.addAttribute("orders",pageUtils);
        return "list";
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
    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html;charset=UTF-8")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
       PayVo payVo= orderService.getOrderPay(orderSn);
       //调用支付宝SDK产生HTML字符串
       String payHtmlStr = orderService.payOrder(payVo);
       return payHtmlStr;
    }

}

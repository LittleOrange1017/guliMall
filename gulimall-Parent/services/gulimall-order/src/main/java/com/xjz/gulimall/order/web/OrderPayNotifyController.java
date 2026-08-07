package com.xjz.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.xjz.gulimall.order.config.AlipayConfigProperties;
import com.xjz.gulimall.order.config.AlipayTemplate;
import com.xjz.gulimall.order.service.OrderService;
import com.xjz.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderPayNotifyController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private AlipayConfigProperties alipayConfigProperties;

    @PostMapping("/payed/notify")
    public String handleAlipayNotify(HttpServletRequest request) throws AlipayApiException, InterruptedException {
        Map<String,String> params=new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        boolean rsaCheckV1 = AlipaySignature.rsaCheckV1(params, alipayConfigProperties.getAlipayPublicKey(), alipayConfigProperties.getCharset(), alipayConfigProperties.getSignType());
        if(rsaCheckV1)
        {
            PayAsyncVo asyncVo = buildPayAsyncVo(params);
            String result= orderService.handlePayResult(asyncVo);
            return result;
        }
        else
        {
            System.err.println("【安全警告】支付宝异步通知签名验证失败！非法请求来源！");
            return "failure";
        }
    }

    private PayAsyncVo buildPayAsyncVo(Map<String, String> params) {
        PayAsyncVo vo = new PayAsyncVo();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String fieldName = snakeToCamel(entry.getKey());
            try {
                Field field = PayAsyncVo.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(vo, entry.getValue());
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return vo;
    }

    private String snakeToCamel(String snake) {
        if (!snake.contains("_")) return snake;
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') { upper = true; }
            else { sb.append(upper ? Character.toUpperCase(c) : c); upper = false; }
        }
        return sb.toString();
    }
}

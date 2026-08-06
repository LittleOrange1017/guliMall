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
    /**
     * 接收支付宝异步通知的主入口
     * 注意：必须返回纯文本 "success" 或 "failure"，不能返回 JSON 或 HTML！
     */
    @PostMapping("/payed/notify")
    public String handleAlipayNotify(PayAsyncVo asyncVo, HttpServletRequest request) throws AlipayApiException {
        // 1. 将 HttpServletRequest 中的参数转换为 AlipaySignature 验签所需的 Map<String, String>
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
        //调用SDK进行RSA2签名验证
        boolean rsaCheckV1 = AlipaySignature.rsaCheckV1(params, alipayConfigProperties.getAlipayPublicKey(), alipayConfigProperties.getCharset(), alipayConfigProperties.getSignType());
        if(rsaCheckV1)
        {
            //验签成功，交给service层进行订单状态更新以及幂等逻辑
           String result= orderService.handlePayResult(asyncVo);
           return result;
        }
        else
        {
            // 验签失败：可能有人伪造回调，记录严重警告日志
            System.err.println("【安全警告】支付宝异步通知签名验证失败！非法请求来源！");
            return "failure";
        }
    }
}

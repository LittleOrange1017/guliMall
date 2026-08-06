package com.xjz.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.xjz.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class AlipayTemplate {
    @Autowired
    private AlipayConfigProperties alipayConfigProperties;
    /**
     * 核心方法：向支付宝发送支付请求并生成自动提交的 HTML 表单
     *
     * @param payVo 支付参数
     * @return 包含自提交 <script> 的 HTML 表单字符串
     */
    public String Pay(PayVo payVo) throws AlipayApiException {
        //已经组装好了payvo
        //1、初始化DefaultAlipayClient
        AlipayConfig alipayConfig=new AlipayConfig();
        alipayConfig.setAppId(alipayConfigProperties.getAppId());
        alipayConfig.setPrivateKey(alipayConfigProperties.getMerchantprivatekey());
        alipayConfig.setAlipayPublicKey(alipayConfigProperties.getAlipayPublicKey());
        alipayConfig.setServerUrl(alipayConfigProperties.getGatewayUrl());
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
        //2、创建请求对象
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //3、设置同步与异步回调路径
        request.setReturnUrl(alipayConfigProperties.getReturnUrl());
        request.setNotifyUrl(alipayConfigProperties.getNotifyUrl());
        //4、使用面向对象的 Model 组装业务参数（避免手动拼接 JSON 字符串）
        AlipayTradePagePayModel model=new AlipayTradePagePayModel();
        model.setOutTradeNo(payVo.getOutTradeNo());
        model.setTotalAmount(payVo.getTotalAmount());
        model.setSubject(payVo.getSubject());
        model.setBody(payVo.getBody());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        //5、将业务model绑定到Request请求对象中
        request.setBizModel(model);
        //6. 执行请求（采用 POST 方式），获取响应对象
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
        //7. 校验执行结果并返回 HTML 表单
        if (response.isSuccess()) {
            return response.getBody(); // 返回包含自提交 <form id="alipaysubmit"> 的 HTML 文本
        } else {
            // 记录日志并抛出业务异常
            throw new RuntimeException("支付宝下单调用失败：" + response.getMsg() + " - " + response.getSubMsg());
        }
    }
}

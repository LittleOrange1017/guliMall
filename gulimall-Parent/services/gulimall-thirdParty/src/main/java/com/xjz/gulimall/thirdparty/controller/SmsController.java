package com.xjz.gulimall.thirdparty.controller;

import com.xjz.gulimall.thirdparty.properties.SmsMsgProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import utils.R;


@RestController
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    private SmsMsgProperties smsMsgProperties;

    /**
     * 发送短信验证码
     * @param phone 手机号
     * @param code 短信验证码
     */
    @GetMapping("/sendcode")
    public R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers=new HttpHeaders();
        headers.set("Authorization","APPCODE "+smsMsgProperties.getAppcode());
        String baseUrl= smsMsgProperties.getHost()+smsMsgProperties.getPath();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("mobile", phone)
                .queryParam("templateId", smsMsgProperties.getTemplateId())
                .queryParam("smsSignId", smsMsgProperties.getSmsSignId())
                .queryParam("param", "**code**:" + code + ",**minute**:1");
        HttpEntity<String> requestEntity=new HttpEntity<>(headers);
        try{
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(), // 获取拼接好并转码后的最终 URL
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            System.out.println("阿里云国阳云短信发送结果：" + response.getBody());
            return R.ok().put("code",200);
        }catch (Exception e)
        {
            System.out.println("短信发送发生异常：" + e.getMessage());
            return R.error();
        }
    }
}

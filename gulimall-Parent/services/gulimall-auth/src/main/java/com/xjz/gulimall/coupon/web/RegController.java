package com.xjz.gulimall.coupon.web;

import com.xjz.gulimall.coupon.feign.ThirdPartyFeignClient;
import org.mockito.verification.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import utils.R;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Controller
public class RegController {
    @Autowired
    private ThirdPartyFeignClient thirdPartyFeignClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @GetMapping({"/reg.html","/reg"})
    public String loginPage(){
        return "reg";
    }
    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone)
    {
        String key="sms:code:"+phone;
        String redisVal = redisTemplate.opsForValue().get(key);
        if(StringUtils.hasText(redisVal))
        {
            long time = Long.parseLong(redisVal.split("_")[1]);
            if(System.currentTimeMillis()-time<60000)
            {
                return R.error("验证码发送太频繁，请稍后再试");
            }
        }
        Random random=new Random();
        String code= String.valueOf(random.nextInt(900000)+100000);
        String value="code_"+System.currentTimeMillis();
        redisTemplate.opsForValue().set(key,value,5, TimeUnit.MINUTES);
        thirdPartyFeignClient.sendSmsCode(phone,code);
        return R.ok();
    }
}

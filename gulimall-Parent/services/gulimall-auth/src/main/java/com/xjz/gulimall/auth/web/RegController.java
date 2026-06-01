package com.xjz.gulimall.auth.web;

import com.xjz.gulimall.auth.dto.RegDto;
import com.xjz.gulimall.auth.dto.RegFeignDto;
import com.xjz.gulimall.auth.feign.MemberFeignClient;
import com.xjz.gulimall.auth.feign.ThirdPartyFeignClient;
import exception.BizCodeEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import utils.R;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class RegController {
    @Autowired
    private ThirdPartyFeignClient thirdPartyFeignClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MemberFeignClient memberFeignClient;
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
        String value=code+"_"+System.currentTimeMillis();
        redisTemplate.opsForValue().set(key,value,5, TimeUnit.MINUTES);
        thirdPartyFeignClient.sendSmsCode(phone,code);
        return R.ok();
    }
    @PostMapping("/register")
    public String register(@Valid RegDto regDto, BindingResult result, RedirectAttributes redirectAttributes)
    {
        if(result.hasErrors())
        {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (k1, k2) -> k1 // 如果有重复的，取第一个
                    ));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.littleorange.com/reg.html";
        }
        //校验验证码
        String code = regDto.getCode();
        String value = redisTemplate.opsForValue().get("sms:code:" + regDto.getPhone());
        if(value==null)
        {
            Map<String,String> errors=new HashMap<>();
            errors.put("code","验证码过期");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.littleorange.com/reg.html";
        }
        String oldCode=value.split("_")[0];
        if(!code.equals(oldCode))
        {
            Map<String,String> errors=new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.littleorange.com/reg.html";
        }
        /*
        调用member服务进行远程调用业务
         */
        RegFeignDto regFeignDto=new RegFeignDto();
        BeanUtils.copyProperties(regDto,regFeignDto);
        R regist = memberFeignClient.regist(regFeignDto);
        if(regist.get("code").equals(BizCodeEnum.PHONE_EXIST_EXCEPTION))
        {
            Map<String,String> errors=new HashMap<>();
            errors.put("errors", (String) regist.get("msg"));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.littleorange.com/reg.html";
        }
        else if(regist.get("code").equals(BizCodeEnum.USER_EXIST_EXCEPTION))
        {
            Map<String,String> errors=new HashMap<>();
            errors.put("errors", (String) regist.get("msg"));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.littleorange.com/reg.html";
        }
        //删除验证码
        redisTemplate.delete("sms:code:"+regDto.getPhone());
        return "redirect:http://auth.littleorange.com/login.html";
    }
}

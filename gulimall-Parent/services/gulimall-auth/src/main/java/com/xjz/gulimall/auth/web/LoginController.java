package com.xjz.gulimall.auth.web;

import com.xjz.gulimall.auth.dto.LoginDto;
import com.xjz.gulimall.auth.feign.MemberFeignClient;
import exception.BizCodeEnum;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import utils.R;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    private final MemberFeignClient memberFeignClient;

    public LoginController(MemberFeignClient memberFeignClient) {
        this.memberFeignClient = memberFeignClient;
    }

    @GetMapping({"/login.html","/","/login"})
    public String loginPage() {
        return "login";
    }
    @PostMapping("/login")
    public String login(@Valid LoginDto loginDto, BindingResult result, RedirectAttributes redirectAttributes)
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
            redirectAttributes.addFlashAttribute("dto",loginDto);
            return "redirect:http://auth.littleorange.com/login.html";
        }
        R loginResult = memberFeignClient.login(loginDto);
        Map<String,String> errors=new HashMap<>();
        if(loginResult.get("code").equals(BizCodeEnum.USERNAME_NOT_EXIST.getCode()))
        {
            errors.put("loginacct", (String) loginResult.get("msg"));
        }
        else if(loginResult.get("code").equals(BizCodeEnum.PASSWORD_ERROR.getCode()))
        {
            errors.put("password", (String) loginResult.get("msg"));
        }
        if(!errors.isEmpty())
        {
            redirectAttributes.addFlashAttribute("errors",errors);
            redirectAttributes.addFlashAttribute("dto",loginDto);
            return "redirect:http://auth.littleorange.com/login.html";
        }
        return "redirect:http://littleorange.com/index.html";
    }
}

package com.xjz.gulimall.productA.controller;

import com.xjz.gulimall.productA.feign.SSOFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import utils.R;

import javax.servlet.http.HttpSession;

@Controller
public class ProductAController {
    @Autowired
    private SSOFeignClient ssoFeignClient;
    @GetMapping("/employees")
    public String employees(Model model, HttpSession session, @RequestParam(value = "token",required = false) String token)
    {
        Object loginUser = session.getAttribute("loginUser");
        if(loginUser!=null)
        {
            return "employees";
        }
        if(StringUtils.hasText(token))
        {
            //刚从验证中心跳转回来
            R verify = ssoFeignClient.verify(token);
            //如果合法，让那边把用户信息发过来
            String userinfo = verify.get("msg").toString();
            session.setAttribute("loginUser",userinfo);
            return "redirect:http://productA.com:10003/employees";
        }
        // 3. 既没有本地Session，也没有合法的Token，老老实实去中央认证中心登录
        String ssoServerUrl = "http://sso-server.com:10001/login.html";
        String currentUrl = "http://productA.com:10003/employees";
        return "redirect:" + ssoServerUrl + "?redirect_url=" + currentUrl;
    }
}

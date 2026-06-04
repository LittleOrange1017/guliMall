package com.xjz.gulimall.sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import utils.R;

import javax.servlet.http.HttpSession;
import java.util.UUID;

@Controller
public class SsoServerController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 渲染统一登录页
     */
    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url, Model model,HttpSession session) {
        Object ssoUser = session.getAttribute("ssoUser");
        if(ssoUser!=null)
        {
            String token = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(token,ssoUser.toString());
            return "redirect:" + url + "?token=" + token;
        }
        // 把客户端的来源地址存入 Thymeleaf 上下文，供表单隐藏域使用
        model.addAttribute("url", url);
        return "login";
    }
    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("redirect_url") String redirectUrl,
                          HttpSession session) {

        // 1. 模拟账密校验
        if ("admin".equals(username) && "123456".equals(password)) {

            // 2. 💡 核心高亮：登录成功！生成一个全系统唯一的、短命的临时门票（Token/Ticket）
            String token = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(token,username);
            session.setAttribute("ssoUser",username);
            // 3. 将用户重定向回他原本要访问的客户端系统，并在 URL 后面把令牌作为参数挂上去！
            return "redirect:" + redirectUrl + "?token=" + token;
        }
        // 登录失败，留在原页（实际中应带上错误提示，此处为了好理解做简化）
        return "login";
    }
    @ResponseBody
    @PostMapping("/verify")
    R verify(@RequestParam("token") String token)
    {
        String userinfo = stringRedisTemplate.opsForValue().get(token);
        if(StringUtils.hasText(userinfo))
        {
            stringRedisTemplate.delete(token);
            return R.ok(userinfo);
        }
        return R.error();
    }
}

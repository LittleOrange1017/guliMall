package com.xjz.gulimall.auth.web;

import com.alibaba.fastjson.JSON;
import com.xjz.gulimall.auth.config.GiteeOauthProperties;
import com.xjz.gulimall.auth.dto.GiteeTokenDto;
import com.xjz.gulimall.auth.dto.GiteeUserDto;
import com.xjz.gulimall.auth.dto.LoginDto;
import com.xjz.gulimall.auth.feign.MemberFeignClient;
import com.xjz.gulimall.auth.vo.GiteeTokenVo;
import vo.MemberVo;
import exception.BizCodeEnum;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import utils.R;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.lang.reflect.Member;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class LoginController {
    @Autowired
    private GiteeOauthProperties giteeOauthProperties;
    private final MemberFeignClient memberFeignClient;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

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
    @GetMapping("/success")
    public String giteeCallback(@RequestParam("code") String code, HttpSession session) throws IOException, InterruptedException {
        String clientSecret = giteeOauthProperties.getClient_Secret();
        RestTemplate restTemplate=new RestTemplate();
        String redirectUri = giteeOauthProperties.getRedirect_uri();
        String clientId = giteeOauthProperties.getClient_Id();
        GiteeTokenVo giteeTokenVo=new GiteeTokenVo();
        giteeTokenVo.setCode(code);
        giteeTokenVo.setClient_id(clientId);
        giteeTokenVo.setClient_secret(clientSecret);
        giteeTokenVo.setGrant_type("authorization_code");
        giteeTokenVo.setRedirect_uri(redirectUri);
        String jsonBody = JSON.toJSONString(giteeTokenVo);
        String url="https://gitee.com/oauth/token";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("User-Agent", "MyApp/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> httpResponse = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        String body = httpResponse.body();
        GiteeTokenDto giteeTokenDto = JSON.parseObject(body, GiteeTokenDto.class);
        //发送第二次GET请求，去获取当前用户的详细资料
        String accessToken = giteeTokenDto.getAccess_token();
        if(accessToken==null||accessToken.isEmpty())
        {
            return "redirect:http://auth.littleorange.com/login.html";
        }
        String userUrl = "https://gitee.com/api/v5/user?access_token=" + accessToken;
        HttpRequest userReq = HttpRequest.newBuilder()
                .uri(URI.create(userUrl))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "MyApp/1.0")
                .GET()
                .build();
        HttpResponse<String> userHttpResponse = httpClient.send(userReq, HttpResponse.BodyHandlers.ofString());
        String userBody = userHttpResponse.body();
        log.info(userBody);
        GiteeUserDto giteeUserDto = JSON.parseObject(userBody, GiteeUserDto.class);
        if (giteeUserDto == null || giteeUserDto.getId() == null) {
            return "redirect:http://auth.littleorange.com/login.html";
        }
        //远程调用会员服务的登录或者注册判断方法进行后续业务处理
        R r = memberFeignClient.loginOrRegist(giteeUserDto);
        MemberVo memberVo=new MemberVo();
        memberVo.setAvatar_url(giteeUserDto.getAvatar_url());
        memberVo.setUsername(giteeUserDto.getLogin());
        session.setAttribute("loginUser",memberVo);
        return "redirect:http://littleorange.com";
    }
}

package com.xjz.gulimall.product.exception;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import utils.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: GulimallExceptionControllerAdvice
 * Package:com.xjz.gulimall.product.exception
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/6 9:39
 * @Version 1.0
 */
@RestControllerAdvice(basePackages = "com.xjz.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handlerJSR303Exception(MethodArgumentNotValidException e) {
        // 1. 获取包含错误字段详细信息的 BindingResult
        BindingResult bindingResult = e.getBindingResult();

        // 2. 准备一个 Map 来专门存放 "字段名" -> "错误提示"
        Map<String, String> errorMap = new HashMap<>();

        // 3. 遍历所有的字段错误 (FieldError 包含了具体的属性名)
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            // fieldError.getField() 获取到的是 "name", "firstLetter" 等属性名
            // fieldError.getDefaultMessage() 获取到的是注解上的 message
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        // 4. 将 Map 放入统一响应对象中返回（建议使用特定的状态码，如 40000 代表参数校验失败）
        return R.error(40000, "提交的数据格式不正确").put("data", errorMap);
    }
}

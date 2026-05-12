package com.xjz.gulimall.product.web;

import com.xjz.gulimall.product.entity.CategoryEntity;
import com.xjz.gulimall.product.service.CategoryService;
import com.xjz.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping({"/","/index","/index.html"})
    public String indexPage(Model model){
        // TODO 1. 查出所有的 1 级分类数据
        List<CategoryEntity> categoryEntityList=categoryService.getLevel1Categorys();
        // TODO 2. 存入 Model 中，供前端 Thymeleaf 页面获取
        model.addAttribute("categorys",categoryEntityList);
        // 3. 视图解析器工作：寻找 classpath:/templates/index.html 进行渲染
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() throws InterruptedException {
        return categoryService.getCatelogJson();
    }

    @ResponseBody
    @GetMapping("/hello")
    public String testHello(){
        return "hello";
    }
}

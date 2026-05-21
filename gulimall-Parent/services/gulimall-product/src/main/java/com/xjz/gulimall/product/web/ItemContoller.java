package com.xjz.gulimall.product.web;

import com.xjz.gulimall.product.entity.CategoryEntity;
import com.xjz.gulimall.product.service.SkuInfoService;
import com.xjz.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ItemContoller {
    @Autowired
    private SkuInfoService skuInfoService;
    @GetMapping({"/{skuId}"})
    public String skuItem(@PathVariable("skuId") Long skuId, Model model){
        SkuItemVo itemVo=skuInfoService.getItem(skuId);
        model.addAttribute("itemVo",itemVo);
        return "item";
    }
}

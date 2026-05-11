package com.xjz.gulimall.search.controller;

import com.xjz.gulimall.search.service.ElasticsearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import to.SkuEsModel;
import utils.R;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search")
@Slf4j
public class ElasticsearchSaveController {
    @Autowired
    private ElasticsearchSaveService elasticsearchSaveService;
    @RequestMapping("/save")
    R saveUp(@RequestBody List<SkuEsModel> skuEsModels)  {
        boolean b=false;
        try {
            // 调用 service 执行保存
            b = elasticsearchSaveService.saveUp(skuEsModels);
        } catch (Exception e) {
            log.error("ElasticSaveController商品上架错误: {}", e);
            // 这里返回专门的商品上架异常状态码
            return R.error(400,"商品保存失败");
        }
        if(b)
        {
            return R.error(400,"商品保存失败");
        }
        else
        {
            return R.ok();
        }
    }
}

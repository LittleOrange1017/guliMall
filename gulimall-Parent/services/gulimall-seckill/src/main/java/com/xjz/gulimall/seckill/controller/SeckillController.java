package com.xjz.gulimall.seckill.controller;

import com.xjz.gulimall.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import to.SeckillSkuRedisTo;
import utils.R;

import java.util.List;

@Controller
public class SeckillController {
    @Autowired
    private SeckillService seckillService;

    /**
     * 场景 1：获取当前时间段正在参与秒杀的所有商品列表
     */
    @GetMapping("/currentSeckillSkus")
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        return seckillService.getCurrentSeckillSkus();
    }


}

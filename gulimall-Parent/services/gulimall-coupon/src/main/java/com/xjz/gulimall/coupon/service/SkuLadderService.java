package com.xjz.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import utils.PageUtils;
import com.xjz.gulimall.coupon.entity.SkuLadderEntity;

import java.util.Map;

/**
 * 商品阶梯价格
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 10:33:22
 */
public interface SkuLadderService extends IService<SkuLadderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


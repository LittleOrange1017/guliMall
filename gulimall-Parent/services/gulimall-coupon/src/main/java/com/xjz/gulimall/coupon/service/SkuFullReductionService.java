package com.xjz.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import to.SkuReductionTo;
import utils.PageUtils;
import com.xjz.gulimall.coupon.entity.SkuFullReductionEntity;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品满减信息
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 10:33:22
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo, BigDecimal price);

}


package com.xjz.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import utils.PageUtils;
import com.xjz.gulimall.coupon.entity.SpuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 10:33:22
 */
public interface SpuFullReductionService extends IService<SpuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


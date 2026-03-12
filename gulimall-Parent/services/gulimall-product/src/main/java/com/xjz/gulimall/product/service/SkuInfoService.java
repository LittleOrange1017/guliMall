package com.xjz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.product.entity.SkuInfoEntity;
import utils.PageUtils;

import java.util.Map;

/**
 * sku信息
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:24
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


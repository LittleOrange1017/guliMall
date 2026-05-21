package com.xjz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.product.entity.SkuInfoEntity;
import com.xjz.gulimall.product.vo.SkuItemVo;
import utils.PageUtils;

import java.util.List;
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

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    SkuInfoEntity getSkuInfo(Long skuId);

    List<SkuInfoEntity> getSkuBySpuId(Long spuId);

    SkuItemVo getItem(Long skuId);
}


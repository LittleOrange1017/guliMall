package com.xjz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.product.entity.CategoryBrandRelationEntity;
import utils.PageUtils;

import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-04-14 12:32:12
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void mySave(Long brandId, Long categoryId);
    void updateBrandName(Long brandId, String name);

    void updateCategoryName(Long catId, String name);
}


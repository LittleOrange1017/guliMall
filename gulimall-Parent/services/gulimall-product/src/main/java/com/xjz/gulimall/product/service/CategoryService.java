package com.xjz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.product.entity.CategoryEntity;
import com.xjz.gulimall.product.vo.Catelog2Vo;
import utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:25
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    int removeCategoryByIds(List<Long> asList);

    int saveCategory(CategoryEntity category);

    int updateCategoryById(CategoryEntity category);

    Long[] findCatelogIds(Long catelogId);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, List<Catelog2Vo>> getCatelogJson() throws InterruptedException;
}


package com.xjz.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.coupon.entity.CategoryBoundsEntity;
import utils.PageUtils;

import java.util.Map;

/**
 * 商品分类积分设置
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 10:33:23
 */
public interface CategoryBoundsService extends IService<CategoryBoundsEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


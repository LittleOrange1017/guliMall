package com.xjz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.product.entity.ProductAttrValueEntity;
import utils.PageUtils;

import java.util.Map;

/**
 * spu属性值
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:24
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


package com.xjz.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.coupon.entity.AttrAttrgroupRelationEntity;
import utils.PageUtils;

import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:25
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


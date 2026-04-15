package com.xjz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.product.dto.AttrDto;
import com.xjz.gulimall.product.entity.AttrEntity;
import com.xjz.gulimall.product.vo.AttrInfoVo;
import utils.PageUtils;

import java.util.Map;

/**
 * 商品属性
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:25
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void save(AttrDto attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId);

    AttrInfoVo getInfo(Long attrId);

    void updateAttr(AttrDto attr);
}


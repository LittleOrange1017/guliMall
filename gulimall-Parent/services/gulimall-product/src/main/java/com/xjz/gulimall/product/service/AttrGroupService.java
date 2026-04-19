package com.xjz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.product.dto.AttrGroupRelationDto;
import com.xjz.gulimall.product.entity.AttrEntity;
import com.xjz.gulimall.product.entity.AttrGroupEntity;
import com.xjz.gulimall.product.vo.AttrgroupWithattrVo;
import utils.PageUtils;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:25
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);
    PageUtils queryPage(Map<String, Object> params, Long categoryId);

    List<AttrEntity> attrRelation(Long attrGroupId);

    void deleteRelation(List<AttrGroupRelationDto> attrGroupRelationDto);

    void addRelation(List<AttrGroupRelationDto> attrGroupRelationDto);

    List<AttrgroupWithattrVo> getGroupWithattr(Long catelogId);
}


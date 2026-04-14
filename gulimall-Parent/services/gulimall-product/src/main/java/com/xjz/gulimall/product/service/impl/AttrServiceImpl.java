package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.dao.AttrDao;
import com.xjz.gulimall.product.dto.AttrDto;
import com.xjz.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.xjz.gulimall.product.entity.AttrEntity;
import com.xjz.gulimall.product.service.AttrAttrgroupRelationService;
import com.xjz.gulimall.product.service.AttrGroupService;
import com.xjz.gulimall.product.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.PageUtils;
import utils.Query;

import java.util.Map;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void save(AttrDto attr) {
        AttrEntity attrEntity=new AttrEntity(null,attr.getAttrName(),attr.getSearchType(),attr.getValueType(),attr.getIcon(),attr.getValueSelect(),attr.getAttrType(),attr.getEnable(),attr.getCatelogId(),attr.getShowDesc());
        this.save(attrEntity);
        String attrName = attr.getAttrName();
        QueryWrapper<AttrEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("attr_name",attrName);
        AttrEntity attrEntity1 = this.getOne(queryWrapper);
        Long attrId = attrEntity1.getAttrId();
        Long attrGroupId = attr.getAttrGroupId();
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity=new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrId(attrId);
        attrAttrgroupRelationEntity.setAttrGroupId(attrGroupId);
        attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
    }

}
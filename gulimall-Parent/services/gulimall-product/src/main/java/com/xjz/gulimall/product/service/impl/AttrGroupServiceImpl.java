package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.dao.AttrDao;
import com.xjz.gulimall.product.dao.AttrGroupDao;
import com.xjz.gulimall.product.dto.AttrGroupRelationDto;
import com.xjz.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.xjz.gulimall.product.entity.AttrEntity;
import com.xjz.gulimall.product.entity.AttrGroupEntity;
import com.xjz.gulimall.product.service.AttrAttrgroupRelationService;
import com.xjz.gulimall.product.service.AttrGroupService;
import com.xjz.gulimall.product.service.AttrService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.PageUtils;
import utils.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    AttrDao attrDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId) {
            String key=(String) params.get("key");
            QueryWrapper<AttrGroupEntity> queryWrapper=new QueryWrapper<>();
            if(!StringUtils.isEmpty(key))
            {
                queryWrapper.and(attrGroupEntityQueryWrapper -> attrGroupEntityQueryWrapper.eq("attr_group_id",key).or().like("attr_group_name",key));
            }
            if(categoryId==0)
            {
                IPage<AttrGroupEntity> page = this.page(
                        new Query<AttrGroupEntity>().getPage(params),
                        queryWrapper
                );
                return new PageUtils(page);
            }
            else {
                queryWrapper.eq("catelog_id", categoryId);
                IPage<AttrGroupEntity> page = this.page(
                        new Query<AttrGroupEntity>().getPage(params),
                        queryWrapper
                );
                return new PageUtils(page);
            }
    }

    @Override
    public List<AttrEntity> attrRelation(Long attrGroupId) {
        List<AttrEntity> attrEntities=new ArrayList<>();
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
        List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(attrAttrgroupRelationEntity -> attrAttrgroupRelationEntity.getAttrId()).collect(Collectors.toList());
        for(Long attrId:attrIds)
        {
            attrEntities.add(attrDao.selectById(attrId));
        }
        return attrEntities;
    }

    @Override
    public void deleteRelation(List<AttrGroupRelationDto> attrGroupRelationDto) {
        if(attrGroupRelationDto!=null&&attrGroupRelationDto.size()>1)
        {
            List<Long> attrIds=new ArrayList<>();
            for(AttrGroupRelationDto relationDto:attrGroupRelationDto)
            {
                attrIds.add(relationDto.getAttrId());
            }
            attrAttrgroupRelationService.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",attrGroupRelationDto.get(0).getAttrGroupId()).in("attr_id",attrIds));
        }
        else
        {
            attrAttrgroupRelationService.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrGroupRelationDto.get(0).getAttrId()));
        }
    }

}
package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.dao.AttrDao;
import com.xjz.gulimall.product.dto.AttrDto;
import com.xjz.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.xjz.gulimall.product.entity.AttrEntity;
import com.xjz.gulimall.product.entity.AttrGroupEntity;
import com.xjz.gulimall.product.entity.CategoryEntity;
import com.xjz.gulimall.product.service.AttrAttrgroupRelationService;
import com.xjz.gulimall.product.service.AttrGroupService;
import com.xjz.gulimall.product.service.AttrService;
import com.xjz.gulimall.product.service.CategoryService;
import com.xjz.gulimall.product.vo.AttrInfoVo;
import com.xjz.gulimall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utils.PageUtils;
import utils.Query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    CategoryService categoryService;

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

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> queryWrapper=new QueryWrapper<>();
        if(catelogId!=0)
        {
            queryWrapper.eq("catelog_id",catelogId);
        }
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key))
        {
            queryWrapper.and(attrEntityQueryWrapper -> attrEntityQueryWrapper.eq("attr_id",key).or().like("attr_name",key));
        }
        if(attrType!=null)
        {
            if(attrType.equals("base"))
            {
                queryWrapper.eq("attr_type",1);
            }
            if(attrType.equals("sale"))
            {
                queryWrapper.eq("attr_type",0);
            }
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
                );
        List<AttrEntity> records = page.getRecords();
        List<AttrVo> collect = records.stream().map(attrEntity -> {
            AttrVo attrVo = new AttrVo();
            BeanUtils.copyProperties(attrEntity, attrVo);
            Long attrId = attrEntity.getAttrId();
            Long catelogId1 = attrEntity.getCatelogId();
            CategoryEntity categoryEntity = categoryService.getOne(new QueryWrapper<CategoryEntity>().eq("cat_id", catelogId1));
            if(categoryEntity!=null)
            {
                attrVo.setCategoryName(categoryEntity.getName());
            }
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if(attrAttrgroupRelationEntity!=null)
            {
                Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
                AttrGroupEntity attrGroupEntity = attrGroupService.getOne(new QueryWrapper<AttrGroupEntity>().eq("attr_group_id", attrGroupId));
                if(attrGroupEntity!=null)
                {
                    attrVo.setAttrGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            return attrVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils=new PageUtils(page);
        pageUtils.setList(collect);
        return pageUtils;
    }

    @Override
    public AttrInfoVo getInfo(Long attrId) {
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        AttrEntity attrEntity = this.getOne(new QueryWrapper<AttrEntity>().eq("attr_id", attrId));
        AttrInfoVo attrInfoVo = new AttrInfoVo();
        BeanUtils.copyProperties(attrEntity, attrInfoVo);
        if (attrAttrgroupRelationEntity != null) {
            attrInfoVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
        }
        Long[] catelogIds = categoryService.findCatelogIds(attrEntity.getCatelogId());
        attrInfoVo.setCatelogPath(catelogIds);
        return attrInfoVo;
    }

    @Override
    @Transactional
    public void updateAttr(AttrDto attr) {
        //修改基本属性表
        AttrEntity attrEntity=new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        //修改关联表
        Long attrGroupId = attr.getAttrGroupId();
        if(attrGroupId!=null)
        {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity=new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrGroupId);
            attrAttrgroupRelationService.update(attrAttrgroupRelationEntity,new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attr.getAttrId()));
        }

    }

    @Override
    public PageUtils attrNoRelation(Long attrgroupId, Map<String, Object> params) {
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //查出当前分类下的所有分组
        List<AttrGroupEntity> attrEntities = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> groupIds = attrEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        //关联表中查出已经关联的属性
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        List<Long> usedattrIds = attrAttrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        //构建查询条件
        QueryWrapper<AttrEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("catelog_id", catelogId).eq("attr_type",1);
        if(usedattrIds!=null&&usedattrIds.size()>0)
        {
            queryWrapper.notIn("attr_id", usedattrIds);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page =this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }


}
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
import com.xjz.gulimall.product.entity.ProductAttrValueEntity;
import com.xjz.gulimall.product.service.AttrAttrgroupRelationService;
import com.xjz.gulimall.product.service.AttrGroupService;
import com.xjz.gulimall.product.service.AttrService;
import com.xjz.gulimall.product.vo.AttrgroupWithattrVo;
import com.xjz.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.PageUtils;
import utils.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    AttrDao attrDao;
    @Autowired
    private ProductAttrValueServiceImpl productAttrValueService;

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
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(attrGroupEntityQueryWrapper -> attrGroupEntityQueryWrapper.eq("attr_group_id", key).or().like("attr_group_name", key));
        }
        if (categoryId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    queryWrapper
            );
            return new PageUtils(page);
        } else {
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
        List<AttrEntity> attrEntities = new ArrayList<>();
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupId));
        List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(attrAttrgroupRelationEntity -> attrAttrgroupRelationEntity.getAttrId()).collect(Collectors.toList());
        for (Long attrId : attrIds) {
            attrEntities.add(attrDao.selectById(attrId));
        }
        return attrEntities;
    }

    @Override
    public void deleteRelation(List<AttrGroupRelationDto> attrGroupRelationDto) {
        if (attrGroupRelationDto != null && attrGroupRelationDto.size() > 1) {
            List<Long> attrIds = new ArrayList<>();
            for (AttrGroupRelationDto relationDto : attrGroupRelationDto) {
                attrIds.add(relationDto.getAttrId());
            }
            attrAttrgroupRelationService.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupRelationDto.get(0).getAttrGroupId()).in("attr_id", attrIds));
        } else {
            attrAttrgroupRelationService.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrGroupRelationDto.get(0).getAttrId()));
        }
    }

    @Override
    public void addRelation(List<AttrGroupRelationDto> attrGroupRelationDto) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrGroupRelationDto.stream().map(new Function<AttrGroupRelationDto, AttrAttrgroupRelationEntity>() {
            @Override
            public AttrAttrgroupRelationEntity apply(AttrGroupRelationDto attrGroupRelationDto) {
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                BeanUtils.copyProperties(attrGroupRelationDto, attrAttrgroupRelationEntity);
                return attrAttrgroupRelationEntity;
            }
        }).collect(Collectors.toList());
        attrAttrgroupRelationService.saveBatch(attrAttrgroupRelationEntities);
    }

    @Override
    public List<AttrgroupWithattrVo> getGroupWithattr(Long catelogId) {
        //查出当前分类下的所有分组ID
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        if(attrGroupEntities==null||attrGroupEntities.size()==0)
        {
            return null;
        }
        List<Long> groupIds = attrGroupEntities.stream().map(attrGroupEntity -> attrGroupEntity.getAttrGroupId()).collect(Collectors.toList());
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        if(attrAttrgroupRelationEntities==null||attrAttrgroupRelationEntities.size()==0) {
            return attrGroupEntities.stream().map(attrGroupEntity -> {
                AttrgroupWithattrVo attrgroupWithattrVo=new AttrgroupWithattrVo();
                BeanUtils.copyProperties(attrGroupEntity,attrgroupWithattrVo);
                return attrgroupWithattrVo;
            }).collect(Collectors.toList());
        }
        List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(new Function<AttrAttrgroupRelationEntity, Long>() {
            @Override
            public Long apply(AttrAttrgroupRelationEntity attrAttrgroupRelationEntity) {
                return attrAttrgroupRelationEntity.getAttrId();
            }
        }).distinct().collect(Collectors.toList());
        List<AttrEntity> attrEntities = attrDao.selectByIds(attrIds);
        Map<Long,AttrEntity> attrMap=new HashMap<>();
        for (AttrEntity attrEntity : attrEntities) {
            attrMap.put(attrEntity.getAttrId(),attrEntity);
        };
        Map<Long, List<AttrAttrgroupRelationEntity>> relationGroupMap = attrAttrgroupRelationEntities.stream().collect(Collectors.groupingBy(attrAttrgroupRelationEntity -> attrAttrgroupRelationEntity.getAttrGroupId()));
        List<AttrgroupWithattrVo> withattrVos = attrGroupEntities.stream().map(new Function<AttrGroupEntity, AttrgroupWithattrVo>() {
            @Override
            public AttrgroupWithattrVo apply(AttrGroupEntity attrGroupEntity) {
                AttrgroupWithattrVo attrgroupWithattrVo = new AttrgroupWithattrVo();
                BeanUtils.copyProperties(attrGroupEntity, attrgroupWithattrVo);
                List<AttrAttrgroupRelationEntity> relations = relationGroupMap.get(attrGroupEntity.getAttrGroupId());
                if (relations != null) {
                    List<AttrEntity> collect = relations.stream().map(new Function<AttrAttrgroupRelationEntity, AttrEntity>() {
                        @Override
                        public AttrEntity apply(AttrAttrgroupRelationEntity attrAttrgroupRelationEntity) {
                            return attrMap.get(attrAttrgroupRelationEntity.getAttrId());
                        }
                    }).collect(Collectors.toList());
                    attrgroupWithattrVo.setAttrs(collect);
                }
                return attrgroupWithattrVo;
            }
        }).collect(Collectors.toList());
        return withattrVos;
    }

    @Override
    public List<SkuItemVo.AttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId,Long catalogId) {
        //1、查出当前spu对应的所有属性分组信息
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catalogId));
        if(groupEntities.isEmpty())
        {
            return null;
        }
        //把这分组的 ID 抽成一个集合，准备拿去查第二步的关联关系
        List<Long> groupIds = groupEntities.stream().map(attrGroupEntity -> attrGroupEntity.getAttrGroupId()).collect(Collectors.toList());
        //根据上一步拿到的 groupIds 集合，批量查出关联关系表
        List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        //把这个分组和属性之间的关联实体转成一个字典map
        //目的就是 以后我想找“基本信息”组下的所有属性，直接map.get(基本信息组Id)就能拿到，不需要再for循环找了
        //map格式就是 <Long groupId,List<AttrAttrgroupRelationEntity>>
        Map<Long, List<AttrAttrgroupRelationEntity>> groupRelationMap = relations.stream().collect(Collectors.groupingBy(AttrAttrgroupRelationEntity::getAttrGroupId));
        //然后就是查具体的SPU底下的规格参数的值
        List<ProductAttrValueEntity> spuAttrValues = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        //同样，把它也变成map，key是属性ID，value是这个属性实体类
        Map<Long, ProductAttrValueEntity> spuAttrMap = spuAttrValues.stream().collect(Collectors.toMap(
                ProductAttrValueEntity::getAttrId,
                v -> v,
                (k1, k2) -> k1));
        return groupEntities.stream().map(new Function<AttrGroupEntity, SkuItemVo.AttrGroupVo>() {
            @Override
            public SkuItemVo.AttrGroupVo apply(AttrGroupEntity attrGroupEntity) {
                SkuItemVo.AttrGroupVo attrGroupVo = new SkuItemVo.AttrGroupVo();
                attrGroupVo.setAttrGroupName(attrGroupEntity.getAttrGroupName());
                //提取出每一个分组对应的属性信息
                List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = groupRelationMap.get(attrGroupEntity.getAttrGroupId());
                if (attrAttrgroupRelationEntities != null && !attrAttrgroupRelationEntities.isEmpty()) {
                    //遍历这些属性信息，根据属性信息去找对应的真实的值
                    List<SkuItemVo.BaseAttrVo> baseAttrVos = attrAttrgroupRelationEntities.stream().filter(relation -> spuAttrMap.containsKey(relation.getAttrId()))
                            .map(new Function<AttrAttrgroupRelationEntity, SkuItemVo.BaseAttrVo>() {
                                @Override
                                public SkuItemVo.BaseAttrVo apply(AttrAttrgroupRelationEntity attrAttrgroupRelationEntity) {
                                    SkuItemVo.BaseAttrVo baseAttrVo = new SkuItemVo.BaseAttrVo();
                                    ProductAttrValueEntity attrValue = spuAttrMap.get(attrAttrgroupRelationEntity.getAttrId());
                                    baseAttrVo.setAttrName(attrValue.getAttrName());
                                    baseAttrVo.setAttrValue(attrValue.getAttrValue());
                                    return baseAttrVo;
                                }
                            }).collect(Collectors.toList());
                    attrGroupVo.setAttrVos(baseAttrVos);
                }
                return attrGroupVo;
            }
        }).collect(Collectors.toList());
    }
}
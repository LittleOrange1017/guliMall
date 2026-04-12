package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.dao.AttrGroupDao;
import com.xjz.gulimall.product.entity.AttrGroupEntity;
import com.xjz.gulimall.product.service.AttrGroupService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import utils.PageUtils;
import utils.Query;

import java.util.Map;
import java.util.function.Consumer;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

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

}
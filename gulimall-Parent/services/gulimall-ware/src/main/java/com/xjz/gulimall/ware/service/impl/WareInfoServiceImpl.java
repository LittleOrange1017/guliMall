package com.xjz.gulimall.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.function.Consumer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import utils.PageUtils;
import utils.Query;


import com.xjz.gulimall.ware.dao.WareInfoDao;
import com.xjz.gulimall.ware.entity.WareInfoEntity;
import com.xjz.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper=new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key))
        {
            queryWrapper.and(wareInfoEntityQueryWrapper -> wareInfoEntityQueryWrapper.eq("id",key).or().like("name",key));
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}
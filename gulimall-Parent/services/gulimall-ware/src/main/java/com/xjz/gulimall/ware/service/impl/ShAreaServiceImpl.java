package com.xjz.gulimall.ware.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import utils.PageUtils;
import utils.Query;


import com.xjz.gulimall.ware.dao.ShAreaDao;
import com.xjz.gulimall.ware.entity.ShAreaEntity;
import com.xjz.gulimall.ware.service.ShAreaService;


@Service("shAreaService")
public class ShAreaServiceImpl extends ServiceImpl<ShAreaDao, ShAreaEntity> implements ShAreaService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ShAreaEntity> page = this.page(
                new Query<ShAreaEntity>().getPage(params),
                new QueryWrapper<ShAreaEntity>()
        );

        return new PageUtils(page);
    }

}
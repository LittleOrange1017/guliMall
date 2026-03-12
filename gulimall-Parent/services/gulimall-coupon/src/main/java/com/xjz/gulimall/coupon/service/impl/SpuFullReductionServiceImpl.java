package com.xjz.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import utils.PageUtils;
import utils.Query;

import com.xjz.gulimall.coupon.dao.SpuFullReductionDao;
import com.xjz.gulimall.coupon.entity.SpuFullReductionEntity;
import com.xjz.gulimall.coupon.service.SpuFullReductionService;


@Service("spuFullReductionService")
public class SpuFullReductionServiceImpl extends ServiceImpl<SpuFullReductionDao, SpuFullReductionEntity> implements SpuFullReductionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuFullReductionEntity> page = this.page(
                new Query<SpuFullReductionEntity>().getPage(params),
                new QueryWrapper<SpuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

}
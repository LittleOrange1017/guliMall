package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import utils.Query;
import com.xjz.gulimall.product.dao.BrandDao;
import com.xjz.gulimall.product.entity.BrandEntity;
import com.xjz.gulimall.product.service.BrandService;
import org.springframework.stereotype.Service;
import utils.PageUtils;

import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Autowired
    private BrandDao brandDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                new QueryWrapper<BrandEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public int updateStatusById(BrandEntity brand) {
        LambdaUpdateWrapper<BrandEntity> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.eq(BrandEntity::getBrandId,brand.getBrandId());
        updateWrapper.set(BrandEntity::getShowStatus,brand.getShowStatus());
        return brandDao.update(brand,updateWrapper);
    }

}
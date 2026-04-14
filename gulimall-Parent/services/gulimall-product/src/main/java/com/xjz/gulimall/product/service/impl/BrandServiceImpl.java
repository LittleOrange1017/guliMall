package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.entity.CategoryBrandRelationEntity;
import com.xjz.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import utils.Query;
import com.xjz.gulimall.product.dao.BrandDao;
import com.xjz.gulimall.product.entity.BrandEntity;
import com.xjz.gulimall.product.service.BrandService;
import org.springframework.stereotype.Service;
import utils.PageUtils;

import java.util.Map;
import java.util.function.Consumer;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> queryWrapper=new QueryWrapper<>();
        if(params.get("key")!=null){
            queryWrapper.and(new Consumer<QueryWrapper<BrandEntity>>() {
                @Override
                public void accept(QueryWrapper<BrandEntity> brandEntityQueryWrapper) {
                    brandEntityQueryWrapper.like("name",params.get("key"))
                            .or()
                            .eq("brand_id",params.get("key"));
                }
            });
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
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

    @Override
    @Transactional
    public void updateDetail(BrandEntity brand) {
        this.updateById(brand);
        if(!StringUtils.isEmpty(brand.getName()))
        {
           categoryBrandRelationService.updateBrandName(brand.getBrandId(), brand.getName());
           //TODO 更新其他关联
        }
    }

}
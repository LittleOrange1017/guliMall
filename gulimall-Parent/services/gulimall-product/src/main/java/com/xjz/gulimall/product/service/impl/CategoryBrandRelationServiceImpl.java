package com.xjz.gulimall.product.service.impl;

import com.xjz.gulimall.product.dao.BrandDao;
import com.xjz.gulimall.product.dao.CategoryDao;
import com.xjz.gulimall.product.entity.BrandEntity;
import com.xjz.gulimall.product.entity.CategoryEntity;
import com.xjz.gulimall.product.service.BrandService;
import com.xjz.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.dao.CategoryBrandRelationDao;
import com.xjz.gulimall.product.entity.CategoryBrandRelationEntity;
import com.xjz.gulimall.product.service.CategoryBrandRelationService;
import utils.PageUtils;
import utils.Query;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private BrandDao brandDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void mySave(Long brandId, Long catelogId) {
        QueryWrapper<BrandEntity> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("brand_id", brandId);
        BrandEntity brand = brandDao.selectOne(queryWrapper1);
        QueryWrapper<CategoryEntity> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("cat_id", catelogId);
        CategoryEntity category = categoryDao.selectOne(queryWrapper2);
        save(new CategoryBrandRelationEntity(null, brandId, catelogId, brand.getName(), category.getName()));

    }

    @Override
    public void updateBrandName(Long brandId, String name) {
        QueryWrapper<CategoryBrandRelationEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("brand_id",brandId);
        update(new CategoryBrandRelationEntity(null,brandId,null,name,null),queryWrapper);
    }

    @Override
    public void updateCategoryName(Long catId, String name) {
        QueryWrapper<CategoryBrandRelationEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("catelog_id",catId);
        update(new CategoryBrandRelationEntity(null,null,catId,null,name),queryWrapper);
    }
}
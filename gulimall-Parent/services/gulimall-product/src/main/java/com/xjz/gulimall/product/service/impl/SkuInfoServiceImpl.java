package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import utils.Query;
import com.xjz.gulimall.product.dao.SkuInfoDao;
import com.xjz.gulimall.product.entity.SkuInfoEntity;
import com.xjz.gulimall.product.service.SkuInfoService;
import org.springframework.stereotype.Service;
import utils.PageUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Consumer;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper=new QueryWrapper<>();
        String key=(String)params.get("key");
        if(!StringUtils.isEmpty(key))
        {
            queryWrapper.and(skuInfoEntityQueryWrapper -> skuInfoEntityQueryWrapper.eq("sku_id",key).or().like("sku_name",key));
        }
        String categoryId=(String) params.get("catelogId");
        if(!StringUtils.isEmpty(categoryId)&&!"0".equalsIgnoreCase(categoryId))
        {
            queryWrapper.eq("catalog_id",categoryId);
        }
        String brandId=(String)params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(brandId))
        {
            queryWrapper.eq("brand_id",brandId);
        }
        //价格区间的检索
        String min=(String)params.get("min");
        if(!StringUtils.isEmpty(min)&&!"0".equalsIgnoreCase(min))
        {
            queryWrapper.ge("price",min);
        }
        String max=(String)params.get("max");
        if(!StringUtils.isEmpty(max)&&!"0".equalsIgnoreCase(max))
        {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                // 只有当 max > 0 时才拼接条件，避免前端传 0 导致查不到数据
                if (bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                    // le: less than or equal to (<=)
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    @Override
    public SkuInfoEntity getSkuInfo(Long skuId) {
        return this.getById(skuId);
    }

}
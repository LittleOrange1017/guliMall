package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.config.MyThreadConfig;
import com.xjz.gulimall.product.entity.*;
import com.xjz.gulimall.product.service.*;
import com.xjz.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import utils.Query;
import com.xjz.gulimall.product.dao.SkuInfoDao;
import org.springframework.stereotype.Service;
import utils.PageUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    private SkuImagesService service;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    @Lazy
    private SpuInfoService spuInfoService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private ThreadPoolExecutor executor;
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

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        QueryWrapper<SkuInfoEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);
        return list(queryWrapper);
    }

    @Override
    public SkuItemVo getItem(Long skuId) {
        SkuItemVo skuItemVo=new SkuItemVo();
        //sku基本信息
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfo = this.getSkuInfo(skuId);
            skuItemVo.setInfo(skuInfo);
            return skuInfo;
        }, executor);
        //sku图片信息
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> imagesEntityList = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            skuItemVo.setImages(imagesEntityList);
        }, executor);
        //spu销售属性组合
        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(info -> {
            if (info != null) {
                List<SkuItemVo.SaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrs(info.getSpuId());
                skuItemVo.setSaleAttrs(saleAttrs);
            }
        }, executor);
        //spu介绍
        CompletableFuture<Void> spuDescFuture = infoFuture.thenAcceptAsync(info -> {
            Long spuId = info.getSpuId();
            SpuInfoDescEntity descEntity = spuInfoDescService.getOne(new QueryWrapper<SpuInfoDescEntity>().eq("spu_id", spuId));
            skuItemVo.setDesc(descEntity);
        }, executor);
        //spu的规格参数信息
        CompletableFuture<Void> attrFuture = infoFuture.thenAcceptAsync(info -> {
            Long catalogId = info.getCatalogId();
            List<SkuItemVo.AttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(info.getSpuId(), catalogId);
            skuItemVo.setAttrgroupWithattrVos(attrGroupVos);
        }, executor);
        CompletableFuture.allOf(
                saleAttrFuture,
                spuDescFuture,
                attrFuture,
                imageFuture
        ).join();
        return skuItemVo;
    }

    @Override
    public Map<Long, BigDecimal> getSkuWeightBySkuIds(List<Long> skuIds) {
        if(skuIds==null||skuIds.isEmpty())
        {
            return Collections.emptyMap();
        }
        List<SkuInfoEntity> skuInfoEntities = this.listByIds(skuIds);
        List<Long> spuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSpuId).distinct().collect(Collectors.toList());
        List<SpuInfoEntity> spuInfoEntities = spuInfoService.listByIds(spuIds);
        Map<Long, BigDecimal> spuWeightMap = spuInfoEntities.stream().collect((Collectors.toMap(SpuInfoEntity::getId, SpuInfoEntity::getWeight)));
        Map<Long, BigDecimal> skuWeightMap = new HashMap<>();
        for(SkuInfoEntity sku:skuInfoEntities)
        {
            skuWeightMap.put(sku.getSkuId(),spuWeightMap.getOrDefault(sku.getSpuId(),BigDecimal.ZERO));
        }
        return skuWeightMap;
    }
}
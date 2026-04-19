package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.dto.Attr;
import com.xjz.gulimall.product.dto.SpuSaveDto;
import com.xjz.gulimall.product.entity.*;
import com.xjz.gulimall.product.feign.CouponFeginClient;
import com.xjz.gulimall.product.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import to.SkuReductionTo;
import to.SpuBoundTo;
import utils.Query;
import com.xjz.gulimall.product.dao.SpuInfoDao;
import org.springframework.stereotype.Service;
import utils.PageUtils;
import utils.R;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("spuInfoService")
@Transactional
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private CouponFeginClient couponFeginClient;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSpu(SpuSaveDto dto) {
        //1.保存spu信息
        SpuInfoEntity spuInfoEntity=new SpuInfoEntity();
        BeanUtils.copyProperties(dto,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.save(spuInfoEntity);
        //2.保存spu的描述图集
        List<String> decript = dto.getDecript();
        SpuInfoDescEntity spuInfoDescEntity=new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        //我们将图片数组转为逗号分割的字符串
        spuInfoDescEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
        //3.保存spu的图片集
        List<String> images = dto.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);
        //4.保存spu的规格参数
        List<SpuSaveDto.BaseAttrs> baseAttrs = dto.getBaseAttrs();
        productAttrValueService.saveProductAttrValue(spuInfoEntity.getId(),baseAttrs);
        //5.保存spu的积分信息
        SpuBoundTo spuBoundTo=new SpuBoundTo();
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        spuBoundTo.setBuyBounds(dto.getBounds().buyBounds);
        spuBoundTo.setGrowBounds(dto.getBounds().growBounds);
        R r = couponFeginClient.saveSpuBounds(spuBoundTo);
        if(!r.get("code").equals(0))
        {
            throw new RuntimeException("保存积分信息失败");
        }
        //6.保存SKU
        List<SpuSaveDto.Skus> skus = dto.getSkus();
        if(skus!=null&&skus.size()>0)
        {
            skus.forEach(skus1 -> saveSkuInfo(skus1, spuInfoEntity));
        }
    }

    @Override
    public void saveSkuInfo(SpuSaveDto.Skus skus, SpuInfoEntity spuInfoEntity) {
        //1.1 保存SKU基本信息
        SkuInfoEntity skuInfoEntity=new SkuInfoEntity();
        BeanUtils.copyProperties(skus,skuInfoEntity);
        skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
        skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
        skuInfoEntity.setSpuId(spuInfoEntity.getId());
        String desc=new String();
        for (String s : skus.getDescar()) {
            desc.join(",",s);
        }
        skuInfoEntity.setSkuDesc(desc);
        //1.2.保存SKU的默认图片信息
        String defaultImg="";
        List<SpuSaveDto.Images> images = skus.getImages();
        for (SpuSaveDto.Images image : images) {
            if(image.getDefaultImg()==1)
            {
                defaultImg=image.getImgUrl();
            }
        }
        skuInfoEntity.setSkuDefaultImg(defaultImg);
        skuInfoService.saveSkuInfo(skuInfoEntity);
        //1.3.拿到生成的自增ID
        Long skuId = skuInfoEntity.getSkuId();
        //2 保存SKU的图片信息
        List<SkuImagesEntity> imagesEntities = skus.getImages().stream()
                .filter(img -> StringUtils.hasLength(img.getImgUrl())) // 过滤空图
                .map(img -> {
                    SkuImagesEntity entity = new SkuImagesEntity();
                    entity.setSkuId(skuId);
                    entity.setImgUrl(img.getImgUrl());
                    entity.setDefaultImg(img.getDefaultImg());
                    return entity;
                }).collect(Collectors.toList());
        skuImagesService.saveImages(imagesEntities);
        //3 保存SKU的销售属性
        List<Attr> attr = skus.getAttr();
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(new Function<Attr, SkuSaleAttrValueEntity>() {
            @Override
            public SkuSaleAttrValueEntity apply(Attr attr) {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuId);
                return skuSaleAttrValueEntity;
            }
        }).collect(Collectors.toList());
        skuSaleAttrValueService.saveSkuSaleAttrValue(skuSaleAttrValueEntities);
        //4 远程调用 保存SKU的优惠信息
        SkuReductionTo skuReductionTo=new SkuReductionTo();
        BeanUtils.copyProperties(skus,skuReductionTo);
        skuReductionTo.setSkuId(skuId);
        if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
            R r = couponFeginClient.saveSkuReduction(skuReductionTo,skus.getPrice());
            if (!r.get("code").equals(0)) {
                throw new RuntimeException("保存优惠信息失败");
            }
        }
    }
}
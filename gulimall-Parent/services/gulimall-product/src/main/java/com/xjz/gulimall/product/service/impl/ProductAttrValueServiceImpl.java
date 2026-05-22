package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.dto.SpuSaveDto;
import com.xjz.gulimall.product.entity.AttrEntity;
import com.xjz.gulimall.product.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import utils.Query;
import com.xjz.gulimall.product.dao.ProductAttrValueDao;
import com.xjz.gulimall.product.entity.ProductAttrValueEntity;
import com.xjz.gulimall.product.service.ProductAttrValueService;
import org.springframework.stereotype.Service;
import utils.PageUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {
    @Autowired
    @Lazy
    private AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveProductAttrValue(Long id, List<SpuSaveDto.BaseAttrs> baseAttrs) {
        List<ProductAttrValueEntity> attrValueEntities = baseAttrs.stream().map(new Function<SpuSaveDto.BaseAttrs, ProductAttrValueEntity>() {
            @Override
            public ProductAttrValueEntity apply(SpuSaveDto.BaseAttrs baseAttrs) {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setSpuId(id);
                productAttrValueEntity.setAttrId(baseAttrs.getAttrId());
                AttrEntity attr = attrService.getById(baseAttrs.getAttrId());
                productAttrValueEntity.setAttrName(attr.getAttrName());
                productAttrValueEntity.setAttrValue(baseAttrs.getAttrValues());
                productAttrValueEntity.setQuickShow(baseAttrs.getShowDesc());
                return productAttrValueEntity;
            }
        }).collect(Collectors.toList());
        this.saveBatch(attrValueEntities);
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrlistforspu(Long spuId) {
        QueryWrapper<ProductAttrValueEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);
        return this.list(queryWrapper);
    }

}
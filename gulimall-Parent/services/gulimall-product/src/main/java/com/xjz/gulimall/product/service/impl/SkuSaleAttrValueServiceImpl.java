package com.xjz.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xjz.gulimall.product.entity.SkuInfoEntity;
import com.xjz.gulimall.product.service.SkuInfoService;
import com.xjz.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import utils.Query;
import com.xjz.gulimall.product.dao.SkuSaleAttrValueDao;
import com.xjz.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.xjz.gulimall.product.service.SkuSaleAttrValueService;
import org.springframework.stereotype.Service;
import utils.PageUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {
    @Autowired
    @Lazy
    private SkuInfoService skuInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuSaleAttrValue(List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities) {
        this.saveBatch(skuSaleAttrValueEntities);
    }

    @Override
    public List<SkuItemVo.SaleAttrVo> getSaleAttrs(Long spuId) {
        List<SkuInfoEntity> skus = skuInfoService.getSkuBySpuId(spuId);
        List<Long> skuIds = skus.stream().map(skuInfoEntity -> skuInfoEntity.getSkuId()).collect(Collectors.toList());
        List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = this.list(new QueryWrapper<SkuSaleAttrValueEntity>().in("sku_id", skuIds));
        Map<Long, List<SkuSaleAttrValueEntity>> attrIdMap = skuSaleAttrValueEntities.stream().collect(Collectors.groupingBy(SkuSaleAttrValueEntity::getAttrId));
        return attrIdMap.entrySet().stream().map(new Function<Map.Entry<Long, List<SkuSaleAttrValueEntity>>, SkuItemVo.SaleAttrVo>() {
            @Override
            public SkuItemVo.SaleAttrVo apply(Map.Entry<Long, List<SkuSaleAttrValueEntity>> longListEntry) {
                SkuItemVo.SaleAttrVo saleAttrVo = new SkuItemVo.SaleAttrVo();
                saleAttrVo.setAttrId(longListEntry.getKey());
                List<SkuSaleAttrValueEntity> values = longListEntry.getValue();
                saleAttrVo.setAttrName(values.get(0).getAttrName());
                Map<String, List<SkuSaleAttrValueEntity>> map = values.stream().collect(Collectors.groupingBy(SkuSaleAttrValueEntity::getAttrValue));
                List<SkuItemVo.AttrValueWithSkuIdVo> attrValueWithSkuIdVos = map.entrySet().stream().map(new Function<Map.Entry<String, List<SkuSaleAttrValueEntity>>, SkuItemVo.AttrValueWithSkuIdVo>() {
                    @Override
                    public SkuItemVo.AttrValueWithSkuIdVo apply(Map.Entry<String, List<SkuSaleAttrValueEntity>> stringListEntry) {
                        SkuItemVo.AttrValueWithSkuIdVo attrValueWithSkuIdVo = new SkuItemVo.AttrValueWithSkuIdVo();
                        attrValueWithSkuIdVo.setAttrValue(stringListEntry.getKey());
                        String skuIds = stringListEntry.getValue().stream().map(new Function<SkuSaleAttrValueEntity, String>() {
                            @Override
                            public String apply(SkuSaleAttrValueEntity skuSaleAttrValueEntity) {
                                return skuSaleAttrValueEntity.getSkuId().toString();
                            }
                        }).collect(Collectors.joining(","));
                        attrValueWithSkuIdVo.setSkuIds(skuIds);
                        return attrValueWithSkuIdVo;
                    }
                }).collect(Collectors.toList());
                saleAttrVo.setAttrValues(attrValueWithSkuIdVos);
                return saleAttrVo;
            }
        }).collect(Collectors.toList());
    }

}
package com.xjz.gulimall.product.vo;

import com.xjz.gulimall.product.entity.SkuImagesEntity;
import com.xjz.gulimall.product.entity.SkuInfoEntity;
import com.xjz.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import vo.SkuHasStockVo;

import java.util.List;
import java.util.Map;

@Data
public class SkuItemVo{
    //sku基本信息获取
    private SkuInfoEntity info;
    //sku的图片信息获取
    private List<SkuImagesEntity> images;
    //Spu的销售属性组合
    private List<SaleAttrVo> saleAttrs;
    //spu的介绍
    private SpuInfoDescEntity desc;
    //sku对应是否有库存
    // Key 是 skuId，Value 是是否有货（true/false）
    private boolean hasStock=true;
    //spu的规格参数信息
    private List<AttrGroupVo> attrgroupWithattrVos;
    //private SeckillSkuVo seckillSkuVo;
    @Data
    public static class SaleAttrVo{
        private String attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }
    @Data
    public static class AttrValueWithSkuIdVo {
        private String attrValue; // 属性值（如 "256G"）
        private String skuIds;    // 关联的 SKU 列表（如 "102,103"），用于前端计算跳转目标
    }
    @Data
    public static class AttrGroupVo{
        private String attrGroupName;
        private List<BaseAttrVo> attrVos;
    }
    @Data
    public static class BaseAttrVo{
        private String attrName;
        private String attrValue;
    }
}

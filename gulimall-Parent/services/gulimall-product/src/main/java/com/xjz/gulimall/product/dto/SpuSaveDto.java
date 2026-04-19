package com.xjz.gulimall.product.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: SpuSaveDto
 * Package:com.xjz.gulimall.product.dto
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/19 9:26
 * @Version 1.0
 */
@Data
public class SpuSaveDto {
    public String spuName ;
    public String spuDescription ;
    public Long catalogId ;
    public Long brandId ;
    public BigDecimal weight ;
    public int publishStatus ;
    public List<String> decript ;
    public List<String> images ;
    public Bounds bounds ;
    public List<BaseAttrs> baseAttrs ;
    public List<Skus> skus;
    @Data
    public static class Skus{
        public List<Attr> attr;
        public String skuName;
        public BigDecimal price;
        public String skuTitle;
        public String skuSubtitle;
        public List<Images> images;
        public List<String> descar;
        public int fullCount;
        public BigDecimal discount;
        public int countStatus;
        public BigDecimal fullPrice;
        public BigDecimal reducePrice;
        public int priceStatus;
        public List<MemberPrice> memberPrice;
    }
    @Data
    public static class MemberPrice {
        public int id;
        public String name;
        public BigDecimal price;
    }
    @Data
    public static class Images {
        public String imgUrl;
        public int defaultImg;
    }
    @Data
    public static class Bounds {
        public BigDecimal buyBounds;
        public BigDecimal growBounds;
    }
    @Data
    public static class BaseAttrs {
        public Long attrId;
        public String attrValues;
        public int showDesc;
    }
}

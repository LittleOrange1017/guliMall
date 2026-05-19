package com.xjz.gulimall.search.vo;

import lombok.Data;
import to.SkuEsModel;

import java.util.Calendar;
import java.util.List;

@Data
public class SearchResult {
    private List<SkuEsModel> product;
    private Integer pageNum=1;//当前页码
    private Long total;//总记录数
    private Integer totalPages;//总页码
    private List<BrandVO> brands;//当前所查询到的结果所涉及到的所有品牌
    private List<AttrVO> attrs;//当前所查询到的结果所涉及到的所有属性
    private List<CatalogVO> catalogs;//当前所查询到的结果所涉及到的所有分类
    private List<NavVo> navs;
    @Data
    public static class BrandVO{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    @Data
    public static class CatalogVO{
        private Long catalogId;
        private String catalogName;
    }
    @Data
    public static class AttrVO{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }
}

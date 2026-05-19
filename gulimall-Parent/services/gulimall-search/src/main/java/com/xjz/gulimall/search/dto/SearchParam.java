package com.xjz.gulimall.search.dto;

import lombok.Data;
import to.SkuEsModel;

import java.util.List;

@Data
public class SearchParam {
    /**
     * 全文匹配关键字
     */
    private String keyword;
    private Long catalog3Id;
    /**
     * sort=saleCount_asc/desc
     * sort=hotScore_asc/desc
     * sort=skuPrice_asc/desc
     */
    private String sort;
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNum;//页码
    private String  oldQueryString;
}

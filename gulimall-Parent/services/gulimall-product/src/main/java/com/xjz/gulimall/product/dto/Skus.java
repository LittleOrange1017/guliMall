package com.xjz.gulimall.product.dto;

import lombok.Data;

import java.util.List;

/**
 * ClassName: Skus
 * Package:com.xjz.gulimall.product.dto
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/19 9:29
 * @Version 1.0
 */
@Data
public class Skus {
    public List<Attr> attr;
    public String skuName;
    public String price;
    public String skuTitle;
    public String skuSubtitle;
    public List<Images> images;
    public List<String> descar;
    public int fullCount;
    public int discount;
    public int countStatus;
    public int fullPrice;
    public int reducePrice;
    public int priceStatus;
    public List<MemberPrice> memberPrice;
}

package com.xjz.gulimall.product.dto;

import lombok.Data;

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
    public int catalogId ;
    public int brandId ;
    public float weight ;
    public int publishStatus ;
    public List<String> decript ;
    public List<String> images ;
    public Bounds bounds ;
    public List<BaseAttrs> baseAttrs ;
    public List<Skus> skus;
}

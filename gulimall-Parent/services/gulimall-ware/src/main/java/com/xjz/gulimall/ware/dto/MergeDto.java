package com.xjz.gulimall.ware.dto;

import lombok.Data;

import java.util.List;

/**
 * ClassName: MergeDto
 * Package:com.xjz.gulimall.ware.dto
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/22 13:57
 * @Version 1.0
 */
@Data
public class MergeDto {
    /**
     * 采购单id(如果是null，说明前端要求新建一个采购单)
     */
    private Long purchaseId;
    /**
     * 采购项id
     */
    private List<Long> items;
}

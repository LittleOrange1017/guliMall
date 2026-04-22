package com.xjz.gulimall.ware.dto;

import lombok.Data;

import java.util.List;

/**
 * ClassName: PurchaseDoneDto
 * Package:com.xjz.gulimall.ware.dto
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/22 15:44
 * @Version 1.0
 */
@Data
public class PurchaseDoneDto {
    private Long id;
    private List<PurchaseItemDoneDto> items;
}

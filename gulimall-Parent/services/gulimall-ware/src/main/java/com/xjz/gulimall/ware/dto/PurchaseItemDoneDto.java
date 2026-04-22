package com.xjz.gulimall.ware.dto;

import lombok.Data;

/**
 * ClassName: PurchaseItemDoneDto
 * Package:com.xjz.gulimall.ware.dto
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/22 15:45
 * @Version 1.0
 */
@Data
public class PurchaseItemDoneDto {
    private Long itemId;
    private Integer status;
    private String reason;
}

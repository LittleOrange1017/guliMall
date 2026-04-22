package com.xjz.gulimall.ware.constant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ClassName: WareConstant
 * Package:com.xjz.gulimall.ware.constant
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/22 13:20
 * @Version 1.0
 */

public class WareConstant {
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public enum PurchaseStatusEnum {
        CREATED(0, "新建"),
        ASSIGNED(1, "已分配"),
        RECEIVE(2, "已领取"),
        FINISH(3, "已完成"),
        HASERROR(4, "有异常");
        private Integer code;
        private String msg;
    }
}

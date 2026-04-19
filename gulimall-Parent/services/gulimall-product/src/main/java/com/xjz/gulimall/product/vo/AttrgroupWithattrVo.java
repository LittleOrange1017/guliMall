package com.xjz.gulimall.product.vo;

import com.xjz.gulimall.product.entity.AttrEntity;
import com.xjz.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * ClassName: AttrgroupWithattrVo
 * Package:com.xjz.gulimall.product.vo
 * Description:
 *
 * @Author 小橘子神灯
 * @Create 2026/4/16 11:23
 * @Version 1.0
 */
@Data
public class AttrgroupWithattrVo extends AttrGroupEntity {
    private List<AttrEntity> attrs;
}

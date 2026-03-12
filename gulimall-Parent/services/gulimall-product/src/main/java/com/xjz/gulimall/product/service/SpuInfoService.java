package com.xjz.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xjz.gulimall.product.entity.SpuInfoEntity;
import utils.PageUtils;

import java.util.Map;

/**
 * spu信息
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:24
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


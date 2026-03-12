package com.xjz.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import utils.PageUtils;
import com.xjz.gulimall.ware.entity.ShAreaEntity;

import java.util.Map;

/**
 * 全国省市区信息
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:16:24
 */
public interface ShAreaService extends IService<ShAreaEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


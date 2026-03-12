package com.xjz.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import utils.PageUtils;
import com.xjz.gulimall.member.entity.MemberCollectSpuEntity;

import java.util.Map;

/**
 * 会员收藏的商品
 *
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 10:59:14
 */
public interface MemberCollectSpuService extends IService<MemberCollectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


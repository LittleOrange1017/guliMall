package com.xjz.gulimall.order.dao;

import com.xjz.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:09:40
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}

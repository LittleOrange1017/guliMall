package com.xjz.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 运费模板
 * 
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-12 11:16:23
 */
@Data
@TableName("wms_feight_template")
public class FeightTemplateEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * name
	 */
	private String name;
	/**
	 * 计费类型【0->按重量，1->按件数】
	 */
	private Integer chargeType;
	/**
	 * 首重
	 */
	private BigDecimal firstWeight;
	/**
	 * 首费
	 */
	private BigDecimal firstFee;
	/**
	 * 续重
	 */
	private BigDecimal continueWeight;
	/**
	 * 续费
	 */
	private BigDecimal continueFee;
	/**
	 * 目的地
	 */
	private Long dest;

}

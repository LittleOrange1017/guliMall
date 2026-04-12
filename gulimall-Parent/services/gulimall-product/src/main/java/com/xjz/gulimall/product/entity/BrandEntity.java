package com.xjz.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 * 
 * @author xjz
 * @email lo_17@163.com
 * @date 2026-03-11 14:45:25
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@Null(message = "品牌id可以为空", groups = ValidationGroups.save.class)
	@NotNull(message = "品牌id必须不为空",groups = ValidationGroups.Update.class)
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空",groups = {ValidationGroups.Update.class,ValidationGroups.save.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(message = "品牌logo地址不能为空",groups = ValidationGroups.save.class)
	@URL(message = "logo必须是个合法的url地址",groups = {ValidationGroups.Update.class,ValidationGroups.save.class})
	private String logo;
	/**
	 * 介绍
	 */
	@NotBlank(message = "介绍不能为空",groups = ValidationGroups.save.class)
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@TableLogic(value = "1",delval = "0")
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotBlank(message = "首字母必须填写",groups = ValidationGroups.save.class)
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是a-z或者A-Z之间的一个字母",groups = {ValidationGroups.Update.class,ValidationGroups.save.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序字段不能为空",groups = ValidationGroups.save.class)
	@Min(value = 0,message = "排序字段必须大于0",groups = {ValidationGroups.Update.class,ValidationGroups.save.class})
	private Integer sort;

}

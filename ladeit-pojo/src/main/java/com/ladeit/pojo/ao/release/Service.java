package com.ladeit.pojo.ao.release;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * @program: ladeit
 * @description: Service
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class Service {
	/**
	 * 主键 primary key
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String id;

	/**
	 * 名称
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String name;

	/**
	 * 冗余
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String serviceGroudId;

	/**
	 * env_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String envId;

	/**
	 * match json
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String match;

	/**
	 * 匹配ip或者dns
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String mapping;

	/**
	 * 状态 0 正常运行 1 金丝雀发布中 2 蓝绿发布中 3 abtest发布中 4 滚动发布中
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String status;

	/**
	 * 创建时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createAt;

	/**
	 * 创建人
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String createBy;

	/**
	 * modify_at
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date modifyAt;

	/**
	 * modify_by
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String modifyBy;

	/**
	 * isdel
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isdel;
}

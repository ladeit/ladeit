package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * @program: ladeit
 * @description: ServiceGroup
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class ServiceGroupAO {
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
	 * env_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String envId;

	/**
	 * cluster_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String clusterId;

	/**
	 * gateway
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String gateway;

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

	/**
	 * isdel_service
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isdelService;
}

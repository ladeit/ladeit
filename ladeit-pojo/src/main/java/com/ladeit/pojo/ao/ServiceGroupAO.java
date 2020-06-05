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
	private String id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * env_id
	 */
	private String envId;

	/**
	 * cluster_id
	 */
	private String clusterId;

	/**
	 * gateway
	 */
	private String gateway;

	/**
	 * 创建时间
	 */
	private Date createAt;

	/**
	 * 创建人
	 */
	private String createBy;

	/**
	 * modify_at
	 */
	private Date modifyAt;

	/**
	 * modify_by
	 */
	private String modifyBy;

	/**
	 * isdel
	 */
	private Boolean isdel;

	/**
	 * isdel_service
	 */
	private Boolean isdelService;
}

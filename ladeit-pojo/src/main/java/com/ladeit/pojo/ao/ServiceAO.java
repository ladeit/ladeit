package com.ladeit.pojo.ao;

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
public class ServiceAO {
	/**
	 * 主键 primary key
	 */
	private String id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 冗余
	 */
	private String serviceGroupId;

	private String serviceGroupName;

	private String clusterId;

	/**
	 * env_id
	 */
	private String envId;

	/**
	 * image_version
	 */
	private String imageVersion;

	/**
	 * match json
	 */
	private String match;

	/**
	 * 匹配ip或者dns
	 */
	private String mapping;

	/**
	 * 状态 0 正常运行 1 金丝雀发布中 2 蓝绿发布中 3 abtest发布中 4 滚动发布中
	 */
	private String status;

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

	private Boolean isDelK8s;

	private String token;

	private Date releaseAt;

	private String serviceType;
}

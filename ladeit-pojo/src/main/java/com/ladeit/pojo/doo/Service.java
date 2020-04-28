package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: Service
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
@Entity
@Table(name = "service")
public class Service {
	/**
	 * 主键 primary key
	 */
	@Id
	private String id;

	/**
	 * 名称
	 */
	@Column(name = "name")
	private String name;

	/**
	 * 冗余
	 */
	@Column(name = "service_group_id")
	private String serviceGroupId;

	/**
	 * cluster_id
	 */
	@Column(name = "cluster_id")
	private String clusterId;

	/**
	 * env_id
	 */
	@Column(name = "env_id")
	private String envId;

	/**
	 * image_version
	 */
	@Column(name = "image_version")
	private String imageVersion;

	/**
	 * image_id
	 */
	@Column(name = "image_id")
	private String imageId;

	/**
	 * match json
	 */
	@Column(name = "`match`")
	private String match;

	/**
	 * 匹配ip或者dns
	 */
	@Column(name = "mapping")
	private String mapping;

	/**
	 * 状态 -1尚未运行 0 正常运行 1 金丝雀发布中 2 蓝绿发布中 3 abtest发布中 4 滚动发布中
	 */
	@Column(name = "status")
	private String status;
	public static final String SERVICE_STATUS_INIT = "-1";
	public static final String SERVICE_STATUS_0 = "0";
	public static final String SERVICE_STATUS_1 = "1";
	public static final String SERVICE_STATUS_2 = "2";
	public static final String SERVICE_STATUS_3 = "3";
	public static final String SERVICE_STATUS_4 = "4";

	/**
	 * 创建时间
	 */
	@Column(name = "create_at")
	private Date createAt;

	/**
	 * 创建人
	 */
	@Column(name = "create_by")
	private String createBy;

	/**
	 * 创建人ID
	 */
	@Column(name = "create_byid")
	private String createById;

	/**
	 * modify_at
	 */
	@Column(name = "modify_at")
	private Date modifyAt;

	/**
	 * modify_by
	 */
	@Column(name = "modify_by")
	private String modifyBy;

	/**
	 * isdel
	 */
	@Column(name = "isdel")
	private Boolean isdel;

	/**
	 * token
	 */
	@Column(name = "token")
	private String token;

	/**
	 * 首次发布时间
	 */
	@Column(name = "release_at")
	private Date releaseAt;

	/**
	 * service_type
	 */
	@Column(name = "service_type")
	private String serviceType;

	@Transient
	private List<String> Ids;

}

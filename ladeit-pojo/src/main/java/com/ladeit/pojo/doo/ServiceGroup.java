package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: ServiceGroup
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
@Table(name = "service_group")
@Entity
public class ServiceGroup {
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
	 * env_id
	 */
	@Column(name = "env_id")
	private String envId;

	/**
	 * cluster_id
	 */
	@Column(name = "cluster_id")
	private String clusterId;

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
	 * Invite_code
	 */
	@Column(name = "invite_code")
	private String inviteCode;

	/**
	 * isdel_service
	 */
	@Column(name = "isdel_service")
	private Boolean isdelService;
}

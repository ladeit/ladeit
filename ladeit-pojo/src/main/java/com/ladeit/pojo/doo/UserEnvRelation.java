package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @program: ladeit
 * @description: UserEnvRelation
 * @author: liuzp
 * @create: 2020/02/01
 * @version: 1.0.0
 */
@Data
@Table(name = "user_env_relation")
@Entity
public class UserEnvRelation {
	/**
	 * id
	 */
	@Id
	private String id;

	/**
	 * user_id
	 */
	@Column(name = "user_id")
	private String userId;

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
	 * admin/regular
	 */
	@Column(name = "access_level")
	private String accessLevel;

	/**
	 * 成员创建时间
	 */
	@Column(name = "create_at")
	private Date createAt;

}

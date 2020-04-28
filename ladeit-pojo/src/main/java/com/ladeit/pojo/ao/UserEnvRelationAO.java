package com.ladeit.pojo.ao;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @program: ladeit
 * @description: UserEnvRelationAO
 * @author: liuzp
 * @create: 2020/02/01
 * @version: 1.0.0
 */
@Data
public class UserEnvRelationAO {
	/**
	 * id
	 */
	private String id;

	/**
	 * user_id
	 */
	private String userId;


	/**
	 * cluster_id
	 */
	private String clusterId;

	/**
	 * env_id
	 */
	private String envId;

	/**
	 * namespace
	 */
	private String namespace;

	/**
	 * admin/regular
	 */
	private String accessLevel;

	/**
	 * 成员创建时间
	 */
	private Date createAt;

}

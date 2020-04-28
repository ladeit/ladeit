package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @program: ladeit
 * @description: UserServiceRelation
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
@Table(name = "user_service_relation")
@Entity
public class UserServiceRelation {
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
	 * service_id
	 */
	@Column(name = "service_id")
	private String serviceId;

	/**
	 * admin/regular  备用字段
	 */
	@Column(name = "access_level")
	private String accessLevel;

	/**
	 * 10/20/30/40/50
	 */
	@Column(name = "role_num")
	private String roleNum;
}

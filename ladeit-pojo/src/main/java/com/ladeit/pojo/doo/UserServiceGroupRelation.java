package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @program: ladeit
 * @description: UserServiceGroupRelation
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
@Table(name = "user_service_group_relation")
@Entity
public class UserServiceGroupRelation {
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
	 * service_group_id
	 */
	@Column(name = "service_group_id")
	private String serviceGroupId;

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

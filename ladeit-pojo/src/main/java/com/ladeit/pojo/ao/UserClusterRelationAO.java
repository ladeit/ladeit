package com.ladeit.pojo.ao;

import io.kubernetes.client.proto.V1;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: UserClusterRelationAO
 * @author: liuzp
 * @create: 2020/02/01
 * @version: 1.0.0
 */
@Data
public class UserClusterRelationAO {
	/**
	 * id
	 */
	private String id;

	/**
	 * user_id
	 */
	private String userId;

	/**
	 * user_name
	 */
	private String userName;

	/**
	 * cluster_id
	 */
	private String clusterId;

	/**
	 * admin/regular
	 */
	private String accessLevel;

	/**
	 * 成员创建时间
	 */
	private Date createAt;

	private List<UserEnvRelationAO> envuser;

}

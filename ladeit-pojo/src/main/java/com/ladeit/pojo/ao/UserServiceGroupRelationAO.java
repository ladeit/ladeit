package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @program: ladeit
 * @description: UserServiceGroupRelation
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class UserServiceGroupRelationAO {
	/**
	 * id
	 */
	private String id;

	/**
	 * user_id
	 */
	private String userId;

	/**
	 * service_group_id
	 */
	private String serviceGroupId;

	/**
	 * admin/regular
	 */
	private String accessLevel;

}

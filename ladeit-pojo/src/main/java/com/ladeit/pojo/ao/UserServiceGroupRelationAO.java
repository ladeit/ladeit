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
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String id;

	/**
	 * user_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String userId;

	/**
	 * service_group_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String serviceGroupId;

	/**
	 * admin/regular
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String accessLevel;

}

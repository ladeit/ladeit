package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @program: ladeit
 * @description: UserServiceRelation
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class UserServiceRelationAO {
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
	 * service_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String serviceId;

	/**
	 * admin/regular  备用字段
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String accessLevel;

	/**
	 * 10/20/30/40/50
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String roleNum;
}

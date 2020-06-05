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
	private String id;

	/**
	 * user_id
	 */
	private String userId;

	/**
	 * service_id
	 */
	private String serviceId;

	/**
	 * admin/regular  备用字段
	 */
	private String accessLevel;

	/**
	 * 10/20/30/40/50
	 */
	private String roleNum;
}

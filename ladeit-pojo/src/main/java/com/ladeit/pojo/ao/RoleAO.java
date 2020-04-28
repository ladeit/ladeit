package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * @program: ladeit
 * @description: Role
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class RoleAO {
	/**
	 * 角色所对应的访问级别 primary key
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String id;

	/**
	 * 角色类型
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String roleType;

	/**
	 * role_num
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer roleNum;

	/**
	 * 角色创建时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createdAt;

	/**
	 * 角色描述信息
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String roleDesc;
}

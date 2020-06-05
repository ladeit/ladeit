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
	private String id;

	/**
	 * 角色类型
	 */
	private String roleType;

	/**
	 * role_num
	 */
	private Integer roleNum;

	/**
	 * 角色创建时间
	 */
	private Date createdAt;

	/**
	 * 角色描述信息
	 */
	private String roleDesc;
}

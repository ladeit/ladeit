package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * @program: ladeit
 * @description: User
 * @author: falcomlife
 * @create: 2019/10/30
 * @version: 1.0.0
 */
@Data
public class UserAO {

	/**
	 * 主键 primary key
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String id;

	/**
	 * 用户昵称
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String nickName;

	/**
	 * 姓
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String lastName;

	/**
	 * 用户名
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String username;

	/**
	 * 用户密码
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String password;

	/**
	 * 用户新密码
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String newPassword;

	/**
	 * 密码盐
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String salt;

	/**
	 * 成员创建时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createAt;

	/**
	 * 成员信息修改时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date updateAt;

	/**
	 * 邮箱地址
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String email;

	/**
	 * 用户是否删除 0：已删除 1：正常
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isdel;

	/**
	 * 用户是否已激活 0：未激活 1：已激活
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean status;

}

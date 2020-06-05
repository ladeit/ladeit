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
	private String id;

	/**
	 * 用户昵称
	 */
	private String nickName;

	/**
	 * 姓
	 */
	private String lastName;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 用户密码
	 */
	private String password;

	/**
	 * 用户新密码
	 */
	private String newPassword;

	/**
	 * 密码盐
	 */
	private String salt;

	/**
	 * 成员创建时间
	 */
	private Date createAt;

	/**
	 * 成员信息修改时间
	 */
	private Date updateAt;

	/**
	 * 邮箱地址
	 */
	private String email;

	/**
	 * 用户是否删除 0：已删除 1：正常
	 */
	private Boolean isdel;

	/**
	 * 用户是否已激活 0：未激活 1：已激活
	 */
	private Boolean status;

}

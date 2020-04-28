package com.ladeit.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 返回给前台的用户信息
 * @program: ladeit
 * @description: UserInfo
 * @author: falcomlife
 * @create: 2019/10/31
 * @version: 1.0.0
 */
@Data
public class UserInfo {
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
	 * 用户是否已激活 0：未激活 1：已激活
	 */
	private Boolean status;

	/**
	* token凭证
	*/
	private String token;

	/**
	* sessionid
	*/
	private String session;
}

package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @program: ladeit
 * @description: User
 * @author: falcomlife
 * @create: 2019/10/30
 * @version: 1.0.0
 */
@Data
@Entity
@Table(name="user")
public class User {

	/**
	 * 主键 primary key
	 */
	@Id
	private String id;

	/**
	 * 用户昵称
	 */
	@Column(name = "nick_name")
	private String nickName;

	/**
	 * 姓
	 */
	@Column(name = "last_name")
	private String lastName;

	/**
	 * 用户名
	 */
	@Column(name = "username")
	private String username;

	/**
	 * 用户密码
	 */
	@Column(name = "password")
	private String password;

	/**
	 * 密码盐
	 */
	@Column(name = "salt")
	private String salt;

	/**
	 * 成员创建时间
	 */
	@Column(name = "create_at")
	private Date createAt;

	/**
	 * 成员信息修改时间
	 */
	@Column(name = "update_at")
	private Date updateAt;

	/**
	 * 邮箱地址
	 */
	@Column(name = "email")
	private String email;

	/**
	 * 用户是否删除 0 否 1 是
	 */
	@Column(name = "isdel")
	private Boolean isdel;

	/**
	 * 用户是否已激活 0：未激活 1：已激活
	 */
	@Column(name = "status")
	private Boolean status;

	/**
	* 语言标记
	*/
	@Transient
	private String lan;
}

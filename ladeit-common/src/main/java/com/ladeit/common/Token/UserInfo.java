package com.ladeit.common.Token;


import lombok.Data;

import static org.gitlab4j.api.Constants.TokenType;


/**
 * @description: BzMemberBO
 * @author: falcomlife
 * @create: 2019/07/15
 * @version: 1.0.0
 */
@Data
public class UserInfo {

	/**
	 * 主键
	 */
	private String id;
	/**
	 * 三方的member_id
	 */
	private String tpMemberId;
	/**
	 * 昵称
	 */
	private String nickname;
	/**
	 * 三方昵称
	 */
	private String tpNickname;
	/**
	 * 角色类型
	 */
	private String roleType;
	/**
	 * 用户名称
	 */
	private String username;
	/**
	 * email
	 */
	private String email;
	/**
	 * 用户访问Token
	 */
	private String oauthToken;

	/**
	 * 三方平台的hosturl
	 */
	private String hosturl;

	/**
	 * token类型
	 */
	private TokenType tokenType;

	/**
	 * 命名空间
	 */
	private String namespace;

	/**
	 * 项目
	 */
	private String project;

	/**
	 * 项目
	 */
	private String slackUserId;

}

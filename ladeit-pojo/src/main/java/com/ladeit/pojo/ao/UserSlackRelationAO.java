package com.ladeit.pojo.ao;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @program: ladeit
 * @description: UserSlackRelation
 * @author: liuzp
 * @create: 2020/1/07
 * @version: 1.0.0
 */
@Data
public class UserSlackRelationAO {
	/**
	 * id
	 */
	private String id;

	/**
	 * user_id
	 */
	private String userId;

	/**
	 * slack_user_id
	 */
	private String slackUserId;

	/**
	 * user_name
	 */
	private String userName;

	/**
	 * slack_user_name
	 */
	private String slackUserName;


	private String channalId;
}

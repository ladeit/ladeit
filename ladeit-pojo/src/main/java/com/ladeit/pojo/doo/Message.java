package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

/**
 * @description: message
 * @author: MddandPyy
 * @create: 2020/3/14
 * @version: 1.0.0
 */
@Data
@Table(name = "message")
@Entity
public class Message {
	/**
	 * id
	 */
	@Id
	private String id;

	/**
	 * title
	 */
	@Column(name = "title")
	private String title;

	/**
	 * target_id
	 */
	@Column(name = "content")
	private String content;

	/**
	 * 创建时间
	 */
	@Column(name = "create_at")
	private Date createAt;

	/**
	 * type
	 */
	@Column(name = "type")
	private String type;

	/**
	 * target_id
	 */
	@Column(name = "target_id")
	private String targetId;

	/**
	 * level
	 */
	@Column(name = "level")
	private String level;

	/**
	 * service_id
	 */
	@Column(name = "service_id")
	private String serviceId;

	/**
	 * operuser_id
	 */
	@Column(name = "operuser_id")
	private String operuserId;

	/**
	 * service_group_id
	 */
	@Column(name = "service_group_id")
	private String serviceGroupId;

	/**
	 * message_type
	 */
	@Column(name = "message_type")
	private String messageType;

	/**
	 * 消息通知对象，或者排除通知的对象
	 */
	@Transient
	private String target_user;

	@Transient
	private Map<String,Object> params;

}

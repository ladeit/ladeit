package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @program: ladeit
 * @description: Operation
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
@Table(name = "operation")
@Entity
public class Operation {
	/**
	 * deploy_id
	 */
	@Column(name = "deploy_id")
	private String deployId;

	/**
	 * 操作对象的类型 service / candidate / release / image / service_group 等
	 */
	@Column(name = "target")
	private String target;
	public static final String TARGET_SERVICE = "service";
	public static final String TARGET_CANDIDATE = "candidate";
	public static final String TARGET_RELEASE = "release";
	public static final String TARGET_IMAGE = "image";
	public static final String TARGET_SERVICE_GROUP = "service_group";

	/**
	 * 操作对象的id
	 */
	@Column(name = "target_id")
	private String targetId;

	/**
	 * 操作内容，应详尽描述
	 */
	@Column(name = "event_log")
	private String eventLog;

	/**
	 * 1 创建节点 2 删除节点 3 修改路由权重 4 修改匹配规则 5 修改策略 6 调整pod数量 (数量太多，待完善)
	 */
	@Column(name = "event_type")
	private Integer eventType;
	public static final Integer EVENT_TYPE_CREATE_NODE = 1;
	public static final Integer EVENT_TYPE_DELETE_NODE = 2;
	public static final Integer EVENT_TYPE_WEIGHT = 3;
	public static final Integer EVENT_TYPE_MATCH = 4;
	public static final Integer EVENT_TYPE_POLICE = 5;
	public static final Integer EVENT_TYPE_RETRIES = 6;

	/**
	 * 创建时间
	 */
	@Column(name = "create_at")
	private Date createAt;

	/**
	 * 创建人
	 */
	@Column(name = "create_by")
	private String createBy;

	/**
	 * 创建人ID
	 */
	@Column(name = "create_byid")
	private String createById;
}

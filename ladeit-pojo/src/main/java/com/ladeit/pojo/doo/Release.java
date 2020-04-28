package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @program: ladeit
 * @description: Release
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
@Entity
@Table(name = "`release`")
public class Release {
	/**
	 * id
	 */
	@Id
	private String id;

	/**
	 * 冗余，一般情况下为候选人的版本号
	 */
	@Column(name = "name")
	private String name;

	/**
	 * service_id
	 */
	@Column(name = "image_id")
	private String imageId;

	/**
	 * service_id
	 */
	@Column(name = "service_id")
	private String serviceId;

	/**
	 * resource_type
	 */
	@Column(name = "resource_type")
	private String resource_type;

	/**
	 * uid
	 */
	@Column(name = "uid")
	private String uid;

	/**
	 * 发布开始时间
	 */
	@Column(name = "deploy_start_at")
	private Date deployStartAt;

	/**
	 * 发布完成时间
	 */
	@Column(name = "deploy_finish_at")
	private Date deployFinishAt;

	/**
	 * 服役开始时间
	 */
	@Column(name = "service_start_at")
	private Date serviceStartAt;

	/**
	 * 服役结束时间
	 */
	@Column(name = "service_finish_at")
	private Date serviceFinishAt;

	/**
	 * 0 准备中 1 服役中 2 退役 3 异常
	 */
	@Column(name = "status")
	private Integer status;

	/**
	 * 1 金丝雀发布 2 蓝绿发布 4 abtest发布 8 滚动发布 每添加一种发布方式，此字段加上相应的值
	 */
	@Column(name = "type")
	private Integer type;

	/**
	 * 服役时长 单位秒 退役时才更新此字段 service_ finish_at减 service_start_at
	 */
	@Column(name = "duration")
	private Long duration;

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

	/**
	 * 操作渠道ladeit,slack
	 */
	@Column(name = "oper_channel")
	private String operChannel;

	/**
	 * 是否使用默认配置
	 */
	@Transient
	private Boolean isDefault;

	/**
	 * 是否使用新的yaml配置
	 */
	@Transient
	private Boolean newYaml;
}

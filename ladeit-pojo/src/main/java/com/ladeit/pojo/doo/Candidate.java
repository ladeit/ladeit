package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @program: ladeit
 * @description: Candidate
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */

@Data
@Entity
@Table(name = "candidate")
public class Candidate {
	/**
	 * 主键 primary key
	 */
	@Id
	private String id;

	/**
	 * version
	 */
	@Column(name = "name")
	private String name;

	/**
	 * project_id
	 */
	@Column(name = "release_id")
	private String releaseId;

	/**
	 * project_id
	 */
	@Column(name = "service_id")
	private String serviceId;
	/**
	 * image_id
	 */
	@Column(name = "image_id")
	private String imageId;

	/**
	 * 1 金丝雀节点 2 蓝绿节点 3 abtest节点
	 */
	@Column(name = "type")
	private Integer type;

	/**
	 * 运行时长 单位秒 删除时才更新此字段
	 */
	@Column(name = "duration")
	private Integer duration;

	/**
	 * 预设pod总数
	 */
	@Column(name = "pod_count")
	private String podCount;

	/**
	 * 0 启用 1 暂停
	 */
	@Column(name = "status")
	private Integer status;

	/**
	 * match中的东西，包括method，param，uri，head等都在这里写
	 */
	@Column(name = "`match`")
	private String match;

	/**
	 * 权重
	 */
	@Column(name = "weight")
	private Integer weight;

	/**
	 * 转发uri
	 */
	@Column(name = "redirect")
	private String redirect;

	/**
	 * 重写uri
	 */
	@Column(name = "rewrite")
	private String rewrite;

	/**
	 * 超时
	 */
	@Column(name = "timeout")
	private Integer timeout;

	/**
	 * 重试
	 */
	@Column(name = "retries")
	private Integer retries;

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

	/**
	 * 是否删除 0 否 1 是
	 */
	@Column(name = "isdel")
	private Boolean isdel;

}
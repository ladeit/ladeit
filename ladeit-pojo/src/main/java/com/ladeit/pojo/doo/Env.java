package com.ladeit.pojo.doo;

import com.ladeit.pojo.dto.metric.pod.Occupy;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: Env
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
@Entity
@Table(name="env")
public class Env {

	/**
	 * 主键 primary key
	 */
	@Id
	private String id;

	/**
	 * 环境名称
	 */
	@Column(name = "env_name")
	private String envName;

	/**
	 * 集群id
	 */
	@Column(name = "cluster_id")
	private String clusterId;

	/**
	 * 命名空间
	 */
	@Column(name = "namespace")
	private String namespace;

	/**
	 * test/dev/staging/prod
	 */
	@Column(name = "env_tag")
	private String envTag;

	/**
	 * 是否需要人工审核(才能部署到此环境
	 */
	@Column(name = "audit")
	private Boolean audit;

	/**
	 * cpu限制
	 */
	@Column(name = "cpu_limit")
	private Integer cpuLimit;

	/**
	 * cpu限制单位
	 */
	@Column(name = "cpu_limit_unit")
	private String cpuLimitUnit;

	/**
	 * 内存限制
	 */
	@Column(name = "mem_limit")
	private Integer memLimit;

	/**
	 * 内存限制单位
	 */
	@Column(name = "mem_limit_unit")
	private String memLimitUnit;

	/**
	 * cpu限制
	 */
	@Column(name = "cpu_request")
	private Integer cpuRequest;

	/**
	 * cpu限制单位
	 */
	@Column(name = "cpu_request_unit")
	private String cpuRequestUnit;

	/**
	 * 内存限制
	 */
	@Column(name = "mem_request")
	private Integer memRequest;

	/**
	 * 内存限制
	 */
	@Column(name = "mem_request_unit")
	private String memRequestUnit;

	/**
	 * 是否启动资源配额
	 */
	@Column(name = "resource_quota")
	private Boolean resourceQuota;

	/**
	 * 流量限制
	 */
	@Column(name = "network_limit")
	private Integer networkLimit;

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
	 * modify_at
	 */
	@Column(name = "modify_at")
	private Date modifyAt;

	/**
	 * modify_by
	 */
	@Column(name = "modify_by")
	private String modifyBy;

	/**
	 * isdel
	 */
	@Column(name = "isdel")
	private Boolean isdel;

	@Column(name = "disable")
	private String disable;

	@Transient
	private List<Occupy> occupyCpuReq;

	@Transient
	private List<Occupy> occupyMemReq;

	@Transient
	private List<Occupy> occupyCpuLimit;

	@Transient
	private List<Occupy> occupyMemLimit;
}

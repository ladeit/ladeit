package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ladeit.pojo.dto.metric.pod.Occupy;
import lombok.Data;

import javax.persistence.Transient;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: Env
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class EnvAO {

	/**
	 * 主键 primary key
	 */
	private String id;

	/**
	 * 环境名称
	 */
	private String envName;

	/**
	 * 集群id
	 */
	private String clusterId;

	/**
	 * 命名空间
	 */
	private String namespace;

	/**
	 * test/dev/staging/prod
	 */
	private String envTag;

	/**
	 * 是否需要人工审核(才能部署到此环境
	 */
	private Boolean audit;

	/**
	 * cpu限制
	 */
	private Integer cpuLimit;

	/**
	 * cpu限制单位
	 */
	private String cpuLimitUnit;

	/**
	 * 内存限制
	 */
	private Integer memLimit;

	/**
	 * 内存限制单位
	 */
	private String memLimitUnit;

	/**
	 * cpu限制
	 */
	private Integer cpuRequest;

	/**
	 * cpu限制单位
	 */
	private String cpuRequestUnit;

	/**
	 * 内存限制
	 */
	private Integer memRequest;

	/**
	 * 内存限制单位
	 */
	private String memRequestUnit;

	/**
	 * 是否启动资源配额
	 */
	private Boolean resourceQuota;
	/**
	 * 流量限制
	 */
	private Integer networkLimit;

	/**
	 * 10-public 20-private
	 */
	private Boolean visibility;

	/**
	 * 创建时间
	 */
	private Date createAt;

	/**
	 * 创建人
	 */
	private String createBy;

	/**
	 * modify_at
	 */
	private Date modifyAt;

	/**
	 * modify_by
	 */
	private String modifyBy;

	/**
	 * isdel
	 */
	private Boolean isdel;

	private String accessLevel;

	private String disable;

	private Map<String,Long> podCount;

	private List<Occupy> occupyCpuReq;

	private List<Occupy> occupyMemReq;

	private List<Occupy> occupyCpuLimit;

	private List<Occupy> occupyMemLimit;

//	private List<Occupy> occupyCpuUsed;

//	private List<Occupy> occupyMemUsed;
}

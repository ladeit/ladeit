package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ladeit.pojo.dto.metric.pod.Occupy;
import lombok.Data;

import javax.persistence.Transient;
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
public class EnvAO {

	/**
	 * 主键 primary key
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String id;

	/**
	 * 环境名称
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String envName;

	/**
	 * 集群id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String clusterId;

	/**
	 * 命名空间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String namespace;

	/**
	 * test/dev/staging/prod
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String envTag;

	/**
	 * 是否需要人工审核(才能部署到此环境
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean audit;

	/**
	 * cpu限制
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer cpuLimit;

	/**
	 * cpu限制单位
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String cpuLimitUnit;

	/**
	 * 内存限制
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer memLimit;

	/**
	 * 内存限制单位
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String memLimitUnit;

	/**
	 * cpu限制
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer cpuRequest;

	/**
	 * cpu限制单位
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String cpuRequestUnit;

	/**
	 * 内存限制
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer memRequest;

	/**
	 * 内存限制单位
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String memRequestUnit;

	/**
	 * 是否启动资源配额
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean resourceQuota;
	/**
	 * 流量限制
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer networkLimit;

	/**
	 * 10-public 20-private
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean visibility;

	/**
	 * 创建时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createAt;

	/**
	 * 创建人
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String createBy;

	/**
	 * modify_at
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date modifyAt;

	/**
	 * modify_by
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String modifyBy;

	/**
	 * isdel
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isdel;

	private String accessLevel;

	private String disable;

	private List<Occupy> occupyCpuReq;

	private List<Occupy> occupyMemReq;

	private List<Occupy> occupyCpuLimit;

	private List<Occupy> occupyMemLimit;

}

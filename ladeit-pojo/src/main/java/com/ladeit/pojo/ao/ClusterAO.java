package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: Cluster
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class ClusterAO {
	/**
	 * 主键 primary key
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String id;

	/**
	 * 集群名称
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String k8sName;

	/**
	 * 集群配置文件
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String k8sKubeconfig;

	/**
	 * 删除标记
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isdel;

	/**
	 * 创建人
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String createBy;

	/**
	 * 创建时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createAt;

	private String accessLevel;

	private String disable;

	private List<EnvAO> envs;

}

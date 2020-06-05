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
	private String id;

	/**
	 * 集群名称
	 */
	private String k8sName;

	/**
	 * 集群配置文件
	 */
	private String k8sKubeconfig;

	/**
	 * 删除标记
	 */
	private Boolean isdel;

	/**
	 * 创建人
	 */
	private String createBy;

	/**
	 * 创建时间
	 */
	private Date createAt;

	private String accessLevel;

	private String disable;

	private List<EnvAO> envs;

}

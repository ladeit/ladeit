package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @program: ladeit
 * @description: Cluster
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
@Entity
@Table(name = "cluster")
public class Cluster {
	/**
	 * 主键 primary key
	 */
	@Id
	private String id;

	/**
	 * 集群名称
	 */
	@Column(name = "k8s_name")
	private String k8sName;

	/**
	 * 集群配置文件
	 */
	@Column(name = "k8s_kubeconfig")
	private String k8sKubeconfig;

	/**
	 * 删除标记
	 */
	@Column(name = "isdel")
	private Boolean isdel;

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
	 * 创建时间
	 */
	@Column(name = "create_at")
	private Date createAt;

	/**
	 * Invite_code
	 */
	@Column(name = "Invite_code")
	private String inviteCode;

	@Column(name = "disable")
	private String disable;

}

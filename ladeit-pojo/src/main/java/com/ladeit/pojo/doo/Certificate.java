package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @program: ladeit
 * @description: Certificate
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
@Table(name = "certificate")
@Entity
public class Certificate {
	/**
	 * id
	 */
	@Id
	private String id;

	/**
	 * service_group_id
	 */
	@Column(name = "service_group_id")
	private String serviceGroupId;

	/**
	 * 内容（base64编码）
	 */
	@Column(name = "content")
	private String content;

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

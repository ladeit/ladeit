package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @description: Yaml
 * @author: MddandPyy
 * @create: 2019/12/09
 * @version: 1.0.0
 */

@Data
@Entity
@Table(name="yaml")
public class Yaml {

	/**
	 * 主键 primary key
	 */
	@Id
	private String id;

	/**
	 * service_group_id
	 */
	@Column(name = "service_group_id")
	private String serviceGroupId;

	/**
	 * service_id
	 */
	@Column(name = "service_id")
	private String serviceId;

	/**
	 * content
	 */
	@Column(name = "content")
	private String content;

	/**
	 * create_at
	 */
	@Column(name = "create_at")
	private Date createAt;

	@Column(name = "name")
	private String name;

	@Column(name = "type")
	private String type;

}

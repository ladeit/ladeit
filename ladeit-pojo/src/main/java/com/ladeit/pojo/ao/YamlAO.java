package com.ladeit.pojo.ao;

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
public class YamlAO {

	/**
	 * 主键 primary key
	 */
	private String id;

	/**
	 * service_group_id
	 */
	private String serviceGroupId;

	/**
	 * service_id
	 */
	private String serviceId;

	/**
	 * content
	 */
	private String content;

	/**
	 * create_at
	 */
	private Date createAt;

	private String name;

	private String type;

}

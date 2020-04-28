package com.ladeit.pojo.doo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @program: ladeit
 * @description: Image
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
@Table(name = "image")
@Entity
public class Image {
	/**
	 * id
	 */
	@Id
	private String id;

	/**
	 * service_id
	 */
	@Column(name = "service_id")
	private String serviceId;

	/**
	 * 上传方标识
	 */
	@Column(name = "upload_source")
	private String uploadSource;

	/**
	 * image
	 */
	@Column(name = "image")
	private String image;

	/**
	 * tag
	 */
	@Column(name = "tag")
	private String tag;

	/**
	 * 默认与tag相同，平台中可以修改此字段
	 */
	@Column(name = "version")
	private String version;

	/**
	 * refs
	 */
	@Column(name = "refs")
	private String refs;

	/**
	 * commit_hash
	 */
	@Column(name = "commit_hash")
	private String commitHash;

	/**
	 * s varchar(255)
	 */
	@Column(name = "comments")
	private String comments;

	/**
	 * create_at
	 */
	@Column(name = "create_at")
	private Date createAt;

	/**
	 * isdel
	 */
	@Column(name = "isdel")
	private Boolean isdel;
}

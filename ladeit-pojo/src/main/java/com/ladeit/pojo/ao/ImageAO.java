package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * @program: ladeit
 * @description: Image
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class ImageAO {
	/**
	 * id
	 */
	private String id;

	/**
	 * service_id
	 */
	private String serviceId;

	/**
	 * 上传方标识
	 */
	private String uploadSource;

	/**
	 * image
	 */
	private String image;

	/**
	 * tag
	 */
	private String tag;

	/**
	 * 默认与tag相同，平台中可以修改此字段
	 */
	private String version;

	/**
	 * refs
	 */
	private String refs;

	/**
	 * commit_hash
	 */
	private String commitHash;

	/**
	 * s varchar(255)
	 */
	private String comments;

	/**
	 * create_at
	 */
	private Date createAt;

	/**
	 * isdel
	 */
	private Boolean isdel;
}

package com.ladeit.pojo.ao;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: Image
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class QueryImageAO {
	/**
	 * id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String id;

	/**
	 * service_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String serviceId;

	/**
	 * 上传方标识
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String uploadSource;

	/**
	 * image
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String image;

	/**
	 * tag
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String tag;

	/**
	 * 默认与tag相同，平台中可以修改此字段
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String version;

	/**
	 * refs
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String refs;

	/**
	 * commit_hash
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String commitHash;

	/**
	 * s varchar(255)
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String comments;

	/**
	 * create_at
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createAt;

	/**
	 * isdel
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isdel;

	private List<ReleaseAO> releaseAO;
}

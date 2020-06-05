package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * @program: ladeit
 * @description: Certificate
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class CertificateAO {
	/**
	 * id
	 */
	private String id;

	/**
	 * service_group_id
	 */
	private String serviceGroupId;

	/**
	 * 内容（base64编码）
	 */
	private String content;

	/**
	 * 创建时间
	 */
	private Date createAt;

	/**
	 * 创建人
	 */
	private String createBy;
}

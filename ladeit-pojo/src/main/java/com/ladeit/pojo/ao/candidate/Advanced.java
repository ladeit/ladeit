package com.ladeit.pojo.ao.candidate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @program: ladeit
 * @description: Advancde
 * @author: falcomlife
 * @create: 2019/11/05
 * @version: 1.0.0
 */
@Data
public class Advanced {

	/**
	 * 权重
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer weight;

	/**
	 * 转发uri
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String redirect;

	/**
	 * 重写uri
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String rewrite;

	/**
	 * 超时
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer timeout;

	/**
	 * 重试
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer retries;
}

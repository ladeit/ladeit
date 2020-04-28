package com.ladeit.pojo.ao.candidate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

/**
 * @program: ladeit
 * @description: MatchAO
 * @author: falcomlife
 * @create: 2019/11/05
 * @version: 1.0.0
 */
@Data
public class Match {
	/**
	 * 名称
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String name;
	/**
	 * 端口
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private int port;
	/**
	 * 忽略大小写
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean ignoreUriCase;
	/**
	 * 头
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Map<String,StringMatch> headers;
	/**
	 * 参数
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Map<String,StringMatch> queryParams;
	/**
	 * uri
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private StringMatch uri;
	/**
	 * scheme
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private StringMatch scheme;
	/**
	 * 方法
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private StringMatch method;
	/**
	 * 权限
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private StringMatch authority;
}

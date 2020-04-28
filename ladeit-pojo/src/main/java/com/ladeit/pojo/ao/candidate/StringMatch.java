package com.ladeit.pojo.ao.candidate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @program: ladeit
 * @description: StringMatch
 * @author: falcomlife
 * @create: 2019/11/05
 * @version: 1.0.0
 */
@Data
public class StringMatch {
	/**
	* 精确匹配
	*/
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String exact;
	/**
	 * 前缀匹配
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String prefix;
	/**
	 * 正则匹配
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String regex;
}

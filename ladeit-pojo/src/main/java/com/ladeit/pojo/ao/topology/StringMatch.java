package com.ladeit.pojo.ao.topology;

import lombok.Data;

import java.util.Map;

/**
 * @program: ladeit
 * @description: StringMatch
 * @author: falcomlife
 * @create: 2019/12/19
 * @version: 1.0.0
 */
@Data
public class StringMatch {
	private String key;
	private String type;
	private String expression;
	private String value;
}

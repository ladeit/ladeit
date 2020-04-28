package com.ladeit.pojo.ao.topology;

import lombok.Data;

import java.util.Map;

/**
 * @program: ladeit
 * @description: Route
 * @author: falcomlife
 * @create: 2019/12/19
 * @version: 1.0.0
 */
@Data
public class Route {
	private String id;
	private String host;
	private String subset;
	private Map<String,String> labels;
}

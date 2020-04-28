package com.ladeit.pojo.ao.topology;

import lombok.Data;

import java.util.List;

/**
 * @program: ladeit
 * @description: CorsPolicy
 * @author: falcomlife
 * @create: 2020/01/08
 * @version: 1.0.0
 */
@Data
public class CorsPolicy {
	private List<String> allowOrigin;
	private List<String> allowMethods;
	private List<String> allowHeaders;
	private List<String> exposeHeaders;
	private long maxAge;
	private Boolean allowCredentials;
}

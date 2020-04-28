package com.ladeit.pojo.ao.configuration;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: LivenessProbe
 * @author: falcomlife
 * @create: 2020/01/07
 * @version: 1.0.0
 */
@Data
public class LivenessProbe {
	private String type;
	private String protocol;
	private String path;
	private String command;
	private List<Map<String,String>> heads;
	private int port;
	private int initialDelaySeconds;
	private int periodSeconds;
	private int timeoutSeconds;
	private int failureThreshold;
	private int successThreshold;
}

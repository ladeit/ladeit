package com.ladeit.pojo.ao.typesResource;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: ServiceResource
 * @author: falcomlife
 * @create: 2019/12/07
 * @version: 1.0.0
 */
@Data
public class ServiceResource extends InnerResource {
	private String name;
	private List<Map<String,String>> ports;
	private Map<String,String> labels;
}

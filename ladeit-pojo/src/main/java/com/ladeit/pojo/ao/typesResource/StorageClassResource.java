package com.ladeit.pojo.ao.typesResource;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: StorageClassResource
 * @author: falcomlife
 * @create: 2019/12/07
 * @version: 1.0.0
 */
@Data
public class StorageClassResource extends InnerResource {
	private String name;
	private String type;
	private Map<String,String> labels;
}

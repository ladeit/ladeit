package com.ladeit.pojo.ao.typesResource;

import lombok.Data;

/**
 * @program: ladeit
 * @description: PVResource
 * @author: falcomlife
 * @create: 2019/12/07
 * @version: 1.0.0
 */
@Data
public class PVResource extends InnerResource {
	private String name;
	private String configuration;
}

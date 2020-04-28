package com.ladeit.pojo.ao.typesResource;

import lombok.Data;

import java.util.List;

/**
 * @program: ladeit
 * @description: InnerResource
 * @author: falcomlife
 * @create: 2019/12/07
 * @version: 1.0.0
 */
@Data
public class InnerResource {
	private List<InnerResource> children;
}

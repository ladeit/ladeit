package com.ladeit.pojo.ao.typesResource;

import lombok.Data;

import java.util.List;

/**
 * @program: ladeit
 * @description: ContainerResource
 * @author: falcomlife
 * @create: 2019/12/07
 * @version: 1.0.0
 */
@Data
public class ContainerResource extends InnerResource {
	private String name;
	private String image;
	private List<String> command;
	private List<String> args;
}

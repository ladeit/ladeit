package com.ladeit.pojo.ao;

import com.ladeit.pojo.ao.typesResource.InnerResource;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: TypesResourceAO
 * @author: falcomlife
 * @create: 2019/12/07
 * @version: 1.0.0
 */
@Data
public class TypesResourceAO {
	private String type;
	private String name;
	private Map<String,String> labels;
	private Date createAt;
	private Object info;
	private List<InnerResource> children;
}

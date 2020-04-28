package com.ladeit.pojo.ao;

import lombok.Data;

@Data
public class YamlContentAO {
	//资源类型
	private String kindType;
	//命名空间
	private String nameSpace;
	//yaml内容
	private String content;
	//serviceid
	private String serviceId;
	// version
	private String version;
}

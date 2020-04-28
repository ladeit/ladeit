package com.ladeit.pojo.ao;

import lombok.Data;

/**
 * @program: ladeit
 * @description: WorkLoadAO
 * @author: falcomlife
 * @create: 2019/12/01
 * @version: 1.0.0
 */
@Data
public class WorkLoadAO {
	/**
	 * 名称
	 */
	private String name;
	/**
	 * 类型
	 */
	private String type;
	/**
	 * 状态
	 */
	private String status;
	/**
	 * pod数
	 */
	private Integer count;
	/**
	 * 希望的pod数
	 */
	private Integer desire;
	/**
	 * 命名空间
	 */
	private String namespace;
	/**
	 * 环境
	 */
	private String env;
}

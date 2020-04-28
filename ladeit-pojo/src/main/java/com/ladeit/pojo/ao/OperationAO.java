package com.ladeit.pojo.ao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * @program: ladeit
 * @description: Operation
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class OperationAO {
	/**
	 * deploy_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String deployId;

	/**
	 * 操作对象的类型 service / candidate / release / image / service_group 等
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String target;

	/**
	 * 操作对象的id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String targetId;

	/**
	 * 操作内容，应详尽描述
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String eventLog;

	/**
	 * 1 创建节点 2 删除节点 3 修改路由权重 4 修改匹配规则 5 修改策略 6 调整pod数量 (数量太多，待完善)
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer eventType;

	/**
	 * 创建时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date createAt;

	/**
	 * 创建人
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String createBy;
}

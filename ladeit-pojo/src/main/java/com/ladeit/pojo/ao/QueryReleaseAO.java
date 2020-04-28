package com.ladeit.pojo.ao;

import com.ladeit.pojo.ao.release.Candidate;
import com.ladeit.pojo.ao.release.Service;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: Release
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */
@Data
public class QueryReleaseAO {
	/**
	 * id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String id;

	/**
	 * 冗余，一般情况下为候选人的版本号
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String name;

	/**
	 * service_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String serviceId;


	/**
	 * image_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String imageId;

	/**
	 * candidate_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Candidate candidate;

	/**
	 * service_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Service service;

	/**
	 * 发布开始时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date deployStartAt;

	/**
	 * 发布完成时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date deployFinishAt;

	/**
	 * 服役开始时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date serviceStartAt;

	/**
	 * 服役结束时间
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Date serviceFinishAt;

	/**
	 * 0 准备中 1 服役中 2 退役 3 异常
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer status;

	/**
	 * 1 金丝雀发布 2 蓝绿发布 4 abtest发布 8 滚动发布 每添加一种发布方式，此字段加上相应的值
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer type;

	/**
	 * 服役时长 单位秒 退役时才更新此字段 service_ finish_at减 service_start_at
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer duration;

	/**
	 * 创建人
	 */

	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String createBy;

	/**
	 * 是否使用默认配置
	 */
	private Boolean isDefault;

	private List<OperationAO> operations;

	private String operChannel;

}

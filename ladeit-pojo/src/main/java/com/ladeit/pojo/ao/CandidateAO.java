package com.ladeit.pojo.ao;

import com.ladeit.pojo.ao.candidate.Advanced;
import com.ladeit.pojo.ao.candidate.Match;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: Candidate
 * @author: falcomlife
 * @create: 2019/11/04
 * @version: 1.0.0
 */

@Data
public class CandidateAO {
	/**
	 * 主键 primary key
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String id;

	/**
	 * version
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String name;

	/**
	 * project_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String releaseId;

	/**
	 * image_id
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String imageId;

	/**
	 * 1 金丝雀节点 2 蓝绿节点 3 abtest节点
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer type;

	/**
	 * 是否是主节点
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean ismaster;

	/**
	 * 运行时长 单位秒 删除时才更新此字段
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer duration;

	/**
	 * 预设pod总数
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private String podCount;

	/**
	 * 0 启用 1 暂停
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer status;

	/**
	 * match中的东西，包括method，param，uri，head等都在这里写
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private List<Match> match;

	/**
	* 高级配置
	*/
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private List<Advanced> advanced;

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

	/**
	 * 是否删除 0 否 1 是
	 */
	// @JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isdel;

}
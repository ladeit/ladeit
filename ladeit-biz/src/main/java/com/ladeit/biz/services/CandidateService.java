package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.TopologyAO;
import com.ladeit.pojo.doo.Candidate;

/**
 * @program: ladeit
 * @description: CandidateService
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
public interface CandidateService {

	/**
	 * 新建candidate
	 *
	 * @param candidate
	 * @return void
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	void insert(Candidate candidate);

	/**
	 * 根据releaseId得到使用中的候选节点
	 *
	 * @param releaseId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Candidate>
	 * @author falcomlife
	 * @date 19-11-11
	 * @version 1.0.0
	 */
	ExecuteResult<Candidate> getInUseCandidateByReleaseId(String releaseId);

	/**
	 * 通过服务id候选节点
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Candidate>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	ExecuteResult<Candidate> getInUseCandidateByServiceId(String serviceId);

	/**
	 * 更新candidate
	 *
	 * @param candidate
	 * @return void
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	void update(Candidate candidate);

	/**
	 * 更新service的拓扑图，重新规划流量
	 *
	 * @param topologyAO
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-23
	 * @version 1.0.0
	 */
	ExecuteResult<String> updateTopology(TopologyAO topologyAO, String serviceId);

	/**
	 * 更新候选节点状态
	 *
	 * @param candidate
	 * @return void
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	void updateStatus(Candidate candidate);

	/**
	 * 根据releaseid和名字获取候选节点
	 *
	 * @param id
	 * @param subset
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Candidate>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	ExecuteResult<Candidate> getByReleaseIdAndName(String id, String subset);

	/**
	 * 更新候选节点权重
	 *
	 * @param id
	 * @param weight
	 * @return void
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	void updateWeight(String id, Integer weight);
}

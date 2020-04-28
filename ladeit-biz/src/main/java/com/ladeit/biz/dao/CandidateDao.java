package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Candidate;

/**
 * @program: ladeit
 * @description: CandidateDao
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
public interface CandidateDao {

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
	 * @return com.ladeit.pojo.doo.Candidate
	 * @author falcomlife
	 * @date 19-11-11
	 * @version 1.0.0
	 */
	Candidate getInUseCandidateByReleaseId(String releaseId);

	/**
	* 更新candidate
	* @author falcomlife
	* @date 19-12-13
	* @version 1.0.0
	* @return void
	* @param candidate
	*/
	void update(Candidate candidate);

	/**
	 * 更新状态
	 */
	void updateStatus(Candidate candidate);

    Candidate getByReleaseIdAndName(String id, String subset);

	void updateWeight(String id, Integer weight);

	Candidate getInUseCandidateByServiceId(String serviceId);
}

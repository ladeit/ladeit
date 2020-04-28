package com.ladeit.biz.services.impl;

import com.ladeit.biz.dao.CandidateDao;
import com.ladeit.biz.services.CandidateService;
import com.ladeit.biz.services.ReleaseService;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.UserInfo;
import com.ladeit.pojo.ao.TopologyAO;
import com.ladeit.pojo.doo.Candidate;
import com.ladeit.pojo.doo.Release;
import com.ladeit.pojo.doo.User;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @program: ladeit
 * @description: CandidateServiceImpl
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
@Service
public class CandidateServiceImpl implements CandidateService {

	@Autowired
	private CandidateDao candidateDao;
	@Autowired
	private ReleaseService releaseService;
	@Autowired
	private MessageUtils messageUtils;

	/**
	 * 新建candidate
	 *
	 * @param candidate
	 * @return void
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	@Override
	public void insert(Candidate candidate) {
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		candidate.setCreateAt(new Date());
		candidate.setCreateBy(user.getUsername());
		candidate.setCreateById(user.getId());
		candidate.setIsdel(false);
		this.candidateDao.insert(candidate);
	}


	/**
	 * 根据releaseId得到使用中的候选节点
	 *
	 * @param releaseId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Candidate>
	 * @author falcomlife
	 * @date 19-11-11
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<Candidate> getInUseCandidateByReleaseId(String releaseId) {
		ExecuteResult<Candidate> result = new ExecuteResult<>();
		Candidate candidate = this.candidateDao.getInUseCandidateByReleaseId(releaseId);
		if (candidate == null) {
			result.setCode(Code.NOTFOUND);
			String message = messageUtils.matchMessage("M0001",new Object[]{releaseId},Boolean.TRUE);
			result.addWarningMessage(message);
			return result;
		}
		result.setResult(candidate);
		return result;
	}

	/**
	 * 根据releaseId得到使用中的候选节点
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Candidate>
	 * @author falcomlife
	 * @date 19-11-11
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<Candidate> getInUseCandidateByServiceId(String serviceId) {
		ExecuteResult<Candidate> result = new ExecuteResult<>();
		Candidate candidate = this.candidateDao.getInUseCandidateByServiceId(serviceId);
		if (candidate == null) {
			result.setCode(Code.NOTFOUND);
			String message = messageUtils.matchMessage("M0002",new Object[]{serviceId},Boolean.TRUE);
			result.addWarningMessage(message);
			return result;
		}
		result.setResult(candidate);
		return result;
	}
	/**
	 * 更新candidate
	 *
	 * @param candidate
	 * @return void
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	@Override
	public void update(Candidate candidate) {
		this.candidateDao.update(candidate);
	}

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
	@Override
	public ExecuteResult<String> updateTopology(TopologyAO topologyAO, String serviceId) {
		return null;
	}

	/**
	 * 更新候选节点状态
	 *
	 * @param candidate
	 * @return void
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public void updateStatus(Candidate candidate) {
		this.candidateDao.updateStatus(candidate);
	}

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
	@Override
	public ExecuteResult<Candidate> getByReleaseIdAndName(String id, String subset) {
		ExecuteResult<Candidate> result = new ExecuteResult<>();
		Candidate c = this.candidateDao.getByReleaseIdAndName(id,subset);
		result.setResult(c);
		return result;
	}

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
	@Override
	public void updateWeight(String id, Integer weight) {
		this.candidateDao.updateWeight(id,weight);
	}
}

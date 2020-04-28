package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.CandidateDao;
import com.ladeit.pojo.doo.Candidate;
import io.ebean.EbeanServer;
import io.ebean.UpdateQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @program: ladeit
 * @description: CandidateDaoImpl
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
@Repository
public class CandidateDaoImpl implements CandidateDao {

	@Autowired
	private EbeanServer server;

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
		this.server.insert(candidate);
	}

	/**
	 * 根据releaseId得到使用中的候选节点
	 *
	 * @param releaseId
	 * @return com.ladeit.pojo.doo.Candidate
	 * @author falcomlife
	 * @date 19-11-11
	 * @version 1.0.0
	 */
	@Override
	public Candidate getInUseCandidateByReleaseId(String releaseId) {
		return this.server.createQuery(Candidate.class).where().eq("release_id", releaseId).eq("status", 0).eq("isdel"
				, false).findOne();
	}

	/**
	* 更新candidate
	* @author falcomlife
	* @date 19-12-13
	* @version 1.0.0
	* @return void
	* @param candidate
	*/
	@Override
	public void update(Candidate candidate) {
		UpdateQuery<Candidate> updateQuery = this.server.update(Candidate.class);
		if(StringUtils.isNotBlank(candidate.getImageId())){
			updateQuery.set("image_id",candidate.getImageId());
		}
		if(StringUtils.isNotBlank(candidate.getReleaseId())){
			updateQuery.set("release_id",candidate.getReleaseId());
		}
		if(candidate.getStatus()!=null){
			updateQuery.set("status",candidate.getStatus());
		}
		updateQuery.where().eq("id",candidate.getId()).update();
	}

	/**
	 * 更新状态
	 */
	@Override
	public void updateStatus(Candidate candidate) {
		this.server.update(Candidate.class).set("status", candidate.getStatus()).where().eq("id", candidate.getId()).update();
	}

	@Override
	public Candidate getByReleaseIdAndName(String id, String subset) {
		return this.server.createQuery(Candidate.class).where().eq("release_id",id).eq("name",subset).findOne();
	}

	@Override
	public void updateWeight(String id, Integer weight) {
		this.server.update(Candidate.class).set("weight",weight).where().eq("id",id).update();
	}

	@Override
	public Candidate getInUseCandidateByServiceId(String serviceId) {
		return this.server.createQuery(Candidate.class).where().eq("service_id", serviceId).eq("status", 0).eq("isdel"
				, false).findOne();
	}
}

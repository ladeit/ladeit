package com.ladeit.biz.utils;

import com.ladeit.pojo.ao.TopologyAO;
import com.ladeit.pojo.dto.CandidateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class Producer {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	public void putCandidate(String releaseId, String serviceid, String candidateId, String imageId, String uid,
							 TopologyAO topology, String version, int type, Integer[] scalecount, Boolean auto) {
		CandidateDto candidate = new CandidateDto();
		candidate.setServiceId(serviceid);
		candidate.setReleaseId(releaseId);
		candidate.setCandidateId(candidateId);
		candidate.setImageId(imageId);
		candidate.setUid(uid);
		candidate.setTopologyAO(topology);
		candidate.setVersion(version);
		candidate.setType(type);
		candidate.setScaleCount(scalecount);
		candidate.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		candidate.setAuto(auto);
		redisTemplate.opsForSet().add("candidate", candidate);
		redisTemplate.opsForSet().add("candidateCache:" + serviceid + "," + releaseId + "," + type, candidate);
	}
}

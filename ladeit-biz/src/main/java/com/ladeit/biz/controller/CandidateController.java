package com.ladeit.biz.controller;

import com.ladeit.biz.services.CandidateService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.CandidateAO;
import com.ladeit.pojo.ao.TopologyAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @program: ladeit
 * @description: CandidateController
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/candidate")
public class CandidateController {


	@Autowired
	private CandidateService candidateService;

	/**
	 * 更新service的拓扑图，重新规划流量
	 * @author falcomlife
	 * @date 19-12-21
	 * @version 1.0.0
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @param topologyAO
	 */
	@PostMapping("/{serviceId}/topology")
	public ExecuteResult<String> createCandidata(@RequestBody TopologyAO topologyAO,@PathVariable String serviceId) {
		return this.candidateService.updateTopology(topologyAO,serviceId);
	}
}

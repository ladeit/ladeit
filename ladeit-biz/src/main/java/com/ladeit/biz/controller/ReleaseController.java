package com.ladeit.biz.controller;

import com.ladeit.biz.services.ReleaseService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.QueryReleaseAO;
import com.ladeit.pojo.ao.ReleaseAO;
import com.ladeit.pojo.ao.TopologyAO;
import com.ladeit.pojo.doo.Candidate;
import com.ladeit.pojo.doo.Release;
import com.ladeit.pojo.doo.Service;
import io.kubernetes.client.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @program: ladeit
 * @description: ReleaseController
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/release")
public class ReleaseController {

	@Autowired
	private ReleaseService releaseService;

	/**
	 * 新建release
	 *
	 * @param releaseAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	@PostMapping
	public ExecuteResult<String> newRelease(@RequestBody ReleaseAO releaseAO) throws IOException, ApiException,
			InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		Release release = new Release();
		Service service = new Service();
		Candidate candidate = new Candidate();
		BeanUtils.copyProperties(releaseAO, release);
		BeanUtils.copyProperties(releaseAO.getCandidate(), candidate);
		BeanUtils.copyProperties(releaseAO.getService(), service);
		release.setOperChannel("ladeit");
		return this.releaseService.newRelease(service.getId(),release, service, candidate, releaseAO.getResourceAO(), releaseAO.getConfiguration());
	}

	/**
	 * 升级release
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	@PutMapping
	public ExecuteResult<String> refreshRelease(@RequestBody ReleaseAO releaseAO) throws IOException, ApiException {
		Release release = new Release();
		Service service = new Service();
		Candidate candidate = new Candidate();
		TopologyAO topology = new TopologyAO();
		BeanUtils.copyProperties(releaseAO, release);
		BeanUtils.copyProperties(releaseAO.getCandidate(), candidate);
		BeanUtils.copyProperties(releaseAO.getService(), service);
		if (releaseAO.getTopologyAO() != null) {
			BeanUtils.copyProperties(releaseAO.getTopologyAO(), topology);
		}
		release.setOperChannel("ladeit");
		return this.releaseService.refreshRelease(service.getId(),release, service, candidate, topology,null,
				releaseAO.getConfiguration());
	}

	/**
	 * 查询某个release
	 *
	 * @param releaseId,releaseName
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	@GetMapping("/get")
	public ExecuteResult<List<ReleaseAO>> queryReleaseInfo(@RequestParam(value = "ReleaseId", required = false) String releaseId, @RequestParam(value = "ReleaseName", required = false) String releaseName) {
		return releaseService.queryReleaseInfo(releaseId, releaseName);
	}

	/**
	 * 根据releaseID查询某个release
	 *
	 * @param releaseId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	@GetMapping("/getOne")
	public ExecuteResult<QueryReleaseAO> queryOneReleaseInfo(@RequestParam("ReleaseId") String releaseId) {
		return releaseService.queryOneReleaseInfo(releaseId);
	}
}

package com.ladeit.biz.controller;

import com.ladeit.biz.services.ImageService;
import com.ladeit.biz.services.ReleaseService;
import com.ladeit.biz.services.ServiceGroupService;
import com.ladeit.biz.services.ServiceService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.doo.Service;
import com.ladeit.util.ExecuteResultUtil;
import com.ladeit.util.ListUtil;
import io.kubernetes.client.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 与组相关操作
 *
 * @author MddandPyy
 * @version V1.0
 * @Classname GroupController
 * @Date 2019/11/6 11:37
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/service")
public class ServiceController {

	@Autowired
	private ServiceService serviceService;

	@Autowired
	private ServiceGroupService serviceGroupService;

	@Autowired
	private ImageService imageService;


	/**
	 * 查询服务热力信息
	 *
	 * @param startDate,endDate
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.HeatMapAO>>
	 * @date 2019/12/09
	 * @ahthor MddandPyy
	 */
	@GetMapping("/getHeatMap")
	public ExecuteResult<List<HeatMapAO>> getServiceHeatMap(@RequestParam("TargetId") String targetId,
@RequestParam(value = "Startdate", required = false) String startDate, @RequestParam(value = "EndDate", required = false) String endDate) {
		return serviceService.getServiceHeatMap(targetId, startDate, endDate);
	}


	/**
	 * 查询服务的相关信息
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceDeployAO>
	 * @date 2019/12/10
	 * @ahthor MddandPyy
	 */
	@GetMapping("/getServiceInfos")
	public ExecuteResult<ServiceDeployAO> getServiceInfos(@RequestParam("ServiceId") String serviceId) {
		return serviceService.getServiceInfos(serviceId);
	}

	/**
	 * 删除服务
	 *
	 * @param serviceAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/2
	 * @ahthor MddandPyy
	 */
	@DeleteMapping("/delete")
	public ExecuteResult<String> deleteService(@RequestBody ServiceAO serviceAO) throws IOException {
		return serviceService.deleteService(serviceAO.getId(), serviceAO);
	}

	/**
	 * 添加机器人发布参数
	 *
	 * @param servicePublishBotAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/6
	 * @ahthor MddandPyy
	 */
	@PostMapping("/addServicePublishBot")
	public ExecuteResult<String> addServicePublishBot(@RequestBody ServicePublishBotAO servicePublishBotAO) {
		return serviceService.addServicePublishBot(servicePublishBotAO.getServiceId(), servicePublishBotAO);
	}

	/**
	 * 添加服务
	 *
	 * @param serviceAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/6
	 * @ahthor MddandPyy
	 */
	@PostMapping("/addService")
	public ExecuteResult<String> addService(@RequestBody ServiceAO serviceAO) throws IOException, ApiException {
		return serviceService.addService(serviceAO.getServiceGroupId(), serviceAO);
	}

	/**
	 * 添加服务镜像
	 *
	 * @param addServiceAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/6
	 * @ahthor MddandPyy
	 */
	@PostMapping("/image")
	public ExecuteResult<String> addService(@RequestBody AddServiceAO addServiceAO) throws IOException, ApiException {
		return serviceGroupService.addService(addServiceAO);
	}

	/**
	 * 更新服务token信息
	 *
	 * @param serviceAO
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceAO>
	 * @date 2020/3/9
	 * @ahthor MddandPyy
	 */
	@PutMapping("/updateServiceToken")
	public ExecuteResult<ServiceAO> updateServiceToken(@RequestBody ServiceAO serviceAO) {
		return serviceService.updateServiceToken(serviceAO.getId(), serviceAO);
	}

	/**
	 * 根据imageId查询镜像信息
	 *
	 * @param imageId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ImageAO>
	 * @date 2020/3/18
	 * @ahthor MddandPyy
	 */
	@GetMapping("/image")
	public ExecuteResult<QueryImageAO> getImageById(@RequestParam("ServiceId") String serviceId, @RequestParam(
	  "ImageId") String imageId) {
		ExecuteResult<QueryImageAO> result = new ExecuteResult<>();
		result = imageService.getImageById(serviceId, imageId);
		return result;
	}

	/**
	 * 获取升级中的service
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ServiceAO>>
	 * @author falcomlife
	 * @date 20-4-21
	 * @version 1.0.0
	 */
	@GetMapping
	public ExecuteResult<List<ServiceAO>> getStatusService() {
		ExecuteResult<List<ServiceAO>> result = new ExecuteResult<>();
		ExecuteResult<List<Service>> service = this.serviceService.getService(null);
		if (!service.getResult().isEmpty()) {
			result.setResult(new ListUtil<Service, ServiceAO>().copyList(service.getResult(), ServiceAO.class));
		}
		return result;
	}

	/**
	 * 重启服务
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-16
	 * @version 1.0.0
	 */
	@PutMapping("/{serviceId}")
	public ExecuteResult<String> restart(@PathVariable String serviceId) throws ApiException {
		return this.serviceService.restart(serviceId);
	}
}

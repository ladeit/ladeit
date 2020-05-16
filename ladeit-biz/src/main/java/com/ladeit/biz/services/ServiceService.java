package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.HeatMapAO;
import com.ladeit.pojo.ao.ServiceAO;
import com.ladeit.pojo.ao.ServiceDeployAO;
import com.ladeit.pojo.ao.ServicePublishBotAO;
import com.ladeit.pojo.doo.Service;
import io.kubernetes.client.ApiException;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: ServiceService
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
public interface ServiceService {

	/**
	 * 更新service
	 *
	 * @param service
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	ExecuteResult<String> updateById(Service service);

	/**
	 * 查询service
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Service>
	 * @author falcomlife
	 * @date 19-12-12
	 * @version 1.0.0
	 */
	ExecuteResult<Service> getById(String serviceId);

	/**
	 * service的operation添加（并且维护service的热力记录信息）
	 *
	 * @param serviceid, eventlog, eventType
	 * @return void
	 * @date 2019/12/9
	 * @ahthor MddandPyy
	 */
	void serviceAddOperation(String serviceGroupId, String serviceid, String serviceName, String eventlog,
							 Integer eventType);

	/**
	 * 添加热力信息记录
	 *
	 * @param targetId
	 * @return void
	 * @date 2020/1/17
	 * @ahthor MddandPyy
	 */
	void insertHeatMap(String targetId);

	/**
	 * 查询服务热力信息
	 *
	 * @param startDate,endDate
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.HeatMapAO>>
	 * @date 2019/12/09
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<HeatMapAO>> getServiceHeatMap(String targetId, String startDate, String endDate);

	/**
	 * 查询服务的相关信息
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceDeployAO>
	 * @date 2019/12/10
	 * @ahthor MddandPyy
	 */
	ExecuteResult<ServiceDeployAO> getServiceInfos(String serviceId);

	/**
	 * 删除服务
	 *
	 * @param serviceId
	 * @param serviceAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author MddandPyy
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	ExecuteResult<String> deleteService(String serviceId, ServiceAO serviceAO) throws IOException;

	/**
	 * 添加机器人发布参数
	 *
	 * @param servicePublishBotAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/6
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> addServicePublishBot(String serviceId, ServicePublishBotAO servicePublishBotAO);

	/**
	 * 根据id更新状态
	 *
	 * @param service
	 * @return void
	 * @author falcomlife
	 * @date 20-4-8
	 * @version 1.0.0
	 */
	void updateStatusById(Service service);

	/**
	 * 根据groupid查询service
	 *
	 * @param groupId
	 * @return
	 */
	List<Service> getServiceByGroupId(String groupId);

	/**
	 * 添加服务
	 *
	 * @param serviceAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/6
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> addService(String groupId, ServiceAO serviceAO) throws IOException, ApiException;

	/**
	 * 更新服务token信息
	 *
	 * @param serviceAO
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceAO>
	 * @date 2020/3/9
	 * @ahthor MddandPyy
	 */
	ExecuteResult<ServiceAO> updateServiceToken(String serviceId, ServiceAO serviceAO);

	/**
	* 获取升级中的service
	* @author falcomlife
	* @date 20-4-21
	* @version 1.0.0
	* @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.doo.Service>>
	* @param status
	*/
	ExecuteResult<List<Service>> getService(String status);

	/**
	 * 查询用户可以访问的所有存在的服务Id
	 *
	 * @param userId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<java.lang.String>>
	 * @author falcomlife
	 * @date 20-4-28
	 * @version 1.0.0
	 */
	ExecuteResult<List<String>> getServiceBelongUser(String userId);

	/**
	* 重启服务
	* @author falcomlife
	* @date 20-5-16
	* @version 1.0.0
	* @return com.ladeit.common.ExecuteResult<java.lang.String>
	* @param serviceId
	*/
	ExecuteResult<String> restart(String serviceId) throws ApiException;
}

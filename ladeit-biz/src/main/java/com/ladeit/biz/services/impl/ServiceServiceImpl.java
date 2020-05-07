package com.ladeit.biz.services.impl;

import com.ladeit.biz.annotation.Authority;
import com.ladeit.biz.dao.*;
import com.ladeit.biz.manager.K8sWorkLoadsManager;
import com.ladeit.biz.services.*;
import com.ladeit.biz.utils.CommonConsant;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.HeatMapAO;
import com.ladeit.pojo.ao.ServiceAO;
import com.ladeit.pojo.ao.ServiceDeployAO;
import com.ladeit.pojo.ao.ServicePublishBotAO;
import com.ladeit.pojo.doo.*;
import com.ladeit.util.ListUtil;
import com.ladeit.util.auth.TokenUtil;
import io.kubernetes.client.ApiException;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @program: ladeit
 * @description: ServiceServiceImpl
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
@Service
public class ServiceServiceImpl implements ServiceService {

	@Autowired
	private ServiceDao serviceDao;
	@Autowired
	private OperationService operationService;
	@Autowired
	private HeatMapDao heatMapDao;
	@Autowired
	private ReleaseDao releaseDao;
	@Autowired
	private ImageDao imageDao;
	@Autowired
	private EnvService envService;
	@Autowired
	private EnvDao envDao;
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private K8sWorkLoadsManager k8sWorkLoadsManager;
	@Autowired
	private OperationDao operationDao;
	@Autowired
	private ServicePublishBotDao servicePublishBotDao;
	@Autowired
	private ServiceGroupService serviceGroupService;
	@Autowired
	private UserServiceRelationDao userServiceRelationDao;
	@Autowired
	private UserServiceGroupRelationDao userServiceGroupRelationDao;
	@Autowired
	private MessageService messageService;
	@Autowired
	private ServiceGroupDao serviceGroupDao;
	@Autowired
	private MessageUtils messageUtils;

	/**
	 * 更新service
	 *
	 * @param service
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> updateById(com.ladeit.pojo.doo.Service service) {
		ExecuteResult<String> result = new ExecuteResult<>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		service.setModifyAt(new Date());
		if (user == null) {
			service.setModifyBy("bot");
		} else {
			service.setModifyBy(user.getUsername());
		}
		this.serviceDao.update(service);
		return result;
	}

	/**
	 * 查询service
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Service>
	 * @author falcomlife
	 * @date 19-12-12
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<com.ladeit.pojo.doo.Service> getById(String serviceId) {
		ExecuteResult<com.ladeit.pojo.doo.Service> result = new ExecuteResult<>();
		com.ladeit.pojo.doo.Service service = this.serviceDao.getById(serviceId);
		result.setResult(service);
		return result;
	}

	/**
	 * service的operation添加（并且维护service的热力记录信息,推送消息至slack）
	 *
	 * @param serviceid, eventlog, eventType
	 * @return void
	 * @date 2019/12/9
	 * @ahthor MddandPyy
	 */
	@Override
	public void serviceAddOperation(String serviceGroupId, String serviceid, String serviceName, String eventlog,
									Integer eventType) {
		Operation operation = new Operation();
		operation.setDeployId(UUID.randomUUID().toString());
		operation.setTarget("service");
		operation.setTargetId(serviceid);
		operation.setEventLog(eventlog);
		operation.setEventType(eventType);
		operationDao.insert(operation);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String queryDate = sdf.format(date);
		HeatMap heatMap = heatMapDao.queryHeatMapByTargetIdAndDate(serviceid, sdf.format(date));
		if (heatMap == null) {
			HeatMap heatMapnew = new HeatMap();
			heatMapnew.setDate(date);
			heatMapnew.setId(UUID.randomUUID().toString());
			heatMapnew.setNum(1);
			heatMapnew.setTargetId(serviceid);
			heatMapDao.insert(heatMapnew);
		} else {
			heatMap.setNum(heatMap.getNum() + 1);
			heatMapDao.update(heatMap);
		}
	}

	/**
	 * 添加热力信息记录
	 *
	 * @param targetId
	 * @return void
	 * @date 2020/1/17
	 * @ahthor MddandPyy
	 */
	@Override
	public void insertHeatMap(String targetId) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String queryDate = sdf.format(date);
		HeatMap heatMap = heatMapDao.queryHeatMapByTargetIdAndDate(targetId, sdf.format(date));
		if (heatMap == null) {
			HeatMap heatMapnew = new HeatMap();
			heatMapnew.setDate(date);
			heatMapnew.setId(UUID.randomUUID().toString());
			heatMapnew.setNum(1);
			heatMapnew.setTargetId(targetId);
			heatMapDao.insert(heatMapnew);
		} else {
			heatMap.setNum(heatMap.getNum() + 1);
			heatMapDao.update(heatMap);
		}
	}

	/**
	 * 查询服务热力信息
	 *
	 * @param startDate,endDate
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.HeatMapAO>>
	 * @date 2019/12/09
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<HeatMapAO>> getServiceHeatMap(String targetId, String startDate, String endDate) {
		ExecuteResult<List<HeatMapAO>> result = new ExecuteResult<>();
		List<HeatMap> heatMaps = heatMapDao.queryHeatMapsByTargetIdAndDate(targetId, startDate, endDate);
		List<HeatMapAO> heatMapAOS = new ListUtil<HeatMap, HeatMapAO>().copyList(heatMaps,
				HeatMapAO.class);
		result.setResult(heatMapAOS);
		return result;
	}

	/**
	 * 查询服务的相关信息
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceDeployAO>
	 * @date 2019/12/10
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<ServiceDeployAO> getServiceInfos(String serviceId) {
		ExecuteResult<ServiceDeployAO> result = new ExecuteResult<ServiceDeployAO>();
		ServiceDeployAO serviceDeployAO = new ServiceDeployAO();
		com.ladeit.pojo.doo.Service service = serviceDao.queryServiceById(serviceId);
		serviceDeployAO.setReleaseAt(service.getReleaseAt());
		serviceDeployAO.setImageVersion(service.getImageVersion());
		serviceDeployAO.setImageId(service.getImageId());
		serviceDeployAO.setStatus(service.getStatus());
		serviceDeployAO.setServiceId(service.getId());
		Release release = releaseDao.getInUseReleaseByServiceId(serviceId);
		if (release != null) {
			serviceDeployAO.setDuration(release.getServiceStartAt());
		}
		List<Image> images = imageDao.queryImages(serviceId);
		serviceDeployAO.setImageNum(images.size());
		List<Release> releases = releaseDao.getReleaseList(serviceId);
		serviceDeployAO.setReleaseNum(releases.size());
		result.setResult(serviceDeployAO);
		return result;
	}

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
	@Override
	@Authority(type = "service", level = "W")
	public ExecuteResult<String> deleteService(String serviceId, ServiceAO serviceAO) throws IOException {
		ExecuteResult<String> result = new ExecuteResult<String>();
		com.ladeit.pojo.doo.Service service = serviceDao.getById(serviceAO.getId());
		service.setIsdel(true);
		serviceDao.delete(service);
		Boolean isDelK8s = serviceAO.getIsDelK8s();
		if (isDelK8s) {
			String envid = service.getEnvId();
			Env env = this.envService.getEnvById(envid);
			Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
			k8sWorkLoadsManager.deleteResources(service.getId(), env.getNamespace(),
					cluster.getK8sKubeconfig(), service.getName());
		}


		ServiceGroup serviceGroup = serviceGroupDao.queryServiceById(service.getServiceGroupId());
		//消息创建
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		Message message = new Message();
		message.setId(UUID.randomUUID().toString());
		// serviceGroup.getName() + "/" + service.getName() + " was deleted by " + user.getUsername()
		message.setTitle(serviceGroup.getName() + "/" + service.getName() + " was deleted by " + user.getUsername() +
				" .");
		message.setContent(serviceGroup.getName() + "/" + service.getName() + " was deleted by " + user.getUsername() + " .");
		message.setCreateAt(new Date());
		message.setType(CommonConsant.MESSAGE_TYPE_4);
		message.setTargetId(service.getId());
		message.setServiceId(service.getId());
		message.setOperuserId(user.getId());
		message.setLevel("NORMAL");
		message.setServiceGroupId(service.getServiceGroupId());
		message.setMessageType(CommonConsant.MESSAGE_TYPE_R);
		messageService.insertMessage(message, true);
		messageService.insertSlackMessage(message);
		String messagestr = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}


	/**
	 * 添加机器人发布参数
	 *
	 * @param servicePublishBotAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/6
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "service", level = "X")
	public ExecuteResult<String> addServicePublishBot(String serviceId, ServicePublishBotAO servicePublishBotAO) {
		ExecuteResult<String> result = new ExecuteResult<String>();
		String operType = servicePublishBotAO.getOperType();
		ServicePublishBot servicePublishBot =
				servicePublishBotDao.queryServicePublishBotByServiceId(servicePublishBotAO.getServiceId());
		String publishBotId = null;
		if (CommonConsant.BOT_OPER_TYPE_0.equals(operType)) {
			if (servicePublishBot != null) {
				servicePublishBotDao.delete(servicePublishBot);
			}
		} else {
			if (servicePublishBot != null) {
				servicePublishBot.setExamineType(servicePublishBotAO.getExamineType());
				servicePublishBot.setOperType(servicePublishBotAO.getOperType());
				servicePublishBot.setPublishType(servicePublishBotAO.getPublishType());
				servicePublishBot.setServiceGroupId(servicePublishBotAO.getServiceGroupId());
				servicePublishBot.setServiceId(servicePublishBotAO.getServiceId());
				publishBotId = servicePublishBot.getId();
				servicePublishBotDao.update(servicePublishBot);
			} else {
				ServicePublishBot servicePublishBotNew = new ServicePublishBot();
				BeanUtils.copyProperties(servicePublishBotAO, servicePublishBotNew);
				publishBotId = UUID.randomUUID().toString();
				servicePublishBotNew.setId(publishBotId);
				servicePublishBotDao.insert(servicePublishBotNew);
			}
		}
		result.setResult(publishBotId);
		return result;
	}

	/**
	 * 根据id更新状态
	 *
	 * @param service
	 * @return void
	 * @author falcomlife
	 * @date 20-4-8
	 * @version 1.0.0
	 */
	@Override
	public void updateStatusById(com.ladeit.pojo.doo.Service service) {
		this.serviceDao.updateStatusById(service);
	}

	/**
	 * 根据groupid查询service
	 *
	 * @param groupId
	 * @return
	 */
	@Override
	public List<com.ladeit.pojo.doo.Service> getServiceByGroupId(String groupId) {
		return this.serviceDao.getServiceByGroupId(groupId);
	}

	/**
	 * 添加服务
	 *
	 * @param serviceAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/6
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "group", level = "W")
	public ExecuteResult<String> addService(String groupId, ServiceAO serviceAO) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<String>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		com.ladeit.pojo.doo.Service s = serviceDao.queryServiceByGroupAndName(serviceAO.getServiceGroupId(),
				serviceAO.getName());
		// 后台验证服务名不能包含大写字母
		Boolean match = Pattern.matches("[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*",
				serviceAO.getName());
		if (!match) {
			result.setCode(Code.PARAM_ERROR);
			String message = messageUtils.matchMessage("M0026", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		if (s != null) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0027", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		//添加服务组token记录
		String token = null;
		try {
			token = TokenUtil.createToken(serviceAO.getName() + System.currentTimeMillis());
		} catch (Exception e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0021", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		com.ladeit.pojo.doo.Service service = new com.ladeit.pojo.doo.Service();
		BeanUtils.copyProperties(serviceAO, service);
		service.setToken(token);
		String uuid = UUID.randomUUID().toString();
		service.setId(uuid);
		service.setCreateAt(new Date());
		service.setIsdel(false);
		//初始状态 -1，尚未运行
		service.setStatus(com.ladeit.pojo.doo.Service.SERVICE_STATUS_INIT);
		service.setCreateBy(user.getUsername());
		service.setCreateById(user.getId());
		ExecuteResult<String> serviceType = envService.namespaceType(service.getClusterId(), service.getEnvId());
		int code = serviceType.getCode();
		if (code == Code.NOTFOUND) {
			Env env = envDao.getEnvById(service.getEnvId());
			env.setDisable("true");
			envDao.updateEnv(env);
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0028", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		service.setServiceType(serviceType.getResult());
		serviceDao.insert(service);
		UserServiceRelation serviceRelation = new UserServiceRelation();
		serviceRelation.setId(UUID.randomUUID().toString());
		serviceRelation.setRoleNum("R,W,X");
		serviceRelation.setServiceId(uuid);
		serviceRelation.setUserId(user.getId());
		userServiceRelationDao.insert(serviceRelation);
		ServiceGroup serviceGroup = serviceGroupDao.queryServiceById(groupId);
		//消息创建
		Message message = new Message();
		message.setId(UUID.randomUUID().toString());
		// serviceGroup.getName() + "/"+service.getName() + " was created by " + user.getUsername()
		message.setTitle(serviceGroup.getName() + "/" + service.getName() + " was created by " + user.getUsername() +
				" .");
		message.setContent(serviceGroup.getName() + "/" + service.getName() + " was created by " + user.getUsername() + " .");
		message.setCreateAt(new Date());
		message.setType(CommonConsant.MESSAGE_TYPE_3);
		message.setTargetId(service.getId());
		message.setServiceId(service.getId());
		message.setOperuserId(user.getId());
		message.setLevel("NORMAL");
		message.setServiceGroupId(service.getServiceGroupId());
		message.setTarget_user(user.getId());
		message.setMessageType(CommonConsant.MESSAGE_TYPE_R);
		messageService.insertMessage(message, true);
		String messagestr = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}

	/**
	 * 更新服务token信息
	 *
	 * @param serviceAO
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceAO>
	 * @date 2020/3/9
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "service", level = "W")
	public ExecuteResult<ServiceAO> updateServiceToken(String serviceId, ServiceAO serviceAO) {
		ExecuteResult<ServiceAO> result = new ExecuteResult<ServiceAO>();
		com.ladeit.pojo.doo.Service service = serviceDao.queryServiceById(serviceId);
		//添加服务组token记录
		String token = null;
		try {
			token = TokenUtil.createToken(serviceAO.getName() + System.currentTimeMillis());
		} catch (Exception e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0021", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		service.setToken(token);
		serviceDao.updateService(service);
		BeanUtils.copyProperties(service, serviceAO);
		result.setResult(serviceAO);
		return result;
	}

	/**
	 * 获取升级中的service
	 *
	 * @param status
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.doo.Service>>
	 * @author falcomlife
	 * @date 20-4-21
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<com.ladeit.pojo.doo.Service>> getService(String status) {
		ExecuteResult<List<com.ladeit.pojo.doo.Service>> result = new ExecuteResult<>();
		com.ladeit.pojo.doo.Service s = new com.ladeit.pojo.doo.Service();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		List<UserServiceRelation> usr = this.userServiceRelationDao.getServiceRelationByUserId(user.getId());
		List<String> ids = usr.stream().map(UserServiceRelation::getServiceId).collect(Collectors.toList());
		List<com.ladeit.pojo.doo.Service> list = null;
		if (status != null) {
			s.setIds(ids);
			s.setStatus(status);
			list = this.serviceDao.getService(s);
		} else {
			s.setIds(ids);
			List<String> statuses = new ArrayList<>();
			statuses.add("1");
			statuses.add("2");
			statuses.add("3");
			statuses.add("4");
			statuses.add("8");
			list = this.serviceDao.getService(s, statuses);
		}
		if (list.isEmpty()) {
			result.setCode(Code.NOTFOUND);
			result.addWarningMessage("未找到发布中的服务");
		} else {
			result.setResult(list);
		}
		return result;
	}

	/**
	 * 查询用户可以访问的所有存在的服务Id
	 *
	 * @param userId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<java.lang.String>>
	 * @author falcomlife
	 * @date 20-4-28
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<String>> getServiceBelongUser(String userId) {
		ExecuteResult<List<String>> result = new ExecuteResult<>();
		List<UserServiceRelation> usr = this.userServiceRelationDao.getServiceRelationByUserId(userId);
		if (usr == null || usr.isEmpty()) {
			result.setCode(Code.NOTFOUND);
			result.addWarningMessage("未找到属于该人员的服务");
			return result;
		}
		List<String> list =
				usr.stream().filter(userServiceRelation -> this.serviceDao.getById(userServiceRelation.getServiceId()) != null).filter(userServiceRelation -> userServiceRelation.getRoleNum() != null && userServiceRelation.getRoleNum().contains("R")).map(userServiceRelation -> userServiceRelation.getServiceId()).collect(Collectors.toList());
		result.setResult(list);
		return result;
	}
}

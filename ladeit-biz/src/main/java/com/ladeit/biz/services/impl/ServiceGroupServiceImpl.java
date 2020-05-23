package com.ladeit.biz.services.impl;

import com.ladeit.api.annotation.SlackLogin;
import com.ladeit.biz.annotation.Authority;
import com.ladeit.biz.dao.*;
import com.ladeit.biz.manager.IstioManager;
import com.ladeit.biz.manager.K8sWorkLoadsManager;
import com.ladeit.biz.services.*;
import com.ladeit.biz.shiro.SecurityUtils;
import com.ladeit.biz.utils.CommonConsant;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.ao.topology.StringMatch;
import com.ladeit.pojo.doo.*;
import com.ladeit.util.ListUtil;
import com.ladeit.util.auth.TokenUtil;
import io.ebean.SqlRow;
import io.kubernetes.client.ApiException;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPMatchRequest;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;

import org.apache.shiro.subject.Subject;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ServiceGroupServiceImpl
 * @Date 2019/11/6 13:27
 */
@Service
public class ServiceGroupServiceImpl implements ServiceGroupService {

	@Autowired
	private ServiceGroupDao serviceGroupDao;
	@Autowired
	private CertificateDao certificateDao;
	@Autowired
	private ServiceDao serviceDao;
	@Autowired
	private EnvDao envDao;
	@Autowired
	private ImageDao imageDao;
	@Autowired
	private ClusterDao clusterDao;
	@Autowired
	private ReleaseDao releaseDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserServiceGroupRelationDao userServiceGroupRelationDao;
	@Autowired
	private UserServiceRelationDao userServiceRelationDao;
	@Autowired
	private ServiceService serviceService;
	@Autowired
	private IstioManager istioManager;
	@Autowired
	private K8sWorkLoadsManager k8sWorkLoadsManager;
	@Autowired
	private EnvService envService;
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private ChannelServiceGroupDao channelServiceGroupDao;
	@Autowired
	private ServicePublishBotDao servicePublishBotDao;
	@Autowired
	private UserEnvRelationDao userEnvRelationDao;
	@Autowired
	private UserClusterRelationDao userClusterRelationDao;
	@Autowired
	private MessageService messageService;
	@Autowired
	private MessageDao messageDao;
	@Autowired
	private ReleaseService releaseService;
	@Autowired
	private UserSlackRelationDao userSlackRelationDao;
	@Autowired
	private MessageUtils messageUtils;
	@Autowired
	private ResourceService resourceService;

	/**
	 * 创建服务组
	 *
	 * @param serviceGroupAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/6
	 * @ahthor MddandPyy
	 */
	@Override
	@Transactional
	public ExecuteResult<String> addServiceGroup(ServiceGroupAO serviceGroupAO) {
		ExecuteResult<String> result = new ExecuteResult<String>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		Date date = new Date();
		// 验证，同一个账号下面的服务组名不能重复，其他结论暂时没出来，这里以后应该有验证
		//添加服务组
		ServiceGroup serviceGroup = new ServiceGroup();
		BeanUtils.copyProperties(serviceGroupAO, serviceGroup);
		ServiceGroup group = serviceGroupDao.queryServiceByNameIsDel(serviceGroup.getName());
		if (group != null) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0020", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		String uuid = UUID.randomUUID().toString();
		serviceGroup.setId(uuid);
		serviceGroup.setCreateBy(user.getUsername());
		serviceGroup.setCreateById(user.getId());
		serviceGroup.setCreateAt(date);
		serviceGroup.setIsdel(false);
		serviceGroup.setIsdelService(false);
		String inviteCode = null;
		try {
			inviteCode = TokenUtil.createToken(serviceGroup.getName() + "invitecode" + System.currentTimeMillis());
		} catch (Exception e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0006", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		serviceGroup.setInviteCode(inviteCode);
		serviceGroupDao.insert(serviceGroup);

		//添加服务组token记录
		String token = null;
		try {
			token = TokenUtil.createToken(serviceGroup.getName() + System.currentTimeMillis());
		} catch (Exception e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0021", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		Certificate certificate = new Certificate();
		certificate.setId(UUID.randomUUID().toString());
		certificate.setServiceGroupId(uuid);
		certificate.setContent(token);
		certificate.setCreateBy(user.getUsername());
		certificate.setCreateById(user.getId());
		certificate.setCreateAt(date);
		certificateDao.insert(certificate);

		//组的创建者，就是组的默认管理员
		UserServiceGroupRelation userServiceGroupRelation = new UserServiceGroupRelation();
		userServiceGroupRelation.setId(UUID.randomUUID().toString());
		userServiceGroupRelation.setServiceGroupId(uuid);
		userServiceGroupRelation.setUserId(user.getId());
		userServiceGroupRelation.setAccessLevel("RW");
		userServiceGroupRelation.setCreateAt(date);
		userServiceGroupRelationDao.insert(userServiceGroupRelation);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);

		return result;
	}

	/**
	 * ci调用接口，传递servicetoken，实现对service镜像的添加
	 *
	 * @param addServiceAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/6
	 * @ahthor MddandPyy
	 */
	@Override
	@Transactional
	public ExecuteResult<String> addService(AddServiceAO addServiceAO) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		Date date = new Date();
		String token = addServiceAO.getToken();
		com.ladeit.pojo.doo.Service exitsService = serviceDao.getServiceByToken(token);
		if (exitsService == null) {
			//String message = messageUtils.matchMessage("M0022", new Object[]{},Boolean.FALSE);
			result.setCode(Code.FAILED);
			result.addErrorMessage("Invalid token");
		} else {
			String exitsServiceName = exitsService.getName();
			String imageServiceName = addServiceAO.getServiceName();
			if (!imageServiceName.equals(exitsServiceName)) {
				//String message = messageUtils.matchMessage("M0033", new Object[]{},Boolean.FALSE);
				result.setCode(Code.FAILED);
				result.addErrorMessage("Invalid service name or token");
				return result;
			}

			// add service version
			serviceService.serviceAddOperation(exitsService.getServiceGroupId(), exitsService.getId(),
					exitsService.getName(), "service版本添加", null);
			//添加镜像
			Image image = new Image();
			String imageid = UUID.randomUUID().toString();
			image.setId(imageid);
			image.setServiceId(exitsService.getId());
			image.setImage(addServiceAO.getImage());
			image.setVersion(addServiceAO.getVersion());
			image.setTag(addServiceAO.getVersion());
			image.setRefs(addServiceAO.getRefs());
			image.setCommitHash(addServiceAO.getCommitHash());
			image.setComments(addServiceAO.getComments());
			image.setCreateAt(date);
			image.setIsdel(false);
			imageDao.insert(image);
			//String messagestr = messageUtils.matchMessage("M0100", new Object[]{},Boolean.FALSE);
			result.setResult("Successfully");
			ServiceGroup group = serviceGroupDao.queryServiceById(exitsService.getServiceGroupId());
			//消息创建
			Message message = new Message();
			message.setId(UUID.randomUUID().toString());
			// found new version
			message.setTitle(group.getName() + "/" + exitsService.getName() + " found new version: " + addServiceAO.getVersion());
			message.setContent(group.getName() + "/" + exitsService.getName() + " found new version: " + addServiceAO.getVersion());
			message.setCreateAt(new Date());
			message.setType(CommonConsant.MESSAGE_TYPE_1);
			message.setTargetId(exitsService.getId());
			message.setServiceId(exitsService.getId());
			message.setLevel("NORMAL");
			message.setServiceGroupId(exitsService.getServiceGroupId());
			message.setMessageType(CommonConsant.MESSAGE_TYPE_S);
			messageService.insertMessage(message, false);
			//调用推送slack消息接口
			Map<String, Object> param = new HashMap();
			param.put("serviceGroupId", exitsService.getServiceGroupId());
			param.put("serviceId", exitsService.getId());
			param.put("serviceName", exitsService.getName());
			param.put("imageId", imageid);
			param.put("imageName", addServiceAO.getImage());
			param.put("tag", addServiceAO.getVersion());
			param.put("version", addServiceAO.getVersion());
			param.put("refs", addServiceAO.getRefs());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			param.put("createAt", sdf.format(date));
			String slackPublish = "no";
			//判断服务自动发布的状态
			ServicePublishBot servicePublishBot =
					this.servicePublishBotDao.queryServicePublishBotByServiceId(exitsService.getId());
			if (servicePublishBot != null && "2".equals(servicePublishBot.getOperType())) {
				if ("8".equals(servicePublishBot.getPublishType())) {
					//如果状态是自动滚动发布，开始自动滚动发布流程
					Release release = new Release();
					com.ladeit.pojo.doo.Service service = new com.ladeit.pojo.doo.Service();
					Candidate candidate = new Candidate();
					candidate.setImageId(imageid);
					candidate.setPodCount("1");
					service.setEnvId(exitsService.getEnvId());
					service.setId(exitsService.getId());
					service.setName(exitsService.getName());
					ExecuteResult<Release> releaseExecuteResult =
							this.releaseService.getInUseReleaseByServiceId(exitsService.getId());
					if (releaseExecuteResult.getResult() == null) {
						release.setName("release none -> " + addServiceAO.getVersion());
					} else {
						Image im = this.imageDao.getImageById(releaseExecuteResult.getResult().getImageId());
						release.setName("release " + im.getVersion() + " -> " + addServiceAO.getVersion());
					}
					release.setType(8);
					//调用发版接口开始发版
					release.setOperChannel("ladeit");
					this.releaseService.refreshReleaseAuto(service.getId(), release, service, candidate, null, true,
							null);
				}
			}
			if (servicePublishBot != null && "1".equals(servicePublishBot.getOperType())) {
				if ("1".equals(servicePublishBot.getExamineType())) {
					slackPublish = "yes";
				}
			}
			param.put("publishBotFlag", slackPublish);
			message.setParams(param);
			messageService.insertSlackMessage(message);
		}
		return result;
	}

	/**
	 * 删除服务组
	 *
	 * @param groupId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/2
	 * @ahthor MddandPyy
	 */
	@Override
	@Transactional
	@Authority(type = "group", level = "W")
	public ExecuteResult<String> deleteServiceGroup(String groupId, ServiceGroupAO serviceGroupAO) throws IOException {
		ExecuteResult<String> result = new ExecuteResult<String>();
		ServiceGroup group = serviceGroupDao.queryServiceById(groupId);
		if (group != null) {
			group.setIsdel(true);
			group.setIsdelService(serviceGroupAO.getIsdelService());
			serviceGroupDao.update(group);
			List<com.ladeit.pojo.doo.Service> services = serviceDao.queryServiceList(group.getId());
			Boolean isdelService = serviceGroupAO.getIsdelService();
			for (com.ladeit.pojo.doo.Service service : services) {
				service.setIsdel(true);
				serviceDao.delete(service);
				if (isdelService) {
					String envid = service.getEnvId();
					Env env = this.envService.getEnvById(envid);
					Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
					k8sWorkLoadsManager.deleteResources(service.getId(), env.getNamespace(),
							cluster.getK8sKubeconfig(), service.getName());
				}
			}
			String messagestr = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
			result.setResult(messagestr);
			//消息创建
			User user = (User) SecurityUtils.getSubject().getPrincipal();
			Message message = new Message();
			message.setId(UUID.randomUUID().toString());
			// is deleted.
			message.setTitle(group.getName() + " was deleted by " + user.getUsername() + ".");
			message.setContent(group.getName() + " was deleted by " + user.getUsername() + " .");
			message.setCreateAt(new Date());
			message.setType(CommonConsant.MESSAGE_TYPE_7);
			message.setTargetId(group.getId());
			message.setOperuserId(user.getId());
			message.setLevel("NORMAL");
			message.setServiceGroupId(group.getId());
			message.setMessageType(CommonConsant.MESSAGE_TYPE_R);
			messageService.insertMessage(message, true);
			//删除服务组下的所有人员的组权限和服务权限
			List<UserServiceGroupRelation> userServiceGroupRelations =
					userServiceGroupRelationDao.queryGrouprelationByGroupId(group.getId());
			for (UserServiceGroupRelation userServiceGroupRelation : userServiceGroupRelations) {
				//查询该组下的服务
				List<com.ladeit.pojo.doo.Service> serviceList =
						serviceDao.queryServiceList(userServiceGroupRelation.getServiceGroupId());
				for (com.ladeit.pojo.doo.Service service : serviceList) {
					UserServiceRelation userServiceRelation =
							userServiceRelationDao.getServiceRelation(userServiceGroupRelation.getUserId(),
									service.getId());
					if (userServiceRelation != null) {
						userServiceRelationDao.delete(userServiceRelation);
					}
				}
				userServiceGroupRelationDao.delete(userServiceGroupRelation);
			}
		} else {
			String messagestr = messageUtils.matchMessage("M0023", new Object[]{}, Boolean.TRUE);
			result.setResult(messagestr);
		}
		return result;
	}

	/**
	 * 查询服务组以及组下面的服务信息
	 *
	 * @param groupName
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.QueryServiceGroupAO>>
	 * @date 2019/11/7
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<QueryServiceGroupAO>> queryServiceGroupInfo(String groupName) throws IOException {
		ExecuteResult<List<QueryServiceGroupAO>> result = new ExecuteResult<List<QueryServiceGroupAO>>();
		List<QueryServiceGroupAO> queryInfo = new ArrayList<QueryServiceGroupAO>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		String userId = user.getId();
		List<UserServiceGroupRelation> groupList = userServiceGroupRelationDao.queryGrouprelationByUserId(userId);
		List<String> groupIdList = new ArrayList<>();
		for (UserServiceGroupRelation relation : groupList) {
			groupIdList.add(relation.getServiceGroupId());
		}
		List<ServiceGroup> serviceGroups = serviceGroupDao.queryServiceGroupList(groupName, groupIdList);
		for (ServiceGroup group : serviceGroups) {
			QueryServiceGroupAO groupAO = new QueryServiceGroupAO();
			List<QueryServiceAO> serviceAOList = new ArrayList<QueryServiceAO>();
			BeanUtils.copyProperties(group, groupAO);
			//获取当前登录人组权限
			UserServiceGroupRelation groupRelation = userServiceGroupRelationDao.getGrouprelation(userId,
					group.getId());
			if (groupRelation != null) {
				groupAO.setAccessLevel(groupRelation.getAccessLevel());
			}
			//获取环境和集群的名称
			//String envId = group.getEnvId();
			//String clusterId = group.getClusterId();
			//Env env = envDao.getEnvById(envId);
			//Cluster cluster = clusterDao.getClusterById(clusterId);
			//groupAO.setEnvName(env.getEnvName());
			//groupAO.setClusterName(cluster.getK8sName());
			List<com.ladeit.pojo.doo.Service> services = serviceDao.queryServiceList(group.getId());
			for (com.ladeit.pojo.doo.Service service : services) {
				//获取当前登录人服务权限
				UserServiceRelation serviceRelation = userServiceRelationDao.getServiceRelation(userId,
						service.getId());
				if ((serviceRelation != null && serviceRelation.getRoleNum().contains("R")) || "admin".equals(user.getUsername())) {
					QueryServiceAO queryServiceAO = new QueryServiceAO();
					//添加服务镜像信息，创建时间排序，前3条
					List<SqlRow> images = imageDao.queryImagesOrderByCreateAt(service.getId());
					List<ImageAO> imageAOS = new ArrayList<>();
					for (SqlRow image : images) {
						ImageAO imageAO = new ImageAO();
						imageAO.setId(image.getString("id"));
						imageAO.setVersion(image.getString("version"));
						imageAO.setCreateAt(image.getTimestamp("create_at"));
						imageAOS.add(imageAO);
					}
					//添加服务通知信息，当前登录人未读消息前3条
					List<SqlRow> messages = messageDao.queryMessageByServiceId(service.getId(), userId);
					List<MessageAO> messageAOS = new ArrayList<>();
					for (SqlRow message : messages) {
						MessageAO messageAO = new MessageAO();
						messageAO.setId(message.getString("id"));
						messageAO.setTitle(message.getString("title"));
						messageAO.setCreateAt(message.getTimestamp("create_at"));
						messageAOS.add(messageAO);
					}
					BeanUtils.copyProperties(service, queryServiceAO);
					queryServiceAO.setImageAOS(imageAOS);
					queryServiceAO.setMessageAOS(messageAOS);
					Env env = envDao.getEnvById(service.getEnvId());
					Cluster cluster = clusterDao.getClusterById(service.getClusterId());
					// ExecuteResult<Release> re = releaseService.getInUseReleaseByServiceId(service.getId());
					// Release release = re.getResult();
					// ReleaseAO releaseAO= new ReleaseAO();
					// BeanUtils.copyProperties(release, releaseAO);
					// queryServiceAO.setRelease(releaseAO);
					if ("-1".equals(service.getStatus())) {
						VirtualService virtualService = this.istioManager.getVirtualservice(cluster.getK8sKubeconfig(),
								"virtual`service-" + service.getName(), env.getNamespace());
						if (virtualService != null) {
							queryServiceAO.setGateway(virtualService.getSpec().getGateways());
							List<List<List<StringMatch>>> stringMatchLists = new ArrayList<>();
							for (HTTPRoute httpRoute : virtualService.getSpec().getHttp()) {
								List<List<StringMatch>> stringMatchList = new ArrayList<>();
								for (HTTPMatchRequest match : httpRoute.getMatch()) {
									List<StringMatch> stringMatchs = new ArrayList<>();
									// Authority
									if (match.getAuthority() != null) {
										StringMatch authority = new StringMatch();
										String expression =
												match.getAuthority().getMatchType().toString().split("\\(")[1].split(
														"=")[0];
										String tmp =
												match.getAuthority().getMatchType().toString().split("\\(")[1].split(
														"=")[1];
										String value = tmp.substring(0, tmp.length() - 1);
										authority.setType("Authority");
										authority.setExpression(expression);
										authority.setValue(value);
										stringMatchs.add(authority);
									}
									// method
									if (match.getMethod() != null) {
										StringMatch method = new StringMatch();
										String expression =
												match.getMethod().getMatchType().toString().split("\\(")[1].split("=")[0];
										String tmp =
												match.getMethod().getMatchType().toString().split("\\(")[1].split("=")[1];
										String value = tmp.substring(0, tmp.length() - 1);
										method.setType("method");
										method.setExpression(expression);
										method.setValue(value);
										stringMatchs.add(method);
									}
									// schema
									if (match.getScheme() != null) {
										StringMatch schema = new StringMatch();
										String expression =
												match.getScheme().getMatchType().toString().split("\\(")[1].split("=")[0];
										String tmp =
												match.getScheme().getMatchType().toString().split("\\(")[1].split("=")[1];
										String value = tmp.substring(0, tmp.length() - 1);
										schema.setType("scheme");
										schema.setExpression(expression);
										schema.setValue(value);
										stringMatchs.add(schema);
									}
									// uri
									if (match.getUri() != null) {
										StringMatch uri = new StringMatch();
										String expression =
												match.getUri().getMatchType().toString().split("\\(")[1].split("=")[0];
										String tmp =
												match.getUri().getMatchType().toString().split("\\(")[1].split("=")[1];
										String value = tmp.substring(0, tmp.length() - 1);
										uri.setType("uri");
										uri.setExpression(expression);
										uri.setValue(value);
										stringMatchs.add(uri);
									}
									// headers
									if (match.getHeaders() != null) {
										match.getHeaders().forEach((key, value) -> {
											StringMatch uri = new StringMatch();
											String expression =
													value.getMatchType().toString().split("\\(")[1].split("=")[0];
											String tmp = value.getMatchType().toString().split("\\(")[1].split("=")[1];
											String val = tmp.substring(0, tmp.length() - 1);
											uri.setKey(key);
											uri.setType("headers");
											uri.setExpression(expression);
											uri.setValue(val);
											stringMatchs.add(uri);
										});
									}
									stringMatchList.add(stringMatchs);
								}
								stringMatchLists.add(stringMatchList);
							}
							queryServiceAO.setMatch(stringMatchLists);
						}
					}
					ServicePublishBot servicePublishBot =
							servicePublishBotDao.queryServicePublishBotByServiceId(service.getId());
					ServicePublishBotAO servicePublishBotAO = new ServicePublishBotAO();
					if (servicePublishBot != null) {
						BeanUtils.copyProperties(servicePublishBot, servicePublishBotAO);
					}
					queryServiceAO.setServicePublishBot(servicePublishBotAO);
					Cluster c = clusterDao.getClusterById(service.getClusterId());
					queryServiceAO.setClustername(c.getK8sName());
					Env e = envDao.getEnvById(service.getEnvId());
					queryServiceAO.setEnvname(e.getNamespace());
					int imagenum = serviceDao.getImageNum(service.getId());
					queryServiceAO.setImagenum(imagenum);
					if (serviceRelation != null) {
						queryServiceAO.setRoleNum(serviceRelation.getRoleNum());
					}
					ExecuteResult<Map<String,Long>> mapRes = this.resourceService.getPodsStatus(service.getId());
					queryServiceAO.setPodStatus(mapRes.getResult());
					serviceAOList.add(queryServiceAO);
				}
			}
			groupAO.setServicelist(serviceAOList);
			queryInfo.add(groupAO);
		}
		result.setResult(queryInfo);
		return result;
	}

	/**
	 * 查询服务组以及组下面的服务信息(管理员)
	 *
	 * @param groupName
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.QueryServiceGroupAO>>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<QueryServiceGroupAO>> queryAdminServiceGroupInfo(String groupName) throws IOException {
		ExecuteResult<List<QueryServiceGroupAO>> result = new ExecuteResult<List<QueryServiceGroupAO>>();
		List<QueryServiceGroupAO> queryInfo = new ArrayList<QueryServiceGroupAO>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		String userId = user.getId();
		List<ServiceGroup> serviceGroups = serviceGroupDao.queryServiceGroupListByName(groupName);
		for (ServiceGroup group : serviceGroups) {
			QueryServiceGroupAO groupAO = new QueryServiceGroupAO();
			List<QueryServiceAO> serviceAOList = new ArrayList<QueryServiceAO>();
			BeanUtils.copyProperties(group, groupAO);
			//获取当前登录人组权限
			UserServiceGroupRelation groupRelation = userServiceGroupRelationDao.getGrouprelation(userId,
					group.getId());
			if (groupRelation != null) {
				groupAO.setAccessLevel(groupRelation.getAccessLevel());
			}
			//获取环境和集群的名称
			//String envId = group.getEnvId();
			//String clusterId = group.getClusterId();
			//Env env = envDao.getEnvById(envId);
			//Cluster cluster = clusterDao.getClusterById(clusterId);
			//groupAO.setEnvName(env.getEnvName());
			//groupAO.setClusterName(cluster.getK8sName());
			List<com.ladeit.pojo.doo.Service> services = serviceDao.queryServiceList(group.getId());
			for (com.ladeit.pojo.doo.Service service : services) {
				QueryServiceAO queryServiceAO = new QueryServiceAO();
				BeanUtils.copyProperties(service, queryServiceAO);
				Env env = envDao.getEnvById(service.getEnvId());
				Cluster cluster = clusterDao.getClusterById(service.getClusterId());
				// ExecuteResult<Release> re = releaseService.getInUseReleaseByServiceId(service.getId());
				// Release release = re.getResult();
				// ReleaseAO releaseAO= new ReleaseAO();
				// BeanUtils.copyProperties(release, releaseAO);
				// queryServiceAO.setRelease(releaseAO);
				//获取当前登录人服务权限
				UserServiceRelation serviceRelation = userServiceRelationDao.getServiceRelation(userId,
						service.getId());
				VirtualService virtualService = this.istioManager.getVirtualservice(cluster.getK8sKubeconfig(),
						"virtualservice-" + service.getName(), env.getNamespace());
				if (virtualService != null) {
					queryServiceAO.setGateway(virtualService.getSpec().getGateways());
					List<List<List<StringMatch>>> stringMatchLists = new ArrayList<>();
					for (HTTPRoute httpRoute : virtualService.getSpec().getHttp()) {
						List<List<StringMatch>> stringMatchList = new ArrayList<>();
						for (HTTPMatchRequest match : httpRoute.getMatch()) {
							List<StringMatch> stringMatchs = new ArrayList<>();
							// Authority
							if (match.getAuthority() != null) {
								StringMatch authority = new StringMatch();
								String expression =
										match.getAuthority().getMatchType().toString().split("\\(")[1].split("=")[0];
								String tmp =
										match.getAuthority().getMatchType().toString().split("\\(")[1].split("=")[1];
								String value = tmp.substring(0, tmp.length() - 1);
								authority.setType("Authority");
								authority.setExpression(expression);
								authority.setValue(value);
								stringMatchs.add(authority);
							}
							// method
							if (match.getMethod() != null) {
								StringMatch method = new StringMatch();
								String expression = match.getMethod().getMatchType().toString().split("\\(")[1].split(
										"=")[0];
								String tmp = match.getMethod().getMatchType().toString().split("\\(")[1].split("=")[1];
								String value = tmp.substring(0, tmp.length() - 1);
								method.setType("method");
								method.setExpression(expression);
								method.setValue(value);
								stringMatchs.add(method);
							}
							// schema
							if (match.getScheme() != null) {
								StringMatch schema = new StringMatch();
								String expression = match.getScheme().getMatchType().toString().split("\\(")[1].split(
										"=")[0];
								String tmp = match.getScheme().getMatchType().toString().split("\\(")[1].split("=")[1];
								String value = tmp.substring(0, tmp.length() - 1);
								schema.setType("scheme");
								schema.setExpression(expression);
								schema.setValue(value);
								stringMatchs.add(schema);
							}
							// uri
							if (match.getUri() != null) {
								StringMatch uri = new StringMatch();
								String expression =
										match.getUri().getMatchType().toString().split("\\(")[1].split("=")[0];
								String tmp = match.getUri().getMatchType().toString().split("\\(")[1].split("=")[1];
								String value = tmp.substring(0, tmp.length() - 1);
								uri.setType("uri");
								uri.setExpression(expression);
								uri.setValue(value);
								stringMatchs.add(uri);
							}
							// headers
							if (match.getHeaders() != null) {
								match.getHeaders().forEach((key, value) -> {
									StringMatch uri = new StringMatch();
									String expression = value.getMatchType().toString().split("\\(")[1].split("=")[0];
									String tmp = value.getMatchType().toString().split("\\(")[1].split("=")[1];
									String val = tmp.substring(0, tmp.length() - 1);
									uri.setKey(key);
									uri.setType("headers");
									uri.setExpression(expression);
									uri.setValue(val);
									stringMatchs.add(uri);
								});
							}
							stringMatchList.add(stringMatchs);
						}
						stringMatchLists.add(stringMatchList);
					}
					queryServiceAO.setMatch(stringMatchLists);
				}
				if (serviceRelation != null) {
					queryServiceAO.setRoleNum(serviceRelation.getRoleNum());
				}
				ServicePublishBot servicePublishBot =
						servicePublishBotDao.queryServicePublishBotByServiceId(service.getId());
				ServicePublishBotAO servicePublishBotAO = new ServicePublishBotAO();
				if (servicePublishBot != null) {
					BeanUtils.copyProperties(servicePublishBot, servicePublishBotAO);
				}
				queryServiceAO.setServicePublishBot(servicePublishBotAO);
				Cluster c = clusterDao.getClusterById(service.getClusterId());
				queryServiceAO.setClustername(c.getK8sName());
				Env e = envDao.getEnvById(service.getEnvId());
				queryServiceAO.setEnvname(e.getNamespace());
				serviceAOList.add(queryServiceAO);
			}
			groupAO.setServicelist(serviceAOList);
			queryInfo.add(groupAO);
		}
		result.setResult(queryInfo);
		return result;
	}

	/**
	 * 查询服务组以及组下面的服务信息(分页)
	 *
	 * @param groupName
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.QueryServiceGroupAO>>
	 * @date 2020/2/10
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<Pager<QueryServiceGroupAO>> queryServiceGroupPagerInfo(int currentPage, int pageSize,
																				String groupName) {
		ExecuteResult<Pager<QueryServiceGroupAO>> result = new ExecuteResult<Pager<QueryServiceGroupAO>>();
		List<ServiceGroup> serviceGroups = serviceGroupDao.queryServiceGroupPagerList(currentPage, pageSize,
				groupName);
		int count = serviceGroupDao.queryGroupCount(groupName);
		Pager<QueryServiceGroupAO> pager = new Pager<>();
		pager.setPageNum(currentPage);
		pager.setPageSize(pageSize);
		List<QueryServiceGroupAO> groupAOList = new ArrayList<QueryServiceGroupAO>();
		for (ServiceGroup group : serviceGroups) {
			QueryServiceGroupAO queryServiceGroupAO = new QueryServiceGroupAO();
			BeanUtils.copyProperties(group, queryServiceGroupAO);
			List<com.ladeit.pojo.doo.Service> services = serviceDao.queryServiceList(group.getId());
			List<QueryServiceAO> queryServiceAOS = new ArrayList<>();
			for (com.ladeit.pojo.doo.Service service : services) {
				QueryServiceAO queryServiceAO = new QueryServiceAO();
				BeanUtils.copyProperties(service, queryServiceAO);
				Cluster c = clusterDao.getClusterById(service.getClusterId());
				queryServiceAO.setClustername(c.getK8sName());
				Env e = envDao.getEnvById(service.getEnvId());
				queryServiceAO.setEnvname(e.getNamespace());
				queryServiceAOS.add(queryServiceAO);
			}
			queryServiceGroupAO.setServicelist(queryServiceAOS);
			groupAOList.add(queryServiceGroupAO);
		}
		pager.setRecords(groupAOList);
		pager.setTotalRecord(count);
		result.setResult(pager);
		return result;
	}

	/**
	 * 查询服务组以及组下面的服务信息(分页),后期优化查询返回sqlrow
	 *
	 * @param groupName
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.QueryServiceGroupAO>>
	 * @date 2020/2/10
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "adminquery")
	public ExecuteResult<Pager<SqlRow>> queryServiceGroupSqlrowPagerInfo(int currentPage, int pageSize,
																		 String groupName, String orderparam) {
		ExecuteResult<Pager<SqlRow>> result = new ExecuteResult<Pager<SqlRow>>();
		List<SqlRow> serviceGroups = serviceGroupDao.queryServiceGroupSqlrowPagerList(currentPage, pageSize, groupName
				, orderparam);
		int count = serviceGroupDao.queryGroupSqlrowCount(groupName);
		Pager<SqlRow> pager = new Pager<>();
		pager.setPageNum(currentPage);
		pager.setPageSize(pageSize);
		pager.setRecords(serviceGroups);
		pager.setTotalRecord(count);
		result.setResult(pager);
		return result;
	}


	/**
	 * 查询服务下的镜像
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/7
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<QueryImageAO>> queryImagesByServiceId(String serviceId) {
		ExecuteResult<List<QueryImageAO>> result = new ExecuteResult<List<QueryImageAO>>();
		List<Image> images = imageDao.queryImages(serviceId);
		List<QueryImageAO> imageAOList = new ArrayList<QueryImageAO>();
		for (Image image : images) {
			QueryImageAO queryImageAO = new QueryImageAO();
			BeanUtils.copyProperties(image, queryImageAO);
			List<Release> releases = releaseDao.getReleasesByServiceIdAndImageId(serviceId, image.getId());
			List<ReleaseAO> releaseAOS = new ListUtil<Release, ReleaseAO>().copyList(releases,
					ReleaseAO.class);
			queryImageAO.setReleaseAO(releaseAOS);
			imageAOList.add(queryImageAO);
		}
		result.setResult(imageAOList);
		return result;
	}

	/**
	 * 查询服务下的镜像(分页)
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/7
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<Pager<QueryImageAO>> queryPageImagesByServiceId(int currentPage, int pageSize,
																		 String serviceId, String startDate,
																		 String endDate) throws ParseException {
		ExecuteResult<Pager<QueryImageAO>> result = new ExecuteResult<>();
		Pager<QueryImageAO> pager = new Pager<>();
		pager.setPageNum(currentPage);
		pager.setPageSize(pageSize);
		List<Image> images = imageDao.queryPageImages(currentPage, pageSize, serviceId, startDate, endDate);
		int imageCount = imageDao.queryPageImagesCount(serviceId, startDate, endDate);
		List<QueryImageAO> imageAOList = new ArrayList<QueryImageAO>();
		for (Image image : images) {
			QueryImageAO queryImageAO = new QueryImageAO();
			BeanUtils.copyProperties(image, queryImageAO);
			List<Release> releases = releaseDao.getReleasesByServiceIdAndImageId(serviceId, image.getId());
			List<ReleaseAO> releaseAOS = new ListUtil<Release, ReleaseAO>().copyList(releases,
					ReleaseAO.class);
			queryImageAO.setReleaseAO(releaseAOS);
			imageAOList.add(queryImageAO);
		}
		pager.setRecords(imageAOList);
		pager.setTotalRecord(imageCount);
		result.setResult(pager);
		return result;
	}

	/**
	 * 查询某个服务
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "service", level = "R")
	//由于此方入参很多，且查询的可能不止一个服务，因此无法使用自定义注解来进行权限控
	//与前台沟通后，此方法只进行单笔查询。那就可以使用自定义注解控制
	public ExecuteResult<List<QueryServiceAO>> queryServiceInfo(String serviceId, String serviceGroup,
																String serviceName) {
		ExecuteResult<List<QueryServiceAO>> result = new ExecuteResult<List<QueryServiceAO>>();
		List<QueryServiceAO> queryInfo = new ArrayList<QueryServiceAO>();
		List<com.ladeit.pojo.doo.Service> services = serviceDao.queryServiceListByParam(serviceId, serviceGroup,
				serviceName);
		for (com.ladeit.pojo.doo.Service service : services) {
			QueryServiceAO queryServiceAO = new QueryServiceAO();
			BeanUtils.copyProperties(service, queryServiceAO);
			Subject subject = org.apache.shiro.SecurityUtils.getSubject();
			User user = (User) SecurityUtils.getSubject().getPrincipal();
			UserServiceRelation userServiceRelation = userServiceRelationDao.getServiceRelation(user.getId(),
					service.getId());
			if (userServiceRelation != null) {
				queryServiceAO.setRoleNum(userServiceRelation.getRoleNum());
				//queryInfo.add(queryServiceAO);
			}
			//else if("admin".equals(user.getUsername())){
			queryInfo.add(queryServiceAO);
			//}
			//ExecuteResult<Release> re = releaseService.getInUseReleaseByServiceId(service.getId());
			//Release release = re.getResult();
			//ReleaseAO releaseAO= new ReleaseAO();
			//BeanUtils.copyProperties(release, releaseAO);
			//queryServiceAO.setRelease(releaseAO);
		}
		result.setResult(queryInfo);
		return result;
	}

	/**
	 * 更新镜像
	 *
	 * @param imageAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> updateImage(String imageId, ImageAO imageAO) {
		ExecuteResult<String> result = new ExecuteResult<String>();
		Image image = imageDao.getImageById(imageId);
		image.setVersion(imageAO.getVersion());
		imageDao.update(image);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 查询服务组下人员信息
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/11/25
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "group", level = "R")
	public ExecuteResult<Pager<ServiceGroupUserAO>> querySeriveGroupUserInfo(String serviceGroupId, int currentPage,
																			 int pageSize) {
		ExecuteResult<Pager<ServiceGroupUserAO>> result = new ExecuteResult<>();
		Pager<ServiceGroupUserAO> pager = new Pager<>();
		pager.setPageNum(currentPage);
		pager.setPageSize(pageSize);
		List<SqlRow> userList = userServiceGroupRelationDao.queryUsersByGroupId(currentPage, pageSize, serviceGroupId);
		int userCount = userServiceGroupRelationDao.getUserCount(serviceGroupId);
		List<ServiceGroupUserAO> resultList = new ArrayList<ServiceGroupUserAO>();
		for (SqlRow sqlRow : userList) {
			ServiceGroupUserAO serviceGroupUserAO = new ServiceGroupUserAO();
			serviceGroupUserAO.setId(sqlRow.getString("id"));
			serviceGroupUserAO.setUserId(sqlRow.getString("user_id"));
			serviceGroupUserAO.setUsername(sqlRow.getString("username"));
			serviceGroupUserAO.setAccessLevel(sqlRow.getString("access_level"));
			serviceGroupUserAO.setCreateAt(sqlRow.getTimestamp("create_at"));
			resultList.add(serviceGroupUserAO);
		}
		pager.setRecords(resultList);
		pager.setTotalRecord(userCount);
		result.setResult(pager);
		return result;
	}

	/**
	 * 查询服务组下人员信息(不分页)
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "group", level = "R")
	public ExecuteResult<List<ServiceGroupUserAO>> queryNoPagerSeriveGroupUserInfo(String serviceGroupId) {
		ExecuteResult<List<ServiceGroupUserAO>> result = new ExecuteResult<>();
		List<SqlRow> userList = userServiceGroupRelationDao.queryNopagerUsersByGroupId(serviceGroupId);
		List<ServiceGroupUserAO> resultList = new ArrayList<ServiceGroupUserAO>();
		for (SqlRow sqlRow : userList) {
			ServiceGroupUserAO serviceGroupUserAO = new ServiceGroupUserAO();
			serviceGroupUserAO.setId(sqlRow.getString("id"));
			serviceGroupUserAO.setUserId(sqlRow.getString("user_id"));
			serviceGroupUserAO.setUsername(sqlRow.getString("username"));
			serviceGroupUserAO.setAccessLevel(sqlRow.getString("access_level"));
			serviceGroupUserAO.setCreateAt(sqlRow.getTimestamp("create_at"));
			List<SqlRow> serviceUserList = userServiceRelationDao.queryNopagerUsersByGroupId(serviceGroupId,
					sqlRow.getString("user_id"));
			List<ServiceUserAO> resultUserList = new ArrayList<ServiceUserAO>();
			for (SqlRow user : serviceUserList) {
				ServiceUserAO serviceUserAO = new ServiceUserAO();
				serviceUserAO.setUserServiceReId(user.getString("id"));
				serviceUserAO.setServiceName(user.getString("name"));
				serviceUserAO.setRoleNum(user.getString("role_num"));
				serviceUserAO.setServiceId(user.getString("serviceid"));
				resultUserList.add(serviceUserAO);
			}
			serviceGroupUserAO.setServiceUsers(resultUserList);
			resultList.add(serviceGroupUserAO);
		}
		result.setResult(resultList);
		return result;
	}

	/**
	 * 查询人员在某服务组下各服务中的权限信息
	 *
	 * @param serviceGroupId,userId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/11/25
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<Pager<ServiceUserAO>> querySeriveUserInfo(int currentPage, int pageSize,
																String serviceGroupId, String userId) {
		ExecuteResult<Pager<ServiceUserAO>> result = new ExecuteResult<>();
		Pager<ServiceUserAO> pager = new Pager<>();
		pager.setPageNum(currentPage);
		pager.setPageSize(pageSize);
		List<SqlRow> userList = userServiceRelationDao.queryUsersByGroupId(currentPage, pageSize, serviceGroupId,
				userId);
		int userCount = userServiceRelationDao.getUserCount(serviceGroupId, userId);
		List<ServiceUserAO> resultList = new ArrayList<ServiceUserAO>();
		for (SqlRow sqlRow : userList) {
			ServiceUserAO serviceUserAO = new ServiceUserAO();
			serviceUserAO.setUserServiceReId(sqlRow.getString("id"));
			serviceUserAO.setServiceName(sqlRow.getString("name"));
			serviceUserAO.setRoleNum(sqlRow.getString("role_num"));
			serviceUserAO.setServiceId(sqlRow.getString("serviceid"));
			resultList.add(serviceUserAO);
		}
		pager.setRecords(resultList);
		pager.setTotalRecord(userCount);
		result.setResult(pager);
		return result;
	}


	/**
	 * 更新人员服务组权限信息
	 *
	 * @param userServiceGroupRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@Override
	@Transactional
	@Authority(type = "group", level = "W")
	public ExecuteResult<String> updateServiceGroupRelation(String groupId,
															UserServiceGroupRelationAO userServiceGroupRelationAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		String groupRelationId = userServiceGroupRelationAO.getId();
		if (groupRelationId != null) {
			UserServiceGroupRelation userServiceGroupRelation =
					userServiceGroupRelationDao.getGroupRelationById(groupRelationId);
			userServiceGroupRelation.setAccessLevel(userServiceGroupRelationAO.getAccessLevel());
			userServiceGroupRelationDao.update(userServiceGroupRelation);
			result.setResult(userServiceGroupRelation.getId());
		} else {
			UserServiceGroupRelation userServiceGroupRelation = new UserServiceGroupRelation();
			userServiceGroupRelation.setId(UUID.randomUUID().toString());
			userServiceGroupRelation.setAccessLevel(userServiceGroupRelationAO.getAccessLevel());
			userServiceGroupRelation.setCreateAt(new Date());
			userServiceGroupRelation.setServiceGroupId(userServiceGroupRelationAO.getServiceGroupId());
			userServiceGroupRelation.setUserId(userServiceGroupRelationAO.getUserId());
			userServiceGroupRelationDao.insert(userServiceGroupRelation);
			result.setResult(userServiceGroupRelation.getId());
		}
		return result;
	}

	/**
	 * 更新人员服务权限信息
	 *
	 * @param userServiceRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "group", level = "W")
	public ExecuteResult<String> updateServiceRelation(String serviceGroupId,
													UserServiceRelationAO userServiceRelationAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		String relationId = userServiceRelationAO.getId();
		if (relationId != null) {
			UserServiceRelation userServiceRelation = userServiceRelationDao.getServiceRelationById(relationId);
			userServiceRelation.setRoleNum(userServiceRelationAO.getRoleNum());
			userServiceRelationDao.update(userServiceRelation);
			result.setResult(userServiceRelation.getId());
		} else {
			UserServiceRelation userServiceRelation = new UserServiceRelation();
			userServiceRelation.setId(UUID.randomUUID().toString());
			//userServiceRelation.setAccessLevel(userServiceRelationAO.getAccessLevel());
			userServiceRelation.setServiceId(userServiceRelationAO.getServiceId());
			userServiceRelation.setUserId(userServiceRelationAO.getUserId());
			userServiceRelation.setRoleNum(userServiceRelationAO.getRoleNum());
			userServiceRelationDao.insert(userServiceRelation);
			result.setResult(userServiceRelation.getId());
		}

		//更新人员服务权限关系的时候，关联维护服务所在命名空间的权限，只新增不删除，判断已存在，则不作处理
		com.ladeit.pojo.doo.Service service = serviceDao.getById(userServiceRelationAO.getServiceId());
		UserClusterRelation userClusterRelationold =
				userClusterRelationDao.queryByClusterIdAndUserId(service.getClusterId(),
						userServiceRelationAO.getUserId());
		if (userClusterRelationold == null) {
			UserClusterRelation userClusterRelation = new UserClusterRelation();
			userClusterRelation.setId(UUID.randomUUID().toString());
			userClusterRelation.setAccessLevel("R");
			userClusterRelation.setClusterId(service.getClusterId());
			userClusterRelation.setUserId(userServiceRelationAO.getUserId());
			userClusterRelation.setCreateAt(new Date());
			userClusterRelationDao.insert(userClusterRelation);
		}
		UserEnvRelation userEnvRelationold = userEnvRelationDao.queryByEnvIdAndUserId(service.getEnvId(),
				userServiceRelationAO.getUserId());
		if (userEnvRelationold == null) {
			UserEnvRelation userEnvRelation = new UserEnvRelation();
			userEnvRelation.setId(UUID.randomUUID().toString());
			userEnvRelation.setAccessLevel("R");
			userEnvRelation.setClusterId(service.getClusterId());
			userEnvRelation.setCreateAt(new Date());
			userEnvRelation.setEnvId(service.getEnvId());
			userEnvRelation.setUserId(userServiceRelationAO.getUserId());
			userEnvRelationDao.insert(userEnvRelation);
		}
		return result;
	}

	/**
	 * 查询要添加的用户信息
	 *
	 * @param serviceGroupId, userName, email
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.AddServiceGroupUserAO>>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<AddServiceGroupUserAO>> queryAddSeriveGroupUserInfo(String serviceGroupId,
																				  String userName, String email) {
		ExecuteResult<List<AddServiceGroupUserAO>> result = new ExecuteResult<>();
		List<SqlRow> users = userDao.getUserByUserNameOrEmail(userName, email);
		List<AddServiceGroupUserAO> userAOList = new ArrayList<>();
		for (SqlRow user : users) {
			UserServiceGroupRelation groupRelation = userServiceGroupRelationDao.getGrouprelation(user.getString("id")
					, serviceGroupId);
			AddServiceGroupUserAO userAO = new AddServiceGroupUserAO();
			userAO.setUserId(user.getString("id"));
			userAO.setUserName(user.getString("username"));
			if (groupRelation == null) {
				userAO.setAddflag(true);
			} else {
				userAO.setAddflag(false);
			}
			userAOList.add(userAO);
		}
		result.setResult(userAOList);
		return result;
	}

	/**
	 * 往组内添加人员
	 *
	 * @param userServiceGroupRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "group", level = "W")
	public ExecuteResult<String> addServiceGroupRelation(String groupId,
														 UserServiceGroupRelationAO userServiceGroupRelationAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		UserServiceGroupRelation userServiceGroupRelation = new UserServiceGroupRelation();
		BeanUtils.copyProperties(userServiceGroupRelationAO, userServiceGroupRelation);
		userServiceGroupRelation.setId(UUID.randomUUID().toString());
		userServiceGroupRelation.setCreateAt(new Date());
		userServiceGroupRelationDao.insert(userServiceGroupRelation);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		//新人进组，添加组内消息
		insertJoinGroupMessage(userServiceGroupRelationAO.getUserId(), userServiceGroupRelationAO.getServiceGroupId(),
				user.getId());
		//添加服务组人员的同时，添加服务组所有服务与被添加人的关联关系
		//addUserServiceRelation(userServiceGroupRelationAO.getServiceGroupId(),userServiceGroupRelationAO.getUserId
		// ());
		//添加服务组人员的同时，添加服务组所有服务所在环境与被添加人的关联关系
		//addUserEnvRelation(userServiceGroupRelationAO.getServiceGroupId(),userServiceGroupRelationAO.getUserId());
		return result;
	}

	/**
	 * 往组内添加人员(多个)
	 *
	 * @param userServiceGroupRelationListAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "group", level = "W")
	public ExecuteResult<String> addServiceGroupRelationList(String groupId,
															 List<UserServiceGroupRelationAO> userServiceGroupRelationListAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		//List<UserServiceGroupRelationAO> list = userServiceGroupRelationListAO.getRelationAOList();
		for (UserServiceGroupRelationAO userServiceGroupRelationAO : userServiceGroupRelationListAO) {
			UserServiceGroupRelation userServiceGroupRelation = new UserServiceGroupRelation();
			BeanUtils.copyProperties(userServiceGroupRelationAO, userServiceGroupRelation);
			userServiceGroupRelation.setId(UUID.randomUUID().toString());
			userServiceGroupRelation.setCreateAt(new Date());
			userServiceGroupRelationDao.insert(userServiceGroupRelation);

			User user = (User) SecurityUtils.getSubject().getPrincipal();
			//新人进组，添加组内消息
			insertJoinGroupMessage(userServiceGroupRelationAO.getUserId(),
					userServiceGroupRelationAO.getServiceGroupId(), user.getId());
			//添加服务组人员的同时，添加服务组所有服务与被添加人的关联关系
			//addUserServiceRelation(userServiceGroupRelationAO.getServiceGroupId(),userServiceGroupRelationAO
			// .getUserId());
			//添加服务组人员的同时，添加服务组所有服务所在环境与被添加人的关联关系
			//addUserEnvRelation(userServiceGroupRelationAO.getServiceGroupId(),userServiceGroupRelationAO.getUserId
			// ());
		}
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 添加服务组人员(通过邀请码)
	 *
	 * @param inviteCode
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/6
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> addServiceGroupRelationByInviteCode(String inviteCode) {
		ExecuteResult<String> result = new ExecuteResult<>();
		ServiceGroup serviceGroup = serviceGroupDao.queryServiceByInviteCode(inviteCode);
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		UserServiceGroupRelation serviceGroupRelation = userServiceGroupRelationDao.getGrouprelation(user.getId(),
				serviceGroup.getId());
		if (serviceGroupRelation != null) {
			String message = messageUtils.matchMessage("M0024", new Object[]{}, Boolean.TRUE);
			result.setResult(message);
		} else {
			UserServiceGroupRelation userServiceGroupRelation = new UserServiceGroupRelation();
			userServiceGroupRelation.setId(UUID.randomUUID().toString());
			userServiceGroupRelation.setUserId(user.getId());
			userServiceGroupRelation.setServiceGroupId(serviceGroup.getId());
			userServiceGroupRelation.setAccessLevel("R");
			userServiceGroupRelation.setCreateAt(new Date());
			userServiceGroupRelationDao.insert(userServiceGroupRelation);
			String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
			result.setResult(message);
			//新人进组，添加组内消息
			insertJoinGroupMessage(user.getId(), serviceGroup.getId(), user.getId());
			//添加服务组人员的同时，添加服务组所有服务与被添加人的关联关系
			//addUserServiceRelation(serviceGroup.getId(),user.getId());
			//添加服务组人员的同时，添加服务组所有服务所在环境与被添加人的关联关系
			//addUserEnvRelation(serviceGroup.getId(),user.getId());
		}
		return result;
	}

	/**
	 * 新人进组消息添加
	 *
	 * @param userId, serviceGroupId, operUserId
	 * @return void
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	public void insertJoinGroupMessage(String userId, String serviceGroupId, String operUserId) {
		User user = userDao.getUserById(userId);
		ServiceGroup group = serviceGroupDao.queryServiceById(serviceGroupId);
		//消息创建
		Message message = new Message();
		message.setId(UUID.randomUUID().toString());
		// is invite to
		message.setTitle(user.getUsername() + " was invited to group " + group.getName());
		message.setContent(user.getUsername() + " was invited to group " + group.getName());
		message.setCreateAt(new Date());
		message.setType(CommonConsant.MESSAGE_TYPE_5);
		//message.setTargetId(userId);
		message.setOperuserId(operUserId);
		message.setLevel("NORMAL");
		message.setServiceGroupId(serviceGroupId);
		message.setTarget_user(userId);
		message.setMessageType(CommonConsant.MESSAGE_TYPE_R);
		messageService.insertMessage(message, true);
	}

	/**
	 * 添加服务组所有服务与被添加人的权限关系
	 *
	 * @param serviceGroupId, userId
	 * @return void
	 * @date 2020/2/1
	 * @ahthor MddandPyy
	 */
	public void addUserServiceRelation(String serviceGroupId, String userId) {
		List<com.ladeit.pojo.doo.Service> services = serviceDao.getServiceByGroupId(serviceGroupId);
		for (com.ladeit.pojo.doo.Service service : services) {
			UserServiceRelation userServiceRelation = userServiceRelationDao.getServiceRelation(userId,
					service.getId());
			if (userServiceRelation == null) {
				UserServiceRelation u = new UserServiceRelation();
				u.setId(UUID.randomUUID().toString());
				u.setRoleNum("R");
				u.setServiceId(service.getId());
				u.setUserId(userId);
				userServiceRelationDao.insert(u);
			}
		}
	}


	/**
	 * 添加服务组所在环境的人员关联关系
	 *
	 * @param serviceGroupId, userId
	 * @return void
	 * @date 2020/2/1
	 * @ahthor MddandPyy
	 */
	public void addUserEnvRelation(String serviceGroupId, String userId) {
		ServiceGroup serviceGroup = serviceGroupDao.queryServiceById(serviceGroupId);
		UserClusterRelation userClusterRelationold =
				userClusterRelationDao.queryByClusterIdAndUserId(serviceGroup.getClusterId(), userId);
		if (userClusterRelationold == null) {
			UserClusterRelation userClusterRelation = new UserClusterRelation();
			userClusterRelation.setId(UUID.randomUUID().toString());
			userClusterRelation.setAccessLevel("R");
			userClusterRelation.setClusterId(serviceGroup.getClusterId());
			userClusterRelation.setUserId(userId);
			userClusterRelation.setCreateAt(new Date());
			userClusterRelationDao.insert(userClusterRelation);
		}
		List<com.ladeit.pojo.doo.Service> services = serviceDao.getServiceByGroupId(serviceGroupId);
		for (com.ladeit.pojo.doo.Service service : services) {
			UserEnvRelation userEnvRelationold = userEnvRelationDao.queryByEnvIdAndUserId(service.getEnvId(), userId);
			if (userEnvRelationold == null) {
				UserEnvRelation userEnvRelation = new UserEnvRelation();
				userEnvRelation.setId(UUID.randomUUID().toString());
				userEnvRelation.setAccessLevel("R");
				userEnvRelation.setClusterId(serviceGroup.getClusterId());
				userEnvRelation.setCreateAt(new Date());
				userEnvRelation.setEnvId(serviceGroup.getEnvId());
				userEnvRelation.setUserId(userId);
				userEnvRelationDao.insert(userEnvRelation);
			}
		}
	}


	/**
	 * 删除组内人员
	 *
	 * @param userServiceGroupRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	@Override
	@Transactional
	@Authority(type = "group", level = "W")
	public ExecuteResult<String> deleteServiceGroupRelation(String groupId,
															UserServiceGroupRelationAO userServiceGroupRelationAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		UserServiceGroupRelation userServiceGroupRelation =
				userServiceGroupRelationDao.getGroupRelationById(userServiceGroupRelationAO.getId());

		User user = (User) SecurityUtils.getSubject().getPrincipal();
		//某人移除某组消息添加
		insertRemoveGroupMessage(userServiceGroupRelation.getUserId(), userServiceGroupRelation.getServiceGroupId(),
				user.getId());
		//查询该组下的服务
		List<com.ladeit.pojo.doo.Service> serviceList =
				serviceDao.queryServiceList(userServiceGroupRelation.getServiceGroupId());
		for (com.ladeit.pojo.doo.Service service : serviceList) {
			UserServiceRelation userServiceRelation =
					userServiceRelationDao.getServiceRelation(userServiceGroupRelation.getUserId(), service.getId());
			if (userServiceRelation != null) {
				userServiceRelationDao.delete(userServiceRelation);
			}
		}


		//删除服务组所在环境的人员关联关系
		//deleteUserEnvRelation(userServiceGroupRelation.getServiceGroupId(),userServiceGroupRelation.getUserId());

		userServiceGroupRelationDao.delete(userServiceGroupRelation);
		// Successfully.
		//result.setResult("删除人员成功");
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 当前登录人离开分组
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/1/2
	 * @ahthor MddandPyy
	 */
	@Override
	@Transactional
	public ExecuteResult<String> leaveServiceGroup(String serviceGroupId) {
		ExecuteResult<String> result = new ExecuteResult<>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		String userId = user.getId();
		//某人移除某组消息添加
		insertRemoveGroupMessage(userId, serviceGroupId, userId);

		UserServiceGroupRelation userServiceGroupRelation =
				userServiceGroupRelationDao.getGrouprelation(userId, serviceGroupId);
		//查询该组下的服务
		List<com.ladeit.pojo.doo.Service> serviceList =
				serviceDao.queryServiceList(serviceGroupId);
		for (com.ladeit.pojo.doo.Service service : serviceList) {
			UserServiceRelation userServiceRelation =
					userServiceRelationDao.getServiceRelation(userId, service.getId());
			if (userServiceRelation != null) {
				userServiceRelationDao.delete(userServiceRelation);
			}

		}

		//删除服务组所在环境的人员关联关系
		//deleteUserEnvRelation(userServiceGroupRelation.getServiceGroupId(),userServiceGroupRelation.getUserId());

		userServiceGroupRelationDao.delete(userServiceGroupRelation);
		// Successfully.
		//result.setResult("操作成功");
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}


	/**
	 * 某人移除某组消息添加
	 *
	 * @param userId, serviceGroupId, operUserId
	 * @return void
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	public void insertRemoveGroupMessage(String userId, String serviceGroupId, String operUserId) {
		User user = userDao.getUserById(userId);
		ServiceGroup group = serviceGroupDao.queryServiceById(serviceGroupId);
		//消息创建
		Message message = new Message();
		message.setId(UUID.randomUUID().toString());
		// is removed from
		message.setTitle(user.getUsername() + " was removed from group " + group.getName());
		message.setContent(user.getUsername() + " was removed from group " + group.getName());
		message.setCreateAt(new Date());
		message.setType(CommonConsant.MESSAGE_TYPE_6);
		//message.setTargetId(userId);
		message.setOperuserId(operUserId);
		message.setLevel("NORMAL");
		message.setServiceGroupId(serviceGroupId);
		message.setTarget_user(userId);
		message.setMessageType(CommonConsant.MESSAGE_TYPE_R);
		messageService.insertMessage(message, true);
	}


	/**
	 * 删除服务组所在环境的人员关联关系
	 *
	 * @param serviceGroupId, userId
	 * @return void
	 * @date 2020/2/1
	 * @ahthor MddandPyy
	 */
	public void deleteUserEnvRelation(String serviceGroupId, String userId) {
		ServiceGroup serviceGroup = serviceGroupDao.queryServiceById(serviceGroupId);
		UserEnvRelation userEnvRelation = userEnvRelationDao.queryByEnvIdAndUserId(serviceGroup.getEnvId(), userId);
		userEnvRelationDao.delete(userEnvRelation);
		List<UserEnvRelation> list = userEnvRelationDao.queryByClusterId(serviceGroup.getClusterId(), userId);
		if (list.size() == 0) {
			UserClusterRelation userClusterRelation =
					userClusterRelationDao.queryByClusterIdAndUserId(serviceGroup.getClusterId(), userId);
			if (userClusterRelation != null) {
				userClusterRelationDao.delete(userClusterRelation);
			}
		}
	}

	/**
	 * 查询服务组token
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.CertificateAO>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<CertificateAO> getGroupToken(String serviceGroupId) {
		ExecuteResult<CertificateAO> result = new ExecuteResult<CertificateAO>();
		CertificateAO certificateAO = new CertificateAO();
		Certificate certificate = certificateDao.queryCertificateByGroupId(serviceGroupId);
		if (certificate != null) {
			BeanUtils.copyProperties(certificate, certificateAO);
		}
		result.setResult(certificateAO);
		return result;
	}

	/**
	 * 更新分组token
	 *
	 * @param groupId
	 * @param certificateAO
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.CertificateAO>
	 * @author MddandPyy
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	@Transactional
	@Authority(type = "group", level = "W")
	public ExecuteResult<CertificateAO> updateGroupToken(String groupId, CertificateAO certificateAO) {
		ExecuteResult<CertificateAO> result = new ExecuteResult<CertificateAO>();
		Certificate certificate = certificateDao.queryCertificateByGroupId(groupId);
		ServiceGroup serviceGroup = serviceGroupDao.queryServiceById(groupId);
		//添加服务组token记录
		String token = null;
		try {
			token = TokenUtil.createToken(serviceGroup.getName() + System.currentTimeMillis());
		} catch (Exception e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0021", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		if (certificate != null) {
			certificate.setContent(token);
			certificateDao.update(certificate);
			BeanUtils.copyProperties(certificate, certificateAO);
		} else {
			User user = (User) SecurityUtils.getSubject().getPrincipal();
			Certificate c = new Certificate();
			c.setId(UUID.randomUUID().toString());
			c.setServiceGroupId(groupId);
			c.setContent(token);
			c.setCreateBy(user.getUsername());
			c.setCreateById(user.getId());
			c.setCreateAt(new Date());
			certificateDao.insert(c);
			BeanUtils.copyProperties(c, certificateAO);
		}
		result.setResult(certificateAO);
		return result;
	}

	/**
	 * 查询服务组邀请码信息
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> inviteUser(String serviceGroupId) {
		ExecuteResult<String> result = new ExecuteResult<String>();
		ServiceGroup serviceGroup = serviceGroupDao.queryServiceById(serviceGroupId);
		result.setResult(serviceGroup.getInviteCode());
		return result;
	}

	/**
	 * 更新服务组名字
	 *
	 * @param serviceGroupAO
	 * @return
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "group", level = "W")
	public ExecuteResult<String> updateGroupName(String groupId, ServiceGroupAO serviceGroupAO) {
		ExecuteResult<String> result = new ExecuteResult<String>();
		ServiceGroup serviceGroup = serviceGroupDao.queryServiceById(serviceGroupAO.getId());
		serviceGroup.setName(serviceGroupAO.getName());
		serviceGroupDao.update(serviceGroup);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 查询服务组绑定的slack channel信息
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ChannelServiceGroupAO>
	 * @date 2020/1/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<ChannelServiceGroupAO>> getChannelInfo(String serviceGroupId) {
		ExecuteResult<List<ChannelServiceGroupAO>> result = new ExecuteResult<List<ChannelServiceGroupAO>>();
		List<ChannelServiceGroup> channelServiceGroup =
				channelServiceGroupDao.queryChannelByServiceGroupId(serviceGroupId);
		if (channelServiceGroup.size() != 0) {
			List<ChannelServiceGroupAO> channelServiceGroupAOS = new ListUtil<ChannelServiceGroup,
					ChannelServiceGroupAO>().copyList(channelServiceGroup,
					ChannelServiceGroupAO.class);
			result.setResult(channelServiceGroupAOS);
		} else {
			result.setResult(null);
		}
		return result;
	}

	/**
	 * 解绑serviceGroup和channel
	 *
	 * @param channelServiceGroupId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/1/14
	 * @ahthor MddandPyy
	 */
	@Override
	@Transactional
	public ExecuteResult<String> unbindChannel(String channelServiceGroupId) {
		ExecuteResult<String> result = new ExecuteResult<String>();
		ChannelServiceGroup channelServiceGroup = channelServiceGroupDao.queryChannelById(channelServiceGroupId);
		//消息创建
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		Message message = new Message();
		message.setId(UUID.randomUUID().toString());
		// bot unbind.
		message.setTitle(channelServiceGroup.getServicegroupName() + " bot unbind.");
		message.setContent(channelServiceGroup.getServicegroupName() + " bot unbind.");
		message.setCreateAt(new Date());
		message.setType(CommonConsant.MESSAGE_TYPE_8);
		message.setOperuserId(user.getId());
		message.setLevel("NORMAL");
		message.setServiceGroupId(channelServiceGroup.getServicegroupId());
		message.setMessageType(CommonConsant.MESSAGE_TYPE_R);
		messageService.insertMessage(message, true);

		Map<String, Object> param = new HashMap<>();
		String userId = user.getId();
		param.put("operUserId", userId);
		param.put("operUserName", user.getUsername());
		param.put("servicegroupName", channelServiceGroup.getServicegroupName());
		param.put("channelId", channelServiceGroup.getChannelId());
		message.setParams(param);
		messageService.insertSlackMessage(message);

		channelServiceGroupDao.delete(channelServiceGroup);
		String messagestr = messageUtils.matchMessage("M0025", new Object[]{}, Boolean.TRUE);
		result.setResult(messagestr);
		return result;
	}

	@Override
	public String getServiceGroupId(String serviceId) {
		com.ladeit.pojo.doo.Service service = serviceDao.getById(serviceId);
		return service.getServiceGroupId();
	}

	/**
	 * 根据id查询group
	 *
	 * @param id
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.ServiceGroup>
	 * @author falcomlife
	 * @date 20-3-20
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<ServiceGroup> getGroupById(String id) {
		ExecuteResult<ServiceGroup> result = new ExecuteResult<>();
		ServiceGroup serviceGroup = this.serviceGroupDao.getGroupById(id);
		result.setResult(serviceGroup);
		return result;
	}

	/**
	 * 查询某服务组下面的服务信息
	 *
	 * @param channelId
	 * @return com.ladeitbot.core.ladeitbotcore.common.ExecuteResult<java.util.List<com.ladeitbot.pojo.ladeitbotpojo.ao.ServiceAO>>
	 * @date 2019/12/25
	 * @ahthor MddandPyy
	 */
	@Override
	@SlackLogin
	public ExecuteResult<BotQueryServiceAO> queryServiceGroupInfoBychannel(String slackUserId, String token,
																		String channelId) {
		ExecuteResult<BotQueryServiceAO> result = new ExecuteResult<BotQueryServiceAO>();

		BotQueryServiceAO botQueryServiceAO = new BotQueryServiceAO();
		List<ChannelServiceGroup> channelServiceGroups =
				channelServiceGroupDao.queryInfoByGroupNameAndChannelId(channelId);
		if (channelServiceGroups.size() == 0) {
			botQueryServiceAO.setFlag(false);
			// There was no bound group of current channel, please use '/ladeit bind [groupname]' to bind.
			//botQueryServiceAO.setMessage("当前channel没有绑定group，请使用 `/ladeit bind [groupname]` 进行绑定。");
			botQueryServiceAO.setMessage("There was no bound group of current channel, please use `/ladeit bind " +
					"[groupname]` to bind.");
		} else if (channelServiceGroups.size() > 1) {
			botQueryServiceAO.setFlag(false);
			// There are multiple groups bind on this channel, please checkout.
			//botQueryServiceAO.setMessage("当前channel绑定的group有多个，请查找原因。");
			botQueryServiceAO.setMessage("There are multiple groups bound this channel, please checkout.");
		} else {
			for (ChannelServiceGroup info : channelServiceGroups) {
				ServiceGroup group = serviceGroupDao.queryServiceById(info.getServicegroupId());
				List<com.ladeit.pojo.doo.Service> list =
						serviceDao.queryServiceListByGroupId(info.getServicegroupId());
				result = ((ServiceGroupServiceImpl) AopContext.currentProxy()).queryServiceSlack(group.getId(), group,
						botQueryServiceAO, list);
			}
		}
		result.setResult(botQueryServiceAO);
		return result;
	}

	@Authority(type = "group", level = "R")
	public ExecuteResult<BotQueryServiceAO> queryServiceSlack(String groupId, ServiceGroup group,
															  BotQueryServiceAO botQueryServiceAO,
															  List<com.ladeit.pojo.doo.Service> list) {
//				List<ServiceAO> serviceAOS = new ListUtil<com.ladeit.pojo.doo.Service, ServiceAO>().copyList(list,
//						ServiceAO.class);
		ExecuteResult<BotQueryServiceAO> result = new ExecuteResult<BotQueryServiceAO>();
		List<ServiceAO> serviceAOS = new ArrayList<>();
		User user = SecurityUtils.getSubject() == null ? null : (User) SecurityUtils.getSubject().getPrincipal();
		for (com.ladeit.pojo.doo.Service service : list) {
			if ("admin".equals(user.getUsername())) {
				ServiceAO serviceAO = new ServiceAO();
				BeanUtils.copyProperties(service, serviceAO);
				serviceAOS.add(serviceAO);
			} else {
				UserServiceRelation userServiceRelation = userServiceRelationDao.getServiceRelation(user.getId(),
						service.getId());
				if (userServiceRelation != null && userServiceRelation.getRoleNum().contains("R")) {
					ServiceAO serviceAO = new ServiceAO();
					BeanUtils.copyProperties(service, serviceAO);
					serviceAOS.add(serviceAO);
				}
			}
		}
		botQueryServiceAO.setFlag(true);
		botQueryServiceAO.setGroupId(group.getId());
		botQueryServiceAO.setGroupName(group.getName());
		botQueryServiceAO.setServiceAOs(serviceAOS);
		result.setResult(botQueryServiceAO);
		return result;
	}


	/**
	 * 加入serviceGroup
	 *
	 * @param joinServiceGroupAO
	 * @return com.ladeitbot.core.ladeitbotcore.common.ExecuteResult<com.ladeitbot.pojo.ladeitbotpojo.ao.ResultAO>
	 * @date 2020/1/8
	 * @ahthor MddandPyy
	 */
	@Override
	@SlackLogin
	public ExecuteResult<ResultAO> joinServiceGroup(String slackUserId, String token,
													JoinServiceGroupAO joinServiceGroupAO) {
		ExecuteResult<ResultAO> result = new ExecuteResult<ResultAO>();
		ResultAO resultAO = new ResultAO();
		//String slackUserId = joinServiceGroupAO.getSlackUserId();
		String serviceGroupId = joinServiceGroupAO.getServiceGroupId();
		UserSlackRelation userSlackRelation = userSlackRelationDao.queryUserSlackRelationBySlackUserId(slackUserId);
		if (userSlackRelation != null) {
			ServiceGroup serviceGroup = serviceGroupDao.queryServiceById(serviceGroupId);
			if (serviceGroup != null) {
				UserServiceGroupRelation relation =
						userServiceGroupRelationDao.getGrouprelation(userSlackRelation.getUserId(), serviceGroupId);
				if (relation != null) {
					resultAO.setFlag(false);
					resultAO.setMessage("You were already a member of " + serviceGroup.getName() + ".");

				} else {
					UserServiceGroupRelation userServiceGroupRelation = new UserServiceGroupRelation();
					userServiceGroupRelation.setId(UUID.randomUUID().toString());
					userServiceGroupRelation.setCreateAt(new Date());
					userServiceGroupRelation.setUserId(userSlackRelation.getUserId());
					userServiceGroupRelation.setServiceGroupId(serviceGroupId);
					userServiceGroupRelation.setAccessLevel("R");
					userServiceGroupRelationDao.insert(userServiceGroupRelation);
					resultAO.setFlag(true);
					resultAO.setMessage("Join in " + serviceGroup.getName() + " successfully.");
				}
			} else {
				resultAO.setFlag(false);
				resultAO.setMessage("There is no such a group.");
			}

		} else {
			resultAO.setFlag(false);
			resultAO.setMessage("You have not bound your Ladeit account yet, please use `/ladeit setup` to bind.");
		}
		result.setResult(resultAO);
		return result;
	}

	/**
	 * slack中查询某个服务下的镜像
	 *
	 * @param slackUserId, channelId, serviceName
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.BotQueryImageAO>
	 * @date 2020/3/30
	 * @ahthor MddandPyy
	 */
	@Override
	@SlackLogin
	public ExecuteResult<BotQueryImageAO> queryServiceImageInfo(String slackUserId, String token, String channelId,
																String serviceName) {
		ExecuteResult<BotQueryImageAO> result = new ExecuteResult<BotQueryImageAO>();
		BotQueryImageAO botQueryImageAO = new BotQueryImageAO();
		List<ChannelServiceGroup> channelServiceGroups =
				channelServiceGroupDao.queryInfoByGroupNameAndChannelId(channelId);
		if (channelServiceGroups.size() == 0) {
			botQueryImageAO.setFlag(false);
			botQueryImageAO.setMessage("There was no bound group of current channel, please use `/ladeit bind " +
					"[groupname]` to bind.");
		} else if (channelServiceGroups.size() > 1) {
			botQueryImageAO.setFlag(false);
			// There are multiple groups bind on this channel, please checkout.
			botQueryImageAO.setMessage("There are multiple groups bind on this channel, please checkout.");
		} else {
			for (ChannelServiceGroup info : channelServiceGroups) {
				ServiceGroup group = serviceGroupDao.queryServiceById(info.getServicegroupId());
				com.ladeit.pojo.doo.Service service = serviceDao.queryServiceByGroupAndName(group.getId(),
						serviceName);
				if (service != null) {
					result =
							((ServiceGroupServiceImpl) AopContext.currentProxy()).querySlackServiceImageInfo(service.getId(), service, botQueryImageAO, group);
				} else {
					botQueryImageAO.setFlag(false);
					botQueryImageAO.setMessage("There is no service named " + serviceName + " in group " + group.getName());
				}

			}
		}
		result.setResult(botQueryImageAO);
		return result;
	}

	@Authority(type = "service", level = "R")
	public ExecuteResult<BotQueryImageAO> querySlackServiceImageInfo(String serviceId,
																	 com.ladeit.pojo.doo.Service service,
																	 BotQueryImageAO botQueryImageAO,
																	 ServiceGroup group) {
		ExecuteResult<BotQueryImageAO> result = new ExecuteResult<BotQueryImageAO>();
		List<Image> images = imageDao.queryImages(service.getId());
		List<Image> imageslimit5 =
				images.stream().sorted(Comparator.comparing(Image::getCreateAt).reversed()).limit(5).collect(Collectors.toList());
		List<ImageAO> imageAOS = new ListUtil<Image, ImageAO>().copyList(imageslimit5,
				ImageAO.class);
		botQueryImageAO.setFlag(true);
		botQueryImageAO.setServiceId(service.getId());
		botQueryImageAO.setServiceName(service.getName());
		botQueryImageAO.setServiceGroupName(group.getName());
		botQueryImageAO.setImageAOs(imageAOS);
		result.setResult(botQueryImageAO);
		return result;
	}
}

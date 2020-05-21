package com.ladeit.biz.runner.events;

import com.alibaba.fastjson.JSONObject;
import com.google.common.reflect.TypeToken;
import com.ladeit.biz.config.SpringBean;
import com.ladeit.biz.dao.ImageDao;
import com.ladeit.biz.dao.ReleaseDao;
import com.ladeit.biz.manager.IstioManager;
import com.ladeit.biz.services.*;
import com.ladeit.biz.utils.CommonConsant;
import com.ladeit.biz.websocket.events.EventSub;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.ImageAO;
import com.ladeit.pojo.ao.TopologyAO;
import com.ladeit.pojo.ao.topology.Match;
import com.ladeit.pojo.ao.topology.Rule;
import com.ladeit.pojo.doo.*;
import com.ladeit.pojo.dto.CandidateDto;
import com.ladeit.pojo.dto.Event;
import com.ladeit.util.ListUtil;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.JSON;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import lombok.extern.slf4j.Slf4j;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @program: ladeit
 * @description: EventsSubcriber
 * @author: falcomlife
 * @create: 2020/04/02
 * @version: 1.0.0
 */
@Slf4j
public class EventsSubcriber {

	private String envId;
	private ApiClient apiClient;
	private CoreV1Api api;
	private EnvService envService;
	private ClusterService clusterService;
	private EventHandler eventHandler;
	private Watch<V1Event> watch;
	private Env env;
	private Cluster cluster;
	private String rev;
	private boolean stop;
	private RedisTemplate<String, Object> redisTemplate;
	private RedisTemplate<String, Object> pubSubRedisTemplate;
	private String config;
	private Message message;
	private Service service;

	public Watch<V1Event> getWatch() {
		return watch;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public EventsSubcriber(String envId, EventHandler eventHandler, String rev) {
		this.envId = envId;
		this.eventHandler = eventHandler;
		this.rev = rev;

	}

	/**
	 * 初始化准备
	 *
	 * @param
	 * @return void
	 * @author falcomlife
	 * @date 20-4-2
	 * @version 1.0.0
	 */
	public void init() throws IOException {
		config = this.dataInit();
		this.clientInit(config);
	}

	private String dataInit() {
		this.envService = SpringBean.getObject(EnvService.class);
		this.clusterService = SpringBean.getObject(ClusterService.class);
		this.redisTemplate = (RedisTemplate<String, Object>) SpringBean.getBean("eventRedisTemplate");
		this.pubSubRedisTemplate = (RedisTemplate<String, Object>) SpringBean.getBean("eventPubSubRedisTemplate");
		this.env = this.envService.getEnvById(this.envId);
		this.cluster = this.clusterService.getClusterById(env.getClusterId());
		this.message = new Message();
		this.service = new Service();
		return cluster.getK8sKubeconfig();
	}

	private void clientInit(String config) throws IOException {
		Reader reader = new StringReader(config);
		this.apiClient = Config.fromConfig(reader);
		OkHttpClient httpClient = apiClient.getHttpClient();
		httpClient.setReadTimeout(0, TimeUnit.SECONDS);
		httpClient.setWriteTimeout(0, TimeUnit.SECONDS);
		ConnectionPool connectionPool = new ConnectionPool(100, 1000 * 60 * 5, TimeUnit.MILLISECONDS);
		httpClient.setConnectionPool(connectionPool);
		Configuration.setDefaultApiClient(apiClient);
		this.api = new CoreV1Api();
	}


	/**
	 * 开始观测
	 *
	 * @param
	 * @return void
	 * @author falcomlife
	 * @date 20-4-2
	 * @version 1.0.0
	 */
	public void watch() {
		ThreadFactory threadPoolFactory = (ThreadFactory) SpringBean.getBean("threadPoolFactory");
		ResourceService resourceService = SpringBean.getObject(ResourceService.class);
		ServiceService serviceService = SpringBean.getObject(ServiceService.class);
		threadPoolFactory.newThread(() -> {
			String currentRev = null;
			Event event = new Event();
			try {
				this.watch =
						Watch.createWatch(
								this.apiClient,
								this.api.listNamespacedEventCall(
										env.getNamespace(), null, null, null, null, null, null, this.rev, null,
										Boolean.TRUE, null, null),
								new TypeToken<Watch.Response<V1Event>>() {
								}.getType());
				while (!stop) {
					Watch.Response<V1Event> item = watch.next();
					if (item != null) {
						V1Event v1event = item.object;
						if (!"Expired".equals(v1event.getReason())) {
							try {
								currentRev = v1event.getMetadata().getResourceVersion();
								String kind = v1event.getInvolvedObject().getKind();
								String uid = v1event.getInvolvedObject().getUid();
								ExecuteResult<JSONObject> result = resourceService.getResourceByUid(config,
										v1event.getInvolvedObject().getUid(), v1event.getInvolvedObject().getKind());
								switch (result.getCode()) {
									case Code.SUCCESS:
										JSONObject jo = result.getResult();
										JSONObject labels = jo.getJSONObject("metadata").getJSONObject(
												"labels");
										if (labels != null) {
											String serviceId = labels.getString("serviceId");
											ExecuteResult<Service> s = serviceService.getById(serviceId);
											if (s.getResult() == null) {
												continue;
											}
											log.info("verison" + currentRev + ",kind:" + v1event.getInvolvedObject().getKind() + "," + "namespace:" + v1event.getInvolvedObject().getNamespace() + ",name" + v1event.getInvolvedObject().getName() + ", reason:" + v1event.getReason() + ",message" + v1event.getMessage());
											// 在redis中存储半小时的历史event信息
											this.redisEventsHistory(event, v1event);
											// 执行生命周期检测，修改数据库信息
											Service service = this.releaseLifecycleMonitor(v1event, serviceId,
													s.getResult(), jo);
											s.getResult().setStatus(service.getStatus());
											// redis发布实时信息
											this.redisEventActual(event, s.getResult());
										} else {
											log.error("该资源没有serviceId的lables，可能不受ladeit管控");
										}
										break;
									case Code.NOTFOUND:
										log.error(kind + ":" + uid + "未找到");
										break;
									default:
										log.info(kind + ":" + uid + "出现异常");
								}
							} catch (Exception e) {
								// 业务部分出现问题不影响下面event的接受
								continue;
							}
						}
					}
				}
			} catch (Exception e) {
				// 如果接收后续events的时候报错，重新开启一个新的订阅者
				// TODO 不知道为什么该watch订阅者会自己关闭
				log.error("<<<<namespace:" + env.getNamespace() + " envId:" + envId + " eventUidId: " + event.getEventUid() + " error >>>>");
				try {
					if (!this.stop) {
						if (StringUtils.isNotBlank(currentRev)) {
							this.eventHandler.error(envId, null);
						} else {
							this.eventHandler.error(envId, null);
						}
					}
				} catch (IOException e1) {
					log.error(e.getMessage(), e1);
				} catch (ApiException e1) {
					log.error(e.getMessage(), e1);
				}
			} finally {
				try {
					watch.close();
					this.stop = true;
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}).start();
	}

	/**
	 * 在redis中存储半小时的历史event信息
	 *
	 * @param event
	 * @param v1event
	 * @return void
	 * @author falcomlife
	 * @date 20-4-29
	 * @version 1.0.0
	 */
	private void redisEventsHistory(Event event, V1Event v1event) {
		long time = System.currentTimeMillis();
		event.setClusterId(cluster.getId());
		event.setEnvId(envId);
		event.setEventUid(v1event.getMetadata().getUid());
		event.setResourceUid(v1event.getInvolvedObject().getUid());
		event.setKind(v1event.getInvolvedObject().getKind());
		event.setName(v1event.getInvolvedObject().getName());
		event.setNamespace(v1event.getMetadata().getNamespace());
		event.setMessage(v1event.getMessage());
		event.setNote(v1event.getMessage());
		event.setReason(v1event.getReason());
		event.setType(v1event.getType());
		event.setStartTime(v1event.getFirstTimestamp().toDate());
		event.setEndTime(v1event.getLastTimestamp().toDate());
		redisTemplate.boundZSetOps("head:" + envId).add(event, time);
		redisTemplate.boundZSetOps("head:" + envId).expire(30, TimeUnit.MINUTES);
		redisTemplate.boundZSetOps("notice:" + envId).add(event, time);
		redisTemplate.boundZSetOps("notice:" + envId).expire(30, TimeUnit.MINUTES);
		redisTemplate.boundZSetOps("service:" + envId).add(event, time);
		redisTemplate.boundZSetOps("service:" + envId).expire(30,
				TimeUnit.MINUTES);
		redisTemplate.boundZSetOps("lifecycle:" + envId).add(event, time);
		redisTemplate.boundZSetOps("lifecycle:" + envId).expire(30,
				TimeUnit.MINUTES);
	}

	/**
	 * redis发布实时信息
	 *
	 * @param event
	 * @param s
	 * @return void
	 * @author falcomlife
	 * @date 20-4-29
	 * @version 1.0.0
	 */
	private void redisEventActual(Event event, Service s) {
		ImageService imageService = SpringBean.getObject(ImageService.class);
		ExecuteResult<List<Image>> i = imageService.getImageByServiceId(s.getId());
		EventSub eventSub = new EventSub();
		BeanUtils.copyProperties(event, eventSub);
		eventSub.setResourceName(eventSub.getName());
		eventSub.setName(s.getName());
		eventSub.setServiceId(s.getId());
		eventSub.setStatus(Integer.parseInt(s.getStatus()));
		if (i.getCode() == Code.SUCCESS) {
			eventSub.setImageAOS(new ListUtil<Image, ImageAO>().copyList(i.getResult(), ImageAO.class));
			eventSub.setImageId(s.getImageId());
			eventSub.setImageVersion(s.getImageVersion());
			eventSub.setImagenum(i.getResult().size());
		}
		redisTemplate.convertAndSend("event:topic:" + s.getId(), JSONObject.toJSONString(eventSub));
	}

	/**
	 * release生命周期监控，修改service，release，candidate状态
	 *
	 * @param event
	 * @return void
	 * @author falcomlife
	 * @date 20-4-3
	 * @version 1.0.0
	 */
	private Service releaseLifecycleMonitor(V1Event event, String serviceId, Service s, JSONObject jo) throws ApiException, IOException {
		Service result = new Service();
		// warning类型的视作服务存在问题，同步更新service状态
		ResourceService resourceService = SpringBean.getObject(ResourceService.class);
		if ("Warning".equals(event.getType())) {
			String kind = event.getInvolvedObject().getKind();
			String uid = event.getInvolvedObject().getUid();
			ExecuteResult<JSONObject> ownerRes = resourceService.getResourceByUid(config, uid, kind);
			if (ownerRes.getCode() != Code.SUCCESS) {
				log.info("未找到资源:" + event.getInvolvedObject().getKind() + "," + event.getInvolvedObject().getUid());
				return result;
			}
			JSONObject owner = ownerRes.getResult();
			JSONObject status = owner.getJSONObject("status");
			Integer desireReplicas = owner.getJSONObject("spec").getInteger("replicas");
			if ("Pod".equals(kind) && "spec.containers{istio-proxy}".equals(event.getInvolvedObject().getFieldPath())) {
				log.info("istio组件报错不进行处理:" + event.getMessage());
			} else if ("Pod".equals(kind)) {
				// 如果pod出现异常，一般是replicaset已经成功扩展出了新的pod，但是pod启动异常，此时服务未达到希望达到的运行数量，系统认为是异常状态。
				this.warningBussiness(event, serviceId, s);
				result.setStatus("8");
			} else if (("ReplicaSet".equals(kind) || "Deployment".equals(kind) || "StatefulSet".equals(kind)) && (status.getInteger("replicas") != null && !status.getInteger(
					"readyReplicas").equals(desireReplicas))) {
				// 如果replicaset或者deployment发生异常event，而且他们的希望值和准备好值不相等，被认为是没有达到希望的运行数量，系统认为是异常状态
				this.warningBussiness(event, serviceId, s);
				result.setStatus("8");
			} else {
				log.info("资源" + event.getInvolvedObject().getKind() + "," + event.getInvolvedObject().getUid() +
						"状态：replicas " + status.getInteger("replicas") + ",readyReplicas " + status.getInteger(
						"readyReplicas"));
				result.setStatus(s.getStatus());
			}
		} else if ("Normal".equals(event.getType())) {
			String kind = event.getInvolvedObject().getKind();
			if ("Pod".equals(kind) && "Started".equals(event.getReason()) && !"istio-proxy".equals(event.getInvolvedObject().getName()) && !"istio-init".equals(event.getInvolvedObject().getName())) {
//				if (jo.getJSONObject("metadata").getJSONArray("ownerReferences") != null) {
//					String parentUid =
//							((JSONObject) (jo.getJSONObject("metadata").getJSONArray("ownerReferences").get(0))).getString("uid");
//					String parentKind =
//							((JSONObject) (jo.getJSONObject("metadata").getJSONArray("ownerReferences").get(0))).getString("kind");
//					ExecuteResult<JSONObject> parentRes = resourceService.getResourceByUid(config, parentUid,
//							parentKind);
//					JSONObject parent = parentRes.getResult();
//					if (parent != null) {
//						Integer replicas = parent.getJSONObject("spec").getInteger("replicas");
//						Integer newReplicas = parent.getJSONObject("status").getInteger("replicas");
//						if (replicas != null && replicas.equals(newReplicas)) {
							this.releaseLifecycleMonitor(jo);
							result.setStatus("0");
//						}
//					}
//				}
			} else if ("ReplicaSet".equals(kind) || "Deployment".equals(kind) || "StatefulSet".equals(kind)) {
				Integer replicas = jo.getJSONObject("spec").getInteger("replicas");
				Integer readyReplicas = jo.getJSONObject("status").getInteger("readyReplicas");
				if (replicas != null && replicas.equals(readyReplicas)) {
					this.updateService(serviceId, "0");
					result.setStatus("0");
				}
			}
		}
		return result;
	}

	private void updateService(String serviceId, String status) {
		ServiceService serviceService = SpringBean.getObject(ServiceService.class);
		this.service.setStatus(status);
		this.service.setId(serviceId);
		serviceService.updateStatusById(this.service);
	}

	@Transactional
	public void warningBussiness(V1Event event, String serviceId, Service s) throws IOException {

		ServiceGroupService serviceGroupService = SpringBean.getObject(ServiceGroupService.class);
		MessageService messageService = SpringBean.getObject(MessageService.class);
		// 如果service已经被删除了，这里就不用再执行逻辑了
		ExecuteResult<ServiceGroup> sg =
				serviceGroupService.getGroupById(s.getServiceGroupId());
		// warning类型的数据送一份进message表
		this.message.setId(UUID.randomUUID().toString());
		this.message.setCreateAt(new Date());
		this.message.setContent(event.getMessage());
		this.message.setLevel("WARNING");
		this.message.setServiceGroupId(s.getServiceGroupId());
		this.message.setServiceId(s.getId());
		this.message.setTargetId(s.getId());
		this.message.setTitle(sg.getResult().getName() + "/" + s.getName() + " " + event.getReason());
		this.message.setType(CommonConsant.MESSAGE_TYPE_11);
		this.message.setMessageType(CommonConsant.MESSAGE_TYPE_S);
		messageService.insertMessage(message, false);
		messageService.insertSlackMessage(message);
		this.updateService(serviceId, "8");
	}

	/**
	 * release生命周期监控，修改service，release，candidate状态
	 *
	 * @param v1pod
	 * @return void
	 * @author falcomlife
	 * @date 20-4-3
	 * @version 1.0.0
	 */
	private void releaseLifecycleMonitor(JSONObject v1pod) throws IOException {
		// deployment滚动发布完成
		ResourceService resourceService = SpringBean.getObject(ResourceService.class);
		String serviceId = (String) v1pod.getJSONObject("metadata").getJSONObject("labels").get("serviceId");
		String releaseId = (String) v1pod.getJSONObject("metadata").getJSONObject("labels").get("releaseId");
		for (Object o : v1pod.getJSONObject("metadata").getJSONArray("ownerReferences")) {
			JSONObject v1OwnerReference = (JSONObject) o;
			String ownerReferenceKind = v1OwnerReference.getString("kind");
			String ownerReferenceUid = v1OwnerReference.getString("uid");
			ExecuteResult<JSONObject> ownerRes = resourceService.getResourceByUid(config, ownerReferenceUid,
					ownerReferenceKind);
			JSONObject owner = ownerRes.getResult();
			if (owner.getJSONObject("status").getInteger("replicas") != null && owner.getJSONObject("status").getInteger("replicas").equals(0)) {
				this.updateService(serviceId, "0");
			} else {
				Integer readyReplicas = owner.getJSONObject("status").getInteger("readyReplicas");
				Integer replicas = owner.getJSONObject("status").getInteger("replicas");
				if (readyReplicas != null && replicas != null && readyReplicas.equals(replicas)) {
					int[] type = {4, 8, 10, 11};
					boolean update = false;
					for (int t : type) {
						CandidateDto candidatedto =
								(CandidateDto) redisTemplate.opsForSet().pop("candidateCache:" + serviceId + "," + releaseId + "," + t);
						if (candidatedto != null) {
							this.deploymentFinishBusiness(candidatedto, serviceId, releaseId);
							update = true;
						}
					}
					if (!update) {
						this.updateService(serviceId, "0");
					}
				}
			}
		}
	}

	@Transactional
	public void deploymentFinishBusiness(CandidateDto candidatedto, String serviceId, String releaseId) throws IOException {
		CandidateService candidateService = SpringBean.getObject(CandidateService.class);
		ServiceService serviceService = SpringBean.getObject(ServiceService.class);
		ReleaseService releaseService = SpringBean.getObject(ReleaseService.class);
		IstioManager istioManager = SpringBean.getObject(IstioManager.class);
		MessageService messageService = SpringBean.getObject(MessageService.class);
		ServiceGroupService serviceGroupService = SpringBean.getObject(ServiceGroupService.class);
		UserService userService = SpringBean.getObject(UserService.class);
		ImageDao imageDao = SpringBean.getObject(ImageDao.class);
		ReleaseDao releaseDao = SpringBean.getObject(ReleaseDao.class);
		TopologyAO topology = candidatedto.getTopologyAO();
		ExecuteResult<Service> service = serviceService.getById(candidatedto.getServiceId());
		if (service.getResult() == null) {
			// service可能已经被删除了，完成后删除redis信息
			redisTemplate.opsForSet().remove("candidateCache:" + serviceId + "," + releaseId);
			return;
		}
		ExecuteResult<ServiceGroup> sgr =
				serviceGroupService.getGroupById(service.getResult().getServiceGroupId());
		ServiceGroup sg = sgr.getResult();
		if (candidatedto.getType() == 4) {
			// AB测试
			// pod已经ready了，开始逻辑,否则不做处理
			ExecuteResult<Candidate> candidateInUseRes =
					candidateService.getInUseCandidateByServiceId(candidatedto.getServiceId());
			Candidate candidateInUse = candidateInUseRes.getResult();
			candidateInUse.setStatus(1);
			// 老的候选下线
			candidateService.updateStatus(candidateInUse);
			Candidate candidateNew = new Candidate();
			candidateNew.setId(candidatedto.getCandidateId());
			candidateNew.setStatus(0);
			// 新的候选上线
			candidateService.updateStatus(candidateNew);
			candidateNew = null;
			// service状态改变
			Service s = new Service();
			s.setId(service.getResult().getId());
			s.setStatus("0");
			serviceService.updateStatusById(s);
			s = null;
			// 修改release状态
			Release r1 = new Release();
			ExecuteResult<Release> releaseInUse =
					releaseService.getInUseReleaseByServiceId(service.getResult().getId());
			r1.setId(releaseInUse.getResult().getId());
			Date now = new Date();
			long start = releaseInUse.getResult().getServiceStartAt().getTime();
			long end = now.getTime();
			r1.setDuration(end - start);
			r1.setServiceFinishAt(now);
			r1.setStatus(2);
			releaseService.updateStatus(r1);
			r1 = null;
			Release r2 = new Release();
			r2.setId(candidatedto.getReleaseId());
			r2.setDeployFinishAt(now);
			r2.setServiceStartAt(now);
			r2.setStatus(1);
			releaseService.updateStatus(r2);
			r2 = null;
			// 解析拓扑图
			// virtualservice
			VirtualService virtualService =
					istioManager.getVirtualservice(cluster.getK8sKubeconfig(),
							"virtualservice-" + service.getResult().getName(),
							env.getNamespace());
			virtualService.getSpec().setHosts(topology.getHost());
			List<HTTPRoute> httpRoutes = new ArrayList<>();
			List<HTTPRouteDestination> httpRouteDestinationsAll = new ArrayList<>();
			for (Match match : topology.getMatch()) {
				HTTPRoute httpRoute = new HTTPRoute();
				List<HTTPMatchRequest> httpMatchRequests = new ArrayList<>();
				List<HTTPRouteDestination> httpRouteDestinations = new ArrayList<>();
				httpRoute.setRoute(httpRouteDestinations);
				if (match.getRule() == null || match.getRule().size() == 0) {
					// 有任何匹配条件，默认路由
					topology.getMap().forEach(map -> {
						if (map.getMatchId().equals(match.getId())) {
							HTTPRouteDestination httpRouteDestination =
									new HTTPRouteDestination();
							Destination destination = new Destination();
							topology.getRoute().forEach(route -> {
								if (map.getRouteId().equals(route.getId())) {
									destination.setSubset(route.getSubset());
									destination.setHost(service.getResult().getName());
								}
							});
							httpRouteDestination.setDestination(destination);
							httpRouteDestination.setWeight(map.getWeight());
							httpRouteDestinations.add(httpRouteDestination);
							httpRouteDestinationsAll.addAll(httpRouteDestinations);
						}
					});
				} else {
					// 有匹配条件的路由
					for (Rule rule : match.getRule()) {
						HTTPMatchRequest httpMatchRequest = new HTTPMatchRequest();
						Map<String, StringMatch> stringStringMatchMap = new HashMap<>();
						rule.getStringMatch().forEach(myStringMatch -> {
							if ("Authority".equals(myStringMatch.getType())) {
								if ("exact".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									ExactMatchType matchType = new ExactMatchType();
									matchType.setExact(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setAuthority(stringMatch);
								} else if ("prefix".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									PrefixMatchType matchType = new PrefixMatchType();
									matchType.setPrefix(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setAuthority(stringMatch);
								} else if ("regex".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									RegexMatchType matchType = new RegexMatchType();
									matchType.setRegex(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setAuthority(stringMatch);
								}
							} else if ("method".equals(myStringMatch.getType())) {
								if ("exact".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									ExactMatchType matchType = new ExactMatchType();
									matchType.setExact(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setMethod(stringMatch);
								} else if ("prefix".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									PrefixMatchType matchType = new PrefixMatchType();
									matchType.setPrefix(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setMethod(stringMatch);
								} else if ("regex".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									RegexMatchType matchType = new RegexMatchType();
									matchType.setRegex(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setMethod(stringMatch);
								}
							} else if ("scheme".equals(myStringMatch.getType())) {
								if ("exact".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									ExactMatchType matchType = new ExactMatchType();
									matchType.setExact(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setScheme(stringMatch);
								} else if ("prefix".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									PrefixMatchType matchType = new PrefixMatchType();
									matchType.setPrefix(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setScheme(stringMatch);
								} else if ("regex".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									RegexMatchType matchType = new RegexMatchType();
									matchType.setRegex(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setScheme(stringMatch);
								}
							} else if ("uri".equals(myStringMatch.getType())) {
								if ("exact".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									ExactMatchType matchType = new ExactMatchType();
									matchType.setExact(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setUri(stringMatch);
								} else if ("prefix".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									PrefixMatchType matchType = new PrefixMatchType();
									matchType.setPrefix(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setUri(stringMatch);
								} else if ("regex".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									RegexMatchType matchType = new RegexMatchType();
									matchType.setRegex(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									httpMatchRequest.setUri(stringMatch);
								}
							} else if ("headers".equals(myStringMatch.getType())) {
								if ("exact".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									ExactMatchType matchType = new ExactMatchType();
									matchType.setExact(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									stringStringMatchMap.put(myStringMatch.getKey(),
											stringMatch);
									httpMatchRequest.setHeaders(stringStringMatchMap);
								} else if ("prefix".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									PrefixMatchType matchType = new PrefixMatchType();
									matchType.setPrefix(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									stringStringMatchMap.put(myStringMatch.getKey(),
											stringMatch);
									httpMatchRequest.setHeaders(stringStringMatchMap);
								} else if ("regex".equals(myStringMatch.getExpression())) {
									StringMatch stringMatch = new StringMatch();
									RegexMatchType matchType = new RegexMatchType();
									matchType.setRegex(myStringMatch.getValue());
									stringMatch.setMatchType(matchType);
									stringStringMatchMap.put(myStringMatch.getKey(),
											stringMatch);
									httpMatchRequest.setHeaders(stringStringMatchMap);
								}
							}

						});
						topology.getMap().forEach(map -> {
							if (map.getMatchId().equals(match.getId())) {
								HTTPRouteDestination httpRouteDestination =
										new HTTPRouteDestination();
								Destination destination = new Destination();
								topology.getRoute().forEach(route -> {
									if (map.getRouteId().equals(route.getId())) {
										destination.setSubset(route.getSubset());
										destination.setHost(service.getResult().getName());
									}
								});
								httpRouteDestination.setDestination(destination);
								httpRouteDestination.setWeight(map.getWeight());
								httpRouteDestinations.add(httpRouteDestination);
								httpRouteDestinationsAll.addAll(httpRouteDestinations);
							}
						});
						httpMatchRequests.add(httpMatchRequest);
					}
				}
				httpRoutes.add(httpRoute);
			}
			httpRouteDestinationsAll.stream().forEach(httpRouteDestination -> {
				List<Release> releases =
						releaseService.getReleaseList(service.getResult().getId());
				releases.forEach(releaseInner -> {
					String version = httpRouteDestination.getDestination().getSubset();
					ExecuteResult<Candidate> candidateRes =
							candidateService.getByReleaseIdAndName(releaseInner.getId(),
									httpRouteDestination.getDestination().getSubset());
					Candidate candidateInner = candidateRes.getResult();
					if (candidateInner != null) {
						candidateService.updateWeight(candidateInner.getId(),
								httpRouteDestination.getWeight());
					}
				});
			});
			virtualService.getSpec().setHttp(httpRoutes);
			VirtualService virtualServiceRes =
					istioManager.createVirtualServices(cluster.getK8sKubeconfig(),
							virtualService);
			// destinationrule
			DestinationRule destinationRule =
					istioManager.getDestinationrules(cluster.getK8sKubeconfig()
							, "dest-" + service.getResult().getName(), env.getNamespace());
			List<Subset> subsets = topology.getRoute().stream().map(route -> {
				Subset subset = new Subset();
				subset.setName(route.getSubset());
				if (route.getLabels() == null) {
					Map<String, String> labels = new HashMap<>();
					labels.put("version", candidatedto.getVersion());
					subset.setLabels(labels);
				} else {
					subset.setLabels(route.getLabels());
				}
				return subset;
			}).collect(Collectors.toList());
			destinationRule.getSpec().setHost(topology.getRoute().get(0).getHost());
			destinationRule.getSpec().setSubsets(subsets);
			istioManager.createDestinationrules(cluster.getK8sKubeconfig(), destinationRule);
		} else if (candidatedto.getType() == 8) {
			// 滚动发布
			// pod已经ready了，开始逻辑,否则不做处理
			// 获取现在的候选
			Candidate candidateNew = new Candidate();
			candidateNew.setId(candidatedto.getCandidateId());
			candidateNew.setStatus(0);
			candidateNew.setReleaseId(candidatedto.getReleaseId());
			candidateNew.setImageId(candidatedto.getImageId());
			candidateNew.setName(candidatedto.getVersion());
			// 新的候选上线
			candidateService.update(candidateNew);
			candidateNew = null;
			// service状态改变
			Service s = new Service();
			s.setId(service.getResult().getId());
			s.setStatus("0");
			serviceService.updateStatusById(s);
			s = null;
			// 修改release状态
			ExecuteResult<Release> releaseInUse =
					releaseService.getInUseReleaseByServiceId(service.getResult().getId());
			if (releaseInUse.getResult() == null) {
				// 如果获取不到正在使用的release，表示这是首次发布，采用一下逻辑
				Release r1 = new Release();
				r1.setId(candidatedto.getReleaseId());
				r1.setStatus(1);
				releaseService.updateStatus(r1);

			} else {
				// 如果能查到正在使用的release，表示这不是首次发布，采用一下逻辑
				Release r1 = new Release();
				r1.setId(releaseInUse.getResult().getId());
				Date now = new Date();
				long start = releaseInUse.getResult().getServiceStartAt().getTime();
				long end = now.getTime();
				r1.setDuration(end - start);
				r1.setServiceFinishAt(now);
				r1.setStatus(2);
				releaseService.updateStatus(r1);
				r1 = null;
				Release r2 = new Release();
				r2.setId(candidatedto.getReleaseId());
				r2.setDeployFinishAt(now);
				r2.setServiceStartAt(now);
				r2.setStatus(1);
				releaseService.updateStatus(r2);
				r2 = null;
			}
			// 解析拓扑图
			// virtualservice
			VirtualService virtualService =
					istioManager.getVirtualservice(cluster.getK8sKubeconfig(),
							"virtualservice-" + service.getResult().getName(),
							env.getNamespace());
			for (HTTPRoute httpRoute : virtualService.getSpec().getHttp()) {
				for (HTTPRouteDestination httpRouteDestination : httpRoute.getRoute()) {
					httpRouteDestination.getDestination().setSubset(candidatedto.getVersion());
				}
			}
			VirtualService virtualServiceRes =
					istioManager.createVirtualServices(cluster.getK8sKubeconfig(),
							virtualService);
			// destinationrule
			DestinationRule destinationRule =
					istioManager.getDestinationrules(cluster.getK8sKubeconfig()
							, "dest-" + service.getResult().getName(), env.getNamespace());
			destinationRule.getSpec().getSubsets().stream().forEach(subset -> {
				subset.setName(candidatedto.getVersion());
				Map<String, String> subsetversion = new HashMap<>();
				subsetversion.put("version", candidatedto.getVersion());
				subset.setLabels(subsetversion);

			});
			istioManager.createDestinationrules(cluster.getK8sKubeconfig(), destinationRule);
		} else if (candidatedto.getType() == 10) {
			// 首次发布和后续发布
			Message message = new Message();
			message.setId(UUID.randomUUID().toString());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			message.setCreateAt(new Date());
			// deploy successfully.
			message.setContent(sg.getName() + "/" + service.getResult().getName() + " deployed successfully.");
			message.setLevel("NORMAL");

			message.setServiceGroupId(service.getResult().getServiceGroupId());
			message.setServiceId(service.getResult().getId());
			message.setTargetId(candidatedto.getReleaseId());
			// deploy successfully.
			message.setTitle(sg.getName() + "/" + service.getResult().getName() + " deployed successfully.");
			message.setType(CommonConsant.MESSAGE_TYPE_2);
			message.setMessageType(CommonConsant.MESSAGE_TYPE_S);
			if (candidatedto.getAuto() != null && candidatedto.getAuto()) {
				User user = userService.getUserByUsername("bot");
				message.setOperuserId(user.getId());
			} else {
				message.setOperuserId(service.getResult().getCreateById());
			}
			Release release = releaseDao.getInUseReleaseByReleaseId(candidatedto.getReleaseId());
			if (release != null) {
				String imageId = release.getImageId();
				Image image = imageDao.getImageById(imageId);
				Map<String, Object> param = new HashMap<>();
				if (image != null) {
					param.put("imageVersioin", image.getVersion());
				}
				param.put("releaseName", release.getName());
				param.put("operChannel", release.getOperChannel());
				message.setParams(param);
			}
			messageService.insertMessage(message, false);
			messageService.insertSlackMessage(message);
			message = null;
		} else if (candidatedto.getType() == 11) {
			// scale
			Message message = new Message();
			message.setId(UUID.randomUUID().toString());
			message.setCreateAt(new Date());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = sdf.format(new Date());
			message.setCreateAt(new Date());
			// sg.getName() + "/" + service.getResult().getName() + " pod " +
			//		" scaling successfully. From" + candidatedto.getScaleCount()[0] + " to " + candidatedto
			// .getScaleCount()[1]
			message.setContent(sg.getName() + "/" + service.getResult().getName() + " pod scaled successfully. From " +
					candidatedto.getScaleCount()[0] + " to " + candidatedto.getScaleCount()[1]);
			message.setLevel("NORMAL");
			message.setOperuserId(service.getResult().getCreateById());
			message.setServiceGroupId(service.getResult().getServiceGroupId());
			message.setServiceId(service.getResult().getId());
			message.setTargetId(service.getResult().getId());
			// sg.getName() + "/" + service.getResult().getName() + " pod scaling successfully."
			message.setTitle(sg.getName() + "/" + service.getResult().getName() + " pod scaled successfully.");
			message.setType(CommonConsant.MESSAGE_TYPE_9);
			message.setMessageType(CommonConsant.MESSAGE_TYPE_S);
			messageService.insertMessage(message, false);
			messageService.insertSlackMessage(message);
			message = null;
		}
	}
}

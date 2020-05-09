package com.ladeit.biz.runner.events;

import com.alibaba.fastjson.JSON;
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
										JSONObject jo = result.getResult().getJSONObject("metadata").getJSONObject(
												"labels");
										if (jo != null) {
											String serviceId = jo.getString("serviceId");
											ExecuteResult<Service> s = serviceService.getById(serviceId);
											if (s.getResult() == null) {
												continue;
											}
											log.info("verison" + currentRev + ",kind:" + v1event.getInvolvedObject().getKind() + "," + "namespace:" + v1event.getInvolvedObject().getNamespace() + ",name" + v1event.getInvolvedObject().getName() + ", reason:" + v1event.getReason() + ",message" + v1event.getMessage());
											// 在redis中存储半小时的历史event信息
											this.redisEventsHistory(event, v1event);
											// redis发布实时信息
											this.redisEventActual(event, s.getResult());
											// 执行生命周期检测，修改数据库信息
											this.releaseLifecycleMonitor(v1event, serviceId, s.getResult());
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
	private void releaseLifecycleMonitor(V1Event event, String serviceId, Service s) throws ApiException, IOException {
		// warning类型的视作服务存在问题，同步更新service状态
		ResourceService resourceService = SpringBean.getObject(ResourceService.class);
		if ("Warning".equals(event.getType())) {
			String kind = event.getInvolvedObject().getKind();
			String uid = event.getInvolvedObject().getUid();
			ExecuteResult<JSONObject> ownerRes = resourceService.getResourceByUid(config, uid, kind);
			if (ownerRes.getCode() != Code.SUCCESS) {
				log.info("未找到资源:" + event.getInvolvedObject().getKind() + "," + event.getInvolvedObject().getUid());
				return;
			}
			JSONObject owner = ownerRes.getResult();
			JSONObject status = owner.getJSONObject("status");
			Integer desireReplicas = owner.getJSONObject("spec").getInteger("replicas");
			if ("Pod".equals(kind) && "spec.containers{istio-proxy}".equals(event.getInvolvedObject().getFieldPath())) {
				log.info("istio组件报错不进行处理:" + event.getMessage());
			} else if ("Pod".equals(kind)) {
				// 如果pod出现异常，一般是replicaset已经成功扩展出了新的pod，但是pod启动异常，此时服务未达到希望达到的运行数量，系统认为是异常状态。
				this.warningBussiness(event, serviceId, s);
			} else if (("ReplicaSet".equals(kind) || "Deployment".equals(kind)) && (status.getInteger("replicas") != null && !status.getInteger(
					"readyReplicas").equals(desireReplicas))) {
				// 如果replicaset或者deployment发生异常event，而且他们的希望值和准备好值不相等，被认为是没有达到希望的运行数量，系统认为是异常状态
				this.warningBussiness(event, serviceId, s);
			} else {
				log.info("资源" + event.getInvolvedObject().getKind() + "," + event.getInvolvedObject().getUid() +
						"状态：replicas " + status.getInteger("replicas") + ",readyReplicas " + status.getInteger(
						"readyReplicas"));
			}
		}
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
}

package com.ladeit.biz.runner.pods;

import com.alibaba.fastjson.JSONObject;
import com.google.common.reflect.TypeToken;
import com.ladeit.biz.config.SpringBean;
import com.ladeit.biz.dao.ImageDao;
import com.ladeit.biz.dao.ReleaseDao;
import com.ladeit.biz.manager.IstioManager;
import com.ladeit.biz.services.*;
import com.ladeit.biz.utils.CommonConsant;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.TopologyAO;
import com.ladeit.pojo.ao.topology.Match;
import com.ladeit.pojo.ao.topology.Rule;
import com.ladeit.pojo.doo.*;
import com.ladeit.pojo.dto.CandidateDto;
import com.ladeit.pojo.dto.Event;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.OkHttpClient;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Event;
import io.kubernetes.client.models.V1OwnerReference;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import lombok.extern.slf4j.Slf4j;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.apache.commons.lang3.StringUtils;
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
public class PodSubcriber {

	private String envId;
	private ApiClient apiClient;
	private CoreV1Api api;
	private EnvService envService;
	private ClusterService clusterService;
	private PodHandler eventHandler;
	private Watch<V1Pod> watch;
	private Env env;
	private Cluster cluster;
	private String rev;
	private boolean stop;
	private RedisTemplate<String, Object> redisTemplate;
	private String config;
	private Service service;

	public Watch<V1Pod> getWatch() {
		return watch;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public PodSubcriber(String envId, PodHandler eventHandler, String rev) {
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
		threadPoolFactory.newThread(() -> {
			String currentRev = null;
			try {
				this.watch =
						Watch.createWatch(
								this.apiClient,
								this.api.listNamespacedPodCall(
										env.getNamespace(), null, null, null, null, null, null, this.rev, null,
										Boolean.TRUE, null, null),
								new TypeToken<Watch.Response<V1Pod>>() {}.getType());
				while (!stop) {
					Watch.Response<V1Pod> item = watch.next();
					if (item != null) {
						V1Pod v1pod = item.object;
						try {
							currentRev = v1pod.getMetadata().getResourceVersion();
							this.releaseLifecycleMonitor(v1pod);
						} catch (Exception e) {
							// 业务部分出现问题不影响下面event的接受
							continue;
						}
					}
				}
			} catch (Exception e) {
				// 如果接收后续events的时候报错，重新开启一个新的订阅者
				// TODO 不知道为什么该watch订阅者会自己关闭
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
	 * release生命周期监控，修改service，release，candidate状态
	 *
	 * @param v1pod
	 * @return void
	 * @author falcomlife
	 * @date 20-4-3
	 * @version 1.0.0
	 */
	private void releaseLifecycleMonitor(V1Pod v1pod) throws IOException {
		// deployment滚动发布完成
		ResourceService resourceService = SpringBean.getObject(ResourceService.class);
		String serviceId = v1pod.getMetadata().getLabels().get("serviceId");
		String releaseId = v1pod.getMetadata().getLabels().get("releaseId");
		for (V1OwnerReference v1OwnerReference : v1pod.getMetadata().getOwnerReferences()) {
			String ownerReferenceKind = v1OwnerReference.getKind();
			String ownerReferenceUid = v1OwnerReference.getUid();
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

	private void updateService(String serviceId, String status) {
		ServiceService serviceService = SpringBean.getObject(ServiceService.class);
		this.service = new Service();
		this.service.setStatus(status);
		this.service.setId(serviceId);
		serviceService.updateStatusById(this.service);
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

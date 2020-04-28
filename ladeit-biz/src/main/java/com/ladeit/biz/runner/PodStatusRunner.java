package com.ladeit.biz.runner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ladeit.biz.dao.ImageDao;
import com.ladeit.biz.dao.ReleaseDao;
import com.ladeit.biz.manager.IstioManager;
import com.ladeit.biz.manager.K8sContainerManager;
import com.ladeit.biz.manager.K8sWorkLoadsManager;
import com.ladeit.biz.services.*;
import com.ladeit.biz.utils.CommonConsant;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.TopologyAO;
import com.ladeit.pojo.ao.topology.Match;
import com.ladeit.pojo.ao.topology.Rule;
import com.ladeit.pojo.doo.*;
import com.ladeit.pojo.dto.CandidateDto;
import com.ladeit.pojo.dto.Event;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1ContainerStatus;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import me.snowdrop.istio.util.YAML;
import org.apache.http.conn.routing.HttpRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PodStatusRunner implements CommandLineRunner {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private ThreadFactory threadPoolFactory;
	@Autowired
	private CandidateService candidateService;
	@Autowired
	private ServiceService serviceService;
	@Autowired
	private ReleaseService releaseService;
	@Autowired
	private EnvService envService;
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private K8sWorkLoadsManager k8sWorkLoadsManager;
	@Autowired
	private IstioManager istioManager;
	@Autowired
	private ServiceGroupService serviceGroupService;
	@Autowired
	private UserService userService;
	@Autowired
	private ReleaseDao releaseDao;
	@Autowired
	private ImageDao imageDao;

	@Override
	public void run(String... args) {
//		Consumer consumer = new Consumer();
//		threadPoolFactory.newThread(() -> {
//			while (true) {
//				try {
//					Thread.sleep(5 * 60 * 1000);
//					// consumer从redis获取所有的候选者
//					Set<Object> set = consumer.getCandidataInRedis();
//					// consumer检查候选者所持有的的pod的情况，并根据情况删除或保留redis中的候选者
//					consumer.exec(set);
//				} catch (InterruptedException e) {
//					log.error(e.getMessage(), e);
//				} catch (Exception e) {
//					log.error(e.getMessage(), e);
//				}
//			}
//		}).start();
	}


	class Consumer {
		// 获取redis中的候选者
		public Set<Object> getCandidataInRedis() {
			return redisTemplate.opsForSet().members("candidate");
		}

		@Transactional(rollbackFor = Exception.class)
		// 执行逻辑
		public void exec(Set<Object> set) {
			set.stream().forEach(c -> {
				log.info("<<<<< consumerstart >>>>>");
				log.info("CandidateDto <---> " + c);
				CandidateDto candidatedto = (CandidateDto) c;
				TopologyAO topology = candidatedto.getTopologyAO();
				ExecuteResult<Service> service = serviceService.getById(candidatedto.getServiceId());
				if (service.getResult() == null) {
					// 如果service已经被删除，这里直接删除redis信息
					redisTemplate.opsForSet().remove("candidate", c);
					return;
				}
				if ("0".equals(service.getResult().getStatus())) {
					// 如果service已经发布完成，这里直接删除redis信息
					redisTemplate.opsForSet().remove("candidate", c);
					return;
				}
				ExecuteResult<ServiceGroup> sgr =
						serviceGroupService.getGroupById(service.getResult().getServiceGroupId());
				ServiceGroup sg = sgr.getResult();
				Env env = envService.getEnvById(service.getResult().getEnvId());
				Cluster cluster = clusterService.getClusterById(env.getClusterId());
				try {
					// 获取现在所有的pod的状态
					List<V1Pod> pods = k8sWorkLoadsManager.getPodsByReleaseId(candidatedto.getReleaseId(),
							cluster.getK8sKubeconfig());
					// "releaseid: " + candidatedto.getReleaseId() + "'s pod amount is " + pods.size()
					log.info("releaseId为" + candidatedto.getReleaseId() + "的pod数量：" + pods.size());
					boolean ready = !pods.isEmpty();
					for (V1Pod pod : pods) {
						for (V1ContainerStatus status : pod.getStatus().getContainerStatuses()) {
							if (status.isReady() && status.getName().equals(service.getResult().getName())) {
								ready = true;
							} else if (!status.isReady() && status.getName().equals(service.getResult().getName())) {
								ready = false;
								break;
							}
						}
						if (!ready) {
							break;
						}
					}
					if (ready) {
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
							// service状态改变
							Service s = new Service();
							s.setId(service.getResult().getId());
							s.setStatus("0");
							serviceService.updateStatusById(s);
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
							Release r2 = new Release();
							r2.setId(candidatedto.getReleaseId());
							r2.setDeployFinishAt(now);
							r2.setServiceStartAt(now);
							r2.setStatus(1);
							releaseService.updateStatus(r2);
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
							// 完成后删除redis信息
							redisTemplate.opsForSet().remove("candidate", c);
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
							// service状态改变
							Service s = new Service();
							s.setId(service.getResult().getId());
							s.setStatus("0");
							serviceService.updateStatusById(s);
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
								Release r2 = new Release();
								r2.setId(candidatedto.getReleaseId());
								r2.setDeployFinishAt(now);
								r2.setServiceStartAt(now);
								r2.setStatus(1);
								releaseService.updateStatus(r2);
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
							log.info(virtualService.toString());
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
							log.info(destinationRule.toString());
							istioManager.createDestinationrules(cluster.getK8sKubeconfig(), destinationRule);
							// 完成后删除redis信息
							redisTemplate.opsForSet().remove("candidate", c);
						} else if (candidatedto.getType() == 10) {
							// 首次发布和后续发布
							Message message = new Message();
							message.setId(UUID.randomUUID().toString());
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							message.setCreateAt(new Date());
							// deploy successfully
							message.setContent(sg.getName() + "/" + service.getResult().getName() + " deployed successfully.");
							message.setLevel("NORMAL");

							message.setServiceGroupId(service.getResult().getServiceGroupId());
							message.setServiceId(service.getResult().getId());
							message.setTargetId(candidatedto.getReleaseId());
							// deploy successfully
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
							messageService.insertMessage(message,false);
							messageService.insertSlackMessage(message);
							// 完成后删除redis信息
							redisTemplate.opsForSet().remove("candidate", c);
						} else if (candidatedto.getType() == 11) {
							// scale
							Message message = new Message();
							message.setId(UUID.randomUUID().toString());
							message.setCreateAt(new Date());
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String time = sdf.format(new Date());
							message.setCreateAt(new Date());
							// sg.getName() + "/" + service.getResult().getName() + " pod " +
							//		" scaling successfully. From " + candidatedto.getScaleCount()[0] + " to " +
							// candidatedto.getScaleCount()[1]
							message.setContent(sg.getName() + "/" + service.getResult().getName() + " pod scaled successfully. From " +
										candidatedto.getScaleCount()[0] + " to " + candidatedto.getScaleCount()[1]);
							message.setLevel("NORMAL");
							message.setOperuserId(service.getResult().getCreateById());
							message.setServiceGroupId(service.getResult().getServiceGroupId());
							message.setServiceId(service.getResult().getId());
							message.setTargetId(service.getResult().getId());
							// pod scaling successfully.
							message.setTitle(sg.getName() + "/" + service.getResult().getName() + " pod scaled successfully.");
							message.setType(CommonConsant.MESSAGE_TYPE_9);
							message.setMessageType(CommonConsant.MESSAGE_TYPE_S);
							messageService.insertMessage(message,false);
							messageService.insertSlackMessage(message);
							// 完成后删除redis信息
							redisTemplate.opsForSet().remove("candidate", c);
						}
					}
				} catch (IOException | ApiException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}
}



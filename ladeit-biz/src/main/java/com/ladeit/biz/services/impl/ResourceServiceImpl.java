package com.ladeit.biz.services.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ladeit.biz.dao.ClusterDao;
import com.ladeit.biz.manager.IstioManager;
import com.ladeit.biz.manager.K8sClusterManager;
import com.ladeit.biz.manager.K8sContainerManager;
import com.ladeit.biz.manager.K8sWorkLoadsManager;
import com.ladeit.biz.services.*;
import com.ladeit.biz.utils.CommonConsant;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.biz.utils.Producer;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.holder.K8sApiCallback;
import com.ladeit.common.holder.K8sHolder;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.ao.configuration.LivenessProbe;
import com.ladeit.pojo.ao.configuration.Volume;
import com.ladeit.pojo.ao.serviceIngress.ServicePort;
import com.ladeit.pojo.ao.topology.*;
import com.ladeit.pojo.ao.topology.StringMatch;
import com.ladeit.pojo.ao.typesResource.*;
import com.ladeit.pojo.doo.*;
import com.ladeit.util.k8s.K8sClientUtil;
import com.ladeit.util.k8s.UnitUtil;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ResourceQuota;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import me.snowdrop.istio.api.Duration;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import me.snowdrop.istio.api.networking.v1alpha3.CorsPolicy;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: ladeit
 * @description: ResourceServiceImpl
 * @author: falcomlife
 * @create: 2019/11/18
 * @version: 1.0.0
 */
@Service
@Slf4j
public class ResourceServiceImpl implements ResourceService {

	@Autowired
	private K8sClusterManager k8sClusterManager;
	@Autowired
	private ClusterDao clusterDao;
	@Autowired
	private K8sWorkLoadsManager k8sWorkLoadsManager;
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private ServiceService serviceService;
	@Autowired
	private EnvService envService;
	@Autowired
	private K8sContainerManager k8sContainerManager;
	@Autowired
	private IstioManager istioManager;
	@Autowired
	private Producer producer;
	@Autowired
	private ReleaseService releaseService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private ServiceGroupService serviceGroupService;
	@Autowired
	private MessageUtils messageUtils;

	/**
	 * 查询yaml
	 *
	 * @param clusterId
	 * @param namespace
	 * @param name
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-18
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> getYaml(String clusterId, String type, String namespace, String name) throws InvocationTargetException, IllegalAccessException {
		ExecuteResult<String> result = new ExecuteResult<>();
		Cluster cluster = this.clusterDao.getClusterById(clusterId);
		Class clazz = this.k8sClusterManager.getClass();
		Method[] methods = clazz.getMethods();
		List invokeList = null;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().toLowerCase().contains(type.toLowerCase())) {
				invokeList = (List) methods[i].invoke(k8sClusterManager, null, cluster.getK8sKubeconfig(), namespace);
			}
		}
		this.resolvingPolicy(invokeList, type, name, result);
		return result;
	}

	/**
	 * 资源列表页面页面查询yaml
	 *
	 * @param serviceId
	 * @param type
	 * @param name
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-17
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> getYamlInService(String serviceId, String type, String name) throws InvocationTargetException, IllegalAccessException {
		ExecuteResult<String> result = new ExecuteResult<>();
		ExecuteResult<com.ladeit.pojo.doo.Service> service = this.serviceService.getById(serviceId);
		Env env = this.envService.getEnvById(service.getResult().getEnvId());
		Cluster cluster = this.clusterDao.getClusterById(env.getClusterId());
		Class clazz = this.k8sClusterManager.getClass();
		Method[] methods = clazz.getMethods();
		List invokeList = null;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().toLowerCase().substring(3).equals(type.toLowerCase() + "s")) {
				invokeList = (List) methods[i].invoke(k8sClusterManager, serviceId, cluster.getK8sKubeconfig(),
						env.getNamespace());
			}
		}
		this.resolvingPolicy(invokeList, type, name, result);
		return result;
	}

	/**
	 * release资源列表页面新建资源
	 *
	 * @param yaml
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-17
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> createByYaml(String yaml, String serviceId) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		ExecuteResult<com.ladeit.pojo.doo.Service> service = this.serviceService.getById(serviceId);
		Env env = this.envService.getEnvById(service.getResult().getEnvId());
		Cluster cluster = this.clusterDao.getClusterById(env.getClusterId());
		Map item = (Map) (new org.yaml.snakeyaml.Yaml().load(yaml));
		String kind = (String) item.get("kind");
		Object metadataObj = item.get("metadata");
		Object specObj = item.get("spec");
		String name = null;
		String namespace = null;
		if (metadataObj != null) {
			// 获取资源的名字
			Map<String, Object> metadata = (Map<String, Object>) metadataObj;
			Object nameobj = metadata.get("name");
			Object namespaceobj = metadata.get("namespace");
			if (nameobj != null) {
				name = nameobj.toString();
			}
			if (namespaceobj == null) {
				namespace = "default";
			} else {
				namespace = (String) namespaceobj;
			}
			// 首先，把该资源加上serviceId的label
			Object labelsObj = metadata.get("labels");
			Map<String, String> labels = null;
			if (labelsObj != null) {
				labels = (Map<String, String>) labelsObj;
			} else {
				labels = new HashMap<>();
			}
			labels.put("serviceId", serviceId);
			metadata.put("labels", labels);
			// 同时，新建的时候需要清空metadata的一些属性
			metadata.remove("annotations");
			metadata.remove("uid");
			metadata.remove("selfLink");
			metadata.remove("resourceVersion");
			metadata.remove("creationTimestamp");
		}
		// 新建的时候需要清空metadata的一些属性
		item.remove("apiVersion");
		item.remove("kind");
		item.remove("status");
		if (specObj != null) {
			Map<String, Object> spec = (Map<String, Object>) specObj;
			String lowerKind = kind.toLowerCase();
			// 如果是controller，那么把他下面的pod也加上serviceId的label,还有version
			if (StringUtils.isNotBlank(kind) && (lowerKind.contains("deployment") || lowerKind.contains("statefulset") || lowerKind.contains("daemonset") || lowerKind.contains("replicationcontroller"))) {
				Object templateObj = spec.get("template");
				if (templateObj != null) {
					Map<String, Object> template = (Map<String, Object>) templateObj;
					Object templateMetadataObj = template.get("metadata");
					if (templateMetadataObj != null) {
						Map<String, Object> templateMetadata = (Map<String, Object>) templateMetadataObj;
						Object labelsObj = templateMetadata.get("labels");
						Map<String, String> labels = null;
						if (labelsObj != null) {
							labels = (Map<String, String>) labelsObj;
						} else {
							labels = new HashMap<>();
						}
						labels.put("serviceId", serviceId);
						templateMetadata.put("labels", labels);
					}
				}
			}
		}
		YamlContentAO yamlAo = new YamlContentAO();
		if ("DestinationRule".equals(kind) || "VirtualService".equals(kind)) {
			yamlAo.setKindType(kind);
		} else {
			yamlAo.setKindType("V1" + kind);
		}
		yaml = new org.yaml.snakeyaml.Yaml().dump(item);
		yamlAo.setContent(yaml);
		yamlAo.setNameSpace(namespace);
		yamlAo.setServiceId(serviceId);
		try {
			this.k8sContainerManager.createByYaml(yamlAo, cluster.getK8sKubeconfig(), name);
		} catch (ApiException e) {
			log.error(yamlAo.toString() + e.getMessage(), e);
			if ("Unprocessable Entity".equals(e.getLocalizedMessage())) {
				result.setCode(Code.K8SWARN);
				String message = messageUtils.matchMessage("M0014", new Object[]{yamlAo.getKindType(), name},
						Boolean.TRUE);
				result.addWarningMessage(message);

			} else if ("Conflict".equals(e.getLocalizedMessage())) {
				result.setCode(Code.K8SWARN);
				String message = messageUtils.matchMessage("M0015", new Object[]{yamlAo.getKindType(),
						e.getLocalizedMessage()}, Boolean.TRUE);
				result.addWarningMessage(message);
			}
		}
		return result;
	}

	/**
	 * release资源列表页面修改资源
	 *
	 * @param yaml
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-17
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> replaceByYaml(String yaml, String serviceId) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		ExecuteResult<com.ladeit.pojo.doo.Service> service = this.serviceService.getById(serviceId);
		Env env = this.envService.getEnvById(service.getResult().getEnvId());
		Cluster cluster = this.clusterDao.getClusterById(env.getClusterId());
		Map item = (Map) (new org.yaml.snakeyaml.Yaml().load(yaml));
		String kind = (String) item.get("kind");
		item.put("apiVersion", null);
		item.put("kind", null);
		Map<String, Object> metadata = (Map<String, Object>) item.get("metadata");
		Object namespaceobj = metadata.get("namespace");
		Object nameobj = metadata.get("name");
		String namespace = null;
		if (namespaceobj != null) {
			namespace = namespaceobj.toString();
		} else {
			namespace = "default";
		}
		String name = null;
		if (nameobj != null) {
			name = nameobj.toString();
		}
		yaml = new org.yaml.snakeyaml.Yaml().dump(item);
		YamlContentAO yamlContentAO = new YamlContentAO();
		yamlContentAO.setServiceId(serviceId);
		yamlContentAO.setNameSpace(namespace);
		yamlContentAO.setContent(yaml);
		yamlContentAO.setKindType("V1" + kind);
		try {
			K8sHolder k8sHolder = new K8sHolder();
			K8sApiCallback apiCallback = new K8sApiCallback(k8sHolder);
			result.setResult(this.k8sContainerManager.replaceByYaml(yamlContentAO, cluster.getK8sKubeconfig(), name,
					apiCallback));
			//k8sHolder.hold();
		} catch (ApiException e) {
			log.error(yamlContentAO.toString() + e.getMessage(), e);
			if ("Unprocessable Entity".equals(e.getLocalizedMessage())) {
				result.setCode(Code.K8SWARN);
				String message = messageUtils.matchMessage("M0014", new Object[]{yamlContentAO.getKindType(), name},
						Boolean.TRUE);
				result.addWarningMessage(message);

			} else if ("Conflict".equals(e.getLocalizedMessage())) {
				result.setCode(Code.K8SWARN);
				String message = messageUtils.matchMessage("M0015", new Object[]{yamlContentAO.getKindType(),
						e.getLocalizedMessage()}, Boolean.TRUE);
				result.addWarningMessage(message);
			}
		}
		return result;
	}

	/**
	 * 服务详情伸缩pod数量
	 *
	 * @param count
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-20
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> scaleWorkload(int count, String serviceId) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		String config = this.getConfigByServiceId(serviceId);
		ExecuteResult<Release> release = this.releaseService.getInUseReleaseByServiceId(serviceId);
		ExecuteResult<List<V1Deployment>> deployments = this.k8sWorkLoadsManager.getDeployment(serviceId, config);
		ExecuteResult<List<V1StatefulSet>> statefulSets = this.k8sWorkLoadsManager.getStatefulSet(serviceId,
				config);
		ExecuteResult<List<V1ReplicationController>> replicationControllers =
				this.k8sWorkLoadsManager.getReplicationControllers(serviceId, config);
		Integer nowPodCount = 0;
		if (deployments.getResult() != null && deployments.getResult().size() != 0) {
			V1Deployment deployment = deployments.getResult().get(0);
			deployment.getSpec().setReplicas(count);
			log.info(">>>>" + deployment.getStatus().toString());
			log.info(">>>>" + deployment.getStatus().getReplicas());
			nowPodCount = deployment.getStatus().getReplicas() == null ? 0 : deployment.getStatus().getReplicas();
			V1Scale scale = this.k8sWorkLoadsManager.scaleDeployment(config,
					deployment.getMetadata().getNamespace(), deployment.getMetadata().getName(), deployment);
			result.setResult(scale.getStatus().getReplicas() + "");
		} else if (statefulSets.getResult() != null && statefulSets.getResult().size() != 0) {
			V1StatefulSet statefulSet = statefulSets.getResult().get(0);
			statefulSet.getSpec().setReplicas(count);
			nowPodCount = statefulSet.getStatus().getReplicas() == null ? 0 : statefulSet.getStatus().getReplicas();
			V1Scale scale = this.k8sWorkLoadsManager.scaleStatefulSet(config,
					statefulSet.getMetadata().getNamespace(), statefulSet.getMetadata().getName(), statefulSet);
			result.setResult(scale.getStatus().getReplicas() + "");
		} else if (replicationControllers.getResult() != null && replicationControllers.getResult().size() != 0) {
			V1ReplicationController replicationController = replicationControllers.getResult().get(0);
			replicationController.getSpec().setReplicas(count);
			nowPodCount = replicationController.getStatus().getReplicas() == null ? 0 :
					replicationController.getStatus().getReplicas();
			V1Scale scale = this.k8sWorkLoadsManager.scaleReplicationControllers(config,
					replicationController.getMetadata().getNamespace(), replicationController.getMetadata().getName(),
					replicationController);
			result.setResult(scale.getStatus().getReplicas() + "");
		}
		ExecuteResult<com.ladeit.pojo.doo.Service> s = this.serviceService.getById(serviceId);
		ExecuteResult<ServiceGroup> sgr = serviceGroupService.getGroupById(s.getResult().getServiceGroupId());
		ServiceGroup sg = sgr.getResult();
		Message message = new Message();
		message.setId(UUID.randomUUID().toString());
		message.setCreateAt(new Date());
		// sg.getName() + "/" + s.getResult().getName() + " pod scaling..."
		message.setContent(sg.getName() + "/" + s.getResult().getName() + " pod scaling...");
		message.setLevel("NORMAL");
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		message.setOperuserId(user.getId());
		message.setServiceGroupId(s.getResult().getServiceGroupId());
		message.setServiceId(s.getResult().getId());
		message.setTargetId(serviceId);
		// pod scaling...
		message.setTitle(sg.getName() + "/" + s.getResult().getName() + " pod scaling...");
		message.setType(CommonConsant.MESSAGE_TYPE_9);
		message.setMessageType(CommonConsant.MESSAGE_TYPE_S);
		this.messageService.insertMessage(message, Boolean.TRUE);
		this.messageService.insertSlackMessage(message);
		Integer[] sacle = {nowPodCount, count};
		this.producer.putCandidate(release.getResult().getId(), serviceId, null, null, null, null, null, 11, sacle,
				false);
		return result;
	}

	/**
	 * 查询service包含的各个状态的pod的数量
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.Map<java.lang.Integer>>
	 * @author falcomlife
	 * @date 19-12-20
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<Map<String, Long>> getPodsStatus(String serviceId) throws IOException {
		ExecuteResult<Map<String, Long>> result = new ExecuteResult<>();
		Map<String, Long> mapResult = new HashMap<>();
		ExecuteResult<List<V1Pod>> pods = this.k8sWorkLoadsManager.getPods(serviceId,
				this.getConfigByServiceId(serviceId));
		if (pods.getResult() != null && pods.getResult().size() != 0) {
			mapResult =
					pods.getResult().stream().map(pod -> pod.getStatus()).collect(Collectors.groupingBy(V1PodStatus::getPhase, Collectors.counting()));
			Long all = mapResult.values().stream().reduce((i, j) -> i + j).get();
			mapResult.put("SUM", all);
		} else {
			mapResult.put("SUM", 0L);
		}
		result.setResult(mapResult);
		return result;
	}

	/**
	 * 查询service的拓扑图
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.TopologyAO>
	 * @author falcomlife
	 * @date 19-12-21
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<TopologyAO> getTopology(String serviceId) throws IOException {
		ExecuteResult<TopologyAO> result = new ExecuteResult<>();
		ExecuteResult<com.ladeit.pojo.doo.Service> service = this.serviceService.getById(serviceId);
		Env env = this.envService.getEnvById(service.getResult().getEnvId());
		Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
		TopologyAO topology = new TopologyAO();
		// 查询Virtualservice
		VirtualService virtualService = this.istioManager.getVirtualservice(cluster.getK8sKubeconfig(),
				"virtualservice-" + service.getResult().getName(), env.getNamespace());
		if (virtualService == null) {
			result.setCode(Code.NOTFOUND);
			return result;
		}
		// 查询Destinationrules
		DestinationRule destinationRule = this.istioManager.getDestinationrules(cluster.getK8sKubeconfig(),
				"dest-" + service.getResult().getName(), env.getNamespace());
		// 封装host
		List<String> host = virtualService.getSpec().getHosts();
		topology.setHost(host);
		if (virtualService.getSpec().getHttp() != null && virtualService.getSpec().getHttp().size() != 0) {
			// 封装match
			List<Match> matchList = virtualService.getSpec().getHttp().stream().map(http -> {
				Match httpMatch = new Match();
				httpMatch.setId(UUID.randomUUID().toString());
				// 处理corspolicy
				com.ladeit.pojo.ao.topology.CorsPolicy corsPolicy = new com.ladeit.pojo.ao.topology.CorsPolicy();
				if (http.getCorsPolicy() != null) {
					BeanUtils.copyProperties(http.getCorsPolicy(), corsPolicy);
					//corsPolicy.setAllowCredentials(http.getCorsPolicy().getAllowCredentials().getValue());
					corsPolicy.setMaxAge(http.getCorsPolicy().getMaxAge().getSeconds());
					httpMatch.setCorsPolicy(corsPolicy);
				}

				// 处理header
				if (http.getHeaders() != null && http.getHeaders().getRequest() != null) {
					Header headers = new Header();
					List<String> add = new ArrayList<>();
					List<String> set = new ArrayList<>();
					if (http.getHeaders().getRequest().getAdd() != null && http.getHeaders().getRequest().getAdd().size() != 0) {
						http.getHeaders().getRequest().getAdd().forEach((key, value) -> {
							add.add(key + ":" + value);
						});
					}
					if (http.getHeaders().getRequest().getSet() != null && http.getHeaders().getRequest().getSet().size() != 0) {
						http.getHeaders().getRequest().getSet().forEach((key, value) -> {
							set.add(key + ":" + value);
						});
					}
					headers.setAdd(add);
					headers.setSet(set);
					headers.setRemove(http.getHeaders().getRequest().getRemove());
					httpMatch.setHeaders(headers);
				}
				// 处理redirect
				if (http.getRedirect() != null) {
					Redirect redirect = new Redirect();
					BeanUtils.copyProperties(http.getRedirect(), redirect);
					httpMatch.setRedirect(redirect);
				}
				// 处理rewrite
				if (http.getRewrite() != null) {
					Rewrite rewrite = new Rewrite();
					BeanUtils.copyProperties(http.getRewrite(), rewrite);
					httpMatch.setRewrite(rewrite);
				}
				// 处理retries
				if (http.getRetries() != null) {
					Retries retries = new Retries();
					BeanUtils.copyProperties(http.getRetries(), retries);
					retries.setPerTryTimeout(http.getRetries().getPerTryTimeout().getSeconds());
					httpMatch.setRetries(retries);
				}
				httpMatch.setName("name");
				if (http.getTimeout() != null) {
					httpMatch.setTimeout(http.getTimeout().toString());
				}
				List<Rule> rules = http.getMatch().stream().map(match -> {
					Rule rule = new Rule();
					rule.setName("name");
					List<StringMatch> stringMatches = new ArrayList<>();
					rule.setStringMatch(stringMatches);
					// Authority
					if (match.getAuthority() != null) {
						StringMatch authority = new StringMatch();
						String expression =
								match.getAuthority().getMatchType().toString().split("\\(")[1].split("=")[0];
						String tmp = match.getAuthority().getMatchType().toString().split("\\(")[1].split("=")[1];
						String value = tmp.substring(0, tmp.length() - 1);
						authority.setType("Authority");
						authority.setExpression(expression);
						authority.setValue(value);
						stringMatches.add(authority);
					}
					// method
					if (match.getMethod() != null) {
						StringMatch method = new StringMatch();
						String expression = match.getMethod().getMatchType().toString().split("\\(")[1].split("=")[0];
						String tmp = match.getMethod().getMatchType().toString().split("\\(")[1].split("=")[1];
						String value = tmp.substring(0, tmp.length() - 1);
						method.setType("method");
						method.setExpression(expression);
						method.setValue(value);
						stringMatches.add(method);
					}
					// scheme
					if (match.getScheme() != null) {
						StringMatch scheme = new StringMatch();
						String expression = match.getScheme().getMatchType().toString().split("\\(")[1].split("=")[0];
						String tmp = match.getScheme().getMatchType().toString().split("\\(")[1].split("=")[1];
						String value = tmp.substring(0, tmp.length() - 1);
						scheme.setType("scheme");
						scheme.setExpression(expression);
						scheme.setValue(value);
						stringMatches.add(scheme);
					}
					// uri
					if (match.getUri() != null) {
						StringMatch uri = new StringMatch();
						String expression = match.getUri().getMatchType().toString().split("\\(")[1].split("=")[0];
						String tmp = match.getUri().getMatchType().toString().split("\\(")[1].split("=")[1];
						String value = tmp.substring(0, tmp.length() - 1);
						uri.setType("uri");
						uri.setExpression(expression);
						uri.setValue(value);
						stringMatches.add(uri);
					}
					// headers
					if (match.getHeaders() != null) {
						match.getHeaders().forEach((key, value) -> {
							StringMatch headInner = new StringMatch();
							String expression = value.getMatchType().toString().split("\\(")[1].split("=")[0];
							String tmp = value.getMatchType().toString().split("\\(")[1].split("=")[1];
							String val = tmp.substring(0, tmp.length() - 1);
							headInner.setKey(key);
							headInner.setType("headers");
							headInner.setExpression(expression);
							headInner.setValue(val);
							stringMatches.add(headInner);
						});
					}
					return rule;
				}).collect(Collectors.toList());
				httpMatch.setRule(rules);
				// 封装映射关系
				List<Map<String, Integer>> nameweights = new ArrayList<>();
				http.getRoute().stream().forEach(route -> {
					Map<String, Integer> nameweight = new HashMap<>();
					nameweight.put(route.getDestination().getSubset(), route.getWeight());
					nameweights.add(nameweight);
				});
				httpMatch.setNameWeight(nameweights);
				return httpMatch;
			}).collect(Collectors.toList());
			topology.setMatch(matchList);
			// 封装route
			List<Route> routeList = destinationRule.getSpec().getSubsets().stream().map(subset -> {
				Route topologyRoute = new Route();
				topologyRoute.setId(UUID.randomUUID().toString());
				topologyRoute.setHost(destinationRule.getSpec().getHost());
				topologyRoute.setSubset(subset.getName());
				topologyRoute.setLabels(subset.getLabels());
				return topologyRoute;
			}).collect(Collectors.toList());
			topology.setRoute(routeList);
			// 封装matchroute映射
			List<MatchRouteMap> matchRouteMapList = new ArrayList<>();
			routeList.stream().forEach(route -> {
				matchList.stream().forEach(match -> {
					MatchRouteMap matchRouteMap = new MatchRouteMap();
					match.getNameWeight().forEach(map -> {
						map.keySet().forEach(subset -> {
							if (subset.equals(route.getSubset())) {
								matchRouteMap.setMatchId(match.getId());
								matchRouteMap.setRouteId(route.getId());
								matchRouteMap.setSubset(subset);
								if (map.get(subset) == null) {
									matchRouteMap.setWeight(100);
								} else {
									matchRouteMap.setWeight(map.get(subset));
								}
								matchRouteMapList.add(matchRouteMap);
							}
						});
					});
				});
			});
			topology.setMap(matchRouteMapList);
			result.setResult(topology);
		}
		return result;
	}

	/**
	 * 更新拓扑图
	 *
	 * @param topologyAO
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-21
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> updateTopology(TopologyAO topologyAO, String serviceId) throws IOException {
		ExecuteResult<String> result = new ExecuteResult<>();
		//查询namespace
		ExecuteResult<com.ladeit.pojo.doo.Service> service = this.serviceService.getById(serviceId);
		Env env = this.envService.getEnvById(service.getResult().getEnvId());
		Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
		// 封装一个VirtualService
		// 基本属性
		VirtualService virtualService = new VirtualService();
		virtualService.setKind("VirtualService");
		virtualService.setApiVersion("networking.istio.io/v1alpha3");
		ObjectMeta objectMeta = new ObjectMeta();
		objectMeta.setNamespace(env.getNamespace());
		objectMeta.setName("virtualservice-" + service.getResult().getName());
		virtualService.setMetadata(objectMeta);
		// 特殊属性
		VirtualServiceSpec spec = new VirtualServiceSpec();
		spec.setHosts(topologyAO.getHost());
		List<String> gateways = new ArrayList<>();
		gateways.add("gateway-" + service.getResult().getName());
		spec.setGateways(gateways);
		List<HTTPRoute> httpRoutes = new ArrayList<>();
		topologyAO.getMatch().stream().forEach(match -> {
			HTTPRoute httpRoute = new HTTPRoute();
			// 处理redirect数据
			if (match.getRedirect() != null) {
				Redirect redirect = match.getRedirect();
				HTTPRedirect httpRedirect = new HTTPRedirect();
				BeanUtils.copyProperties(redirect, httpRedirect);
				httpRoute.setRedirect(httpRedirect);
			}
			// 处理rewrite数据
			if (match.getRewrite() != null) {
				Rewrite rewrite = match.getRewrite();
				HTTPRewrite httpRewrite = new HTTPRewrite();
				BeanUtils.copyProperties(rewrite, httpRewrite);
				httpRoute.setRewrite(httpRewrite);
			}
			// 处理Retries数据
			if (match.getRetries() != null) {
				Retries retries = match.getRetries();
				HTTPRetry httpRetry = new HTTPRetry();
				BeanUtils.copyProperties(retries, httpRetry);
				Duration duration = new Duration(0, retries.getPerTryTimeout());
				httpRetry.setPerTryTimeout(duration);
				httpRoute.setRetries(httpRetry);
			}
			// 处理headers数据
			if (match.getHeaders() != null) {
				Header header = match.getHeaders();
				Headers headers = new Headers();
				HeaderOperations headerOperations = new HeaderOperations();
				if (header.getAdd() != null && header.getAdd().size() != 0) {
					Map<String, String> addMap = header.getAdd().stream().map(add -> {
						String key = add.split(":")[0];
						String value = add.split(":")[1];
						Map<String, String> map = new HashMap<>();
						map.put(key, value);
						return map;
					}).collect(Collectors.toList()).stream().reduce((pre, next) -> {
						next.putAll(pre);
						return next;
					}).get();
					headerOperations.setAdd(addMap);
				}
				if (header.getSet() != null && header.getSet().size() != 0) {
					Map<String, String> setMap = header.getSet().stream().map(set -> {
						String key = set.split(":")[0];
						String value = set.split(":")[1];
						Map<String, String> map = new HashMap<>();
						map.put(key, value);
						return map;
					}).collect(Collectors.toList()).stream().reduce((pre, next) -> {
						next.putAll(pre);
						return next;
					}).get();
					headerOperations.setSet(setMap);
				}
				headerOperations.setRemove(header.getRemove());
				headers.setRequest(headerOperations);
				httpRoute.setHeaders(headers);
			}
			// 处理CorsPolicy数据
			if (match.getCorsPolicy() != null) {
				CorsPolicy corsPolicy = new CorsPolicy();
				BeanUtils.copyProperties(match.getCorsPolicy(), corsPolicy);
				//BoolValue allowCredentials = new BoolValue();
				//allowCredentials.setValue(match.getCorsPolicy().getAllowCredentials());
				//corsPolicy.setAllowCredentials(allowCredentials);
				Duration maxage = new Duration(0, match.getCorsPolicy().getMaxAge());
				corsPolicy.setMaxAge(maxage);
				httpRoute.setCorsPolicy(corsPolicy);
			}
			List<HTTPMatchRequest> httpMatchRequests = new ArrayList<>();
			match.getRule().stream().forEach(rule -> {
				HTTPMatchRequest httpMatchRequest = new HTTPMatchRequest();
				rule.getStringMatch().stream().forEach(stringMatch -> {
					me.snowdrop.istio.api.networking.v1alpha3.StringMatch istioStringMatch =
							new me.snowdrop.istio.api.networking.v1alpha3.StringMatch();
					Map<String, me.snowdrop.istio.api.networking.v1alpha3.StringMatch> map = new HashMap<>();
					if ("Authority".equals(stringMatch.getType())) {
						if ("exact".equals(stringMatch.getExpression())) {
							ExactMatchType exactMatchType = new ExactMatchType();
							exactMatchType.setExact(stringMatch.getValue());
							istioStringMatch.setMatchType(exactMatchType);
						} else if ("prefix".equals(stringMatch.getExpression())) {
							PrefixMatchType prefixMatchType = new PrefixMatchType();
							prefixMatchType.setPrefix(stringMatch.getValue());
							istioStringMatch.setMatchType(prefixMatchType);
						} else if ("regex".equals(stringMatch.getExpression())) {
							RegexMatchType regexMatchType = new RegexMatchType();
							regexMatchType.setRegex(stringMatch.getValue());
							istioStringMatch.setMatchType(regexMatchType);
						}
						httpMatchRequest.setAuthority(istioStringMatch);
					} else if ("method".equals(stringMatch.getType())) {
						if ("exact".equals(stringMatch.getExpression())) {
							ExactMatchType exactMatchType = new ExactMatchType();
							exactMatchType.setExact(stringMatch.getValue());
							istioStringMatch.setMatchType(exactMatchType);
						} else if ("prefix".equals(stringMatch.getExpression())) {
							PrefixMatchType prefixMatchType = new PrefixMatchType();
							prefixMatchType.setPrefix(stringMatch.getValue());
							istioStringMatch.setMatchType(prefixMatchType);
						} else if ("regex".equals(stringMatch.getExpression())) {
							RegexMatchType regexMatchType = new RegexMatchType();
							regexMatchType.setRegex(stringMatch.getValue());
							istioStringMatch.setMatchType(regexMatchType);
						}
						httpMatchRequest.setMethod(istioStringMatch);
					} else if ("scheme".equals(stringMatch.getType())) {
						if ("exact".equals(stringMatch.getExpression())) {
							ExactMatchType exactMatchType = new ExactMatchType();
							exactMatchType.setExact(stringMatch.getValue());
							istioStringMatch.setMatchType(exactMatchType);
						} else if ("prefix".equals(stringMatch.getExpression())) {
							PrefixMatchType prefixMatchType = new PrefixMatchType();
							prefixMatchType.setPrefix(stringMatch.getValue());
							istioStringMatch.setMatchType(prefixMatchType);
						} else if ("regex".equals(stringMatch.getExpression())) {
							RegexMatchType regexMatchType = new RegexMatchType();
							regexMatchType.setRegex(stringMatch.getValue());
							istioStringMatch.setMatchType(regexMatchType);
						}
						httpMatchRequest.setScheme(istioStringMatch);
					} else if ("uri".equals(stringMatch.getType())) {
						if ("exact".equals(stringMatch.getExpression())) {
							ExactMatchType exactMatchType = new ExactMatchType();
							exactMatchType.setExact(stringMatch.getValue());
							istioStringMatch.setMatchType(exactMatchType);
						} else if ("prefix".equals(stringMatch.getExpression())) {
							PrefixMatchType prefixMatchType = new PrefixMatchType();
							prefixMatchType.setPrefix(stringMatch.getValue());
							istioStringMatch.setMatchType(prefixMatchType);
						} else if ("regex".equals(stringMatch.getExpression())) {
							RegexMatchType regexMatchType = new RegexMatchType();
							regexMatchType.setRegex(stringMatch.getValue());
							istioStringMatch.setMatchType(regexMatchType);
						}
						httpMatchRequest.setUri(istioStringMatch);
					} else if ("headers".equals(stringMatch.getType())) {
						if ("exact".equals(stringMatch.getExpression())) {
							ExactMatchType exactMatchType = new ExactMatchType();
							exactMatchType.setExact(stringMatch.getValue());
							istioStringMatch.setMatchType(exactMatchType);
						} else if ("prefix".equals(stringMatch.getExpression())) {
							PrefixMatchType prefixMatchType = new PrefixMatchType();
							prefixMatchType.setPrefix(stringMatch.getValue());
							istioStringMatch.setMatchType(prefixMatchType);
						} else if ("regex".equals(stringMatch.getExpression())) {
							RegexMatchType regexMatchType = new RegexMatchType();
							regexMatchType.setRegex(stringMatch.getValue());
							istioStringMatch.setMatchType(regexMatchType);
						}
						map.put(stringMatch.getKey(), istioStringMatch);
						httpMatchRequest.setHeaders(map);
					}
					httpMatchRequests.add(httpMatchRequest);
				});
			});
			List<HTTPRouteDestination> httpRouteDestinations = new ArrayList<>();
			httpRoute.setMatch(httpMatchRequests);
			topologyAO.getMap().stream().forEach(map -> {
				if (map.getMatchId().equals(match.getId())) {
					topologyAO.getRoute().stream().forEach(route -> {
						if (map.getRouteId().equals(route.getId())) {
							HTTPRouteDestination httpRouteDestination = new HTTPRouteDestination();
							httpRouteDestination.setWeight(map.getWeight());
							Destination destination = new Destination();
							destination.setHost(route.getHost());
							destination.setSubset(route.getSubset());
							httpRouteDestination.setDestination(destination);
							httpRouteDestinations.add(httpRouteDestination);
						}
					});
				}
			});
			httpRoute.setRoute(httpRouteDestinations);
			httpRoutes.add(httpRoute);
		});
		spec.setHttp(httpRoutes);
		virtualService.setSpec(spec);
		// 封装一个Gateway
		Gateway gateway = new Gateway();
		GatewaySpec gatewaySpec = new GatewaySpec();
		Map<String, String> seletor = new HashMap<>();
		seletor.put("istio", "ingressgateway");
		gatewaySpec.setSelector(seletor);
		List<Server> servers = new ArrayList<>();
		topologyAO.getHost().stream().forEach(host -> {
			Server server = new Server();
			server.setHosts(topologyAO.getHost());
			Port port = new Port();
			port.setName("http");
			port.setProtocol("HTTP");
			port.setNumber(80);
			server.setPort(port);
			servers.add(server);
		});
		gatewaySpec.setServers(servers);
		gateway.setSpec(gatewaySpec);
		ObjectMeta gatewayMeta = new ObjectMeta();
		gatewayMeta.setNamespace(env.getNamespace());
		gatewayMeta.setName("gateway-" + service.getResult().getName());
		gateway.setMetadata(gatewayMeta);
		try {
			this.istioManager.createVirtualServices(cluster.getK8sKubeconfig(), virtualService);
			this.istioManager.createGateway(cluster.getK8sKubeconfig(), gateway);
		} catch (Exception e) {
			result.addErrorMessage(e.getLocalizedMessage());
			result.setCode(Code.FAILED);
		}
		Message message = new Message();
		ExecuteResult<ServiceGroup> sgr = serviceGroupService.getGroupById(service.getResult().getServiceGroupId());
		ServiceGroup sg = sgr.getResult();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		message.setId(UUID.randomUUID().toString());
		message.setCreateAt(new Date());
		// Successfully.
		message.setContent(sg.getName() + "/" + service.getResult().getName() + " topology tuned successfully.");
		message.setLevel("NORMAL");
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		message.setOperuserId(user.getId());
		message.setServiceGroupId(service.getResult().getServiceGroupId());
		message.setServiceId(service.getResult().getId());
		message.setTargetId(serviceId);
		// Successfully.
		message.setTitle(sg.getName() + "/" + service.getResult().getName() + " topology tuned successfully.");
		message.setType(CommonConsant.MESSAGE_TYPE_4);
		message.setMessageType(CommonConsant.MESSAGE_TYPE_S);
		this.messageService.insertMessage(message, true);
		this.messageService.insertSlackMessage(message);
		return result;
	}

	/**
	 * 查询分组拓扑图
	 *
	 * @param groupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.GroupTopologyAO>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<GroupTopologyAO> getGroupTopology(String groupId) {
		ExecuteResult<GroupTopologyAO> result = new ExecuteResult<>();
		GroupTopologyAO groupTopology = new GroupTopologyAO();
		List<TopologyAO> list = new ArrayList<>();
		groupTopology.setTopologys(list);
		result.setResult(groupTopology);
		List<com.ladeit.pojo.doo.Service> services = this.serviceService.getServiceByGroupId(groupId);
		services.stream().forEach(service -> {
			TopologyAO topologyAO = new TopologyAO();
			if (!service.getStatus().equals("-1")) {
				ExecuteResult<TopologyAO> resultService = null;
				try {
					resultService = this.getTopology(service.getId());
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
				list.add(resultService.getResult());
			}
		});
		return result;
	}

	/**
	 * 查看service的配置信息（每次更新时候用）
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ConfigurationAO>
	 * @author falcomlife
	 * @date 20-4-6
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<ConfigurationAO> getConfiguration(String serviceId) throws IOException, ApiException {
		ExecuteResult<ConfigurationAO> result = new ExecuteResult<>();
		ConfigurationAO configuration = new ConfigurationAO();
		ExecuteResult<com.ladeit.pojo.doo.Service> service = this.serviceService.getById(serviceId);
		Env env = this.envService.getEnvById(this.serviceService.getById(serviceId).getResult().getEnvId());
		Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
		ExecuteResult<List<V1Deployment>> depRes = this.k8sWorkLoadsManager.getDeployment(serviceId,
				cluster.getK8sKubeconfig());
		ExecuteResult<List<V1StatefulSet>> sfRes = this.k8sWorkLoadsManager.getStatefulSet(serviceId,
				cluster.getK8sKubeconfig());
		ExecuteResult<List<V1ReplicationController>> rcRes =
				this.k8sWorkLoadsManager.getReplicationControllers(serviceId
						, cluster.getK8sKubeconfig());
		ExecuteResult<List<V1Service>> svcRes = this.k8sWorkLoadsManager.getService(serviceId,
				cluster.getK8sKubeconfig());
		VirtualService virtualService = this.istioManager.getVirtualservice(cluster.getK8sKubeconfig(),
				"virtualservice-" + service.getResult().getName(), env.getNamespace());
		List<V1ResourceQuota> rqs = this.k8sClusterManager.getResourceQuota(env.getNamespace(),
				cluster.getK8sKubeconfig());
		if (depRes.getResult() != null && !depRes.getResult().isEmpty()) {
			V1Deployment dep = depRes.getResult().get(0);
			V1Container container = dep.getSpec().getTemplate().getSpec().getContainers().get(0);
			configuration.setArgs(container.getArgs());
			configuration.setCommand(container.getCommand().get(0));
			if (configuration.getCommand() != null && !container.getCommand().isEmpty()) {
				configuration.setCommand(container.getCommand().get(0));
			}
			Quantity limitscpu = container.getResources().getLimits().get("cpu");
			Quantity limitsmemory = container.getResources().getLimits().get("memory");
			Quantity requestscpu = container.getResources().getRequests().get("cpu");
			Quantity requestsmemory = container.getResources().getRequests().get("memory");
			if (limitscpu != null) {
				configuration.setCpuLimit(Integer.parseInt(UnitUtil.stripNumberUnit(limitscpu.toSuffixedString())[0]));
				String unit = UnitUtil.stripNumberUnit(limitscpu.toSuffixedString())[1];
				if (unit == null) {
					configuration.setCpuLimitUnit("core");
				} else {
					configuration.setCpuLimitUnit(unit);
				}
			}
			if (limitsmemory != null) {
				configuration.setMemLimit(Integer.parseInt(UnitUtil.stripNumberUnit(limitsmemory.toSuffixedString())[0]));
				configuration.setMemLimitUnit(UnitUtil.stripNumberUnit(limitsmemory.toSuffixedString())[1]);
			}
			if (requestscpu != null) {
				configuration.setCpuRequest(Integer.parseInt(UnitUtil.stripNumberUnit(requestscpu.toSuffixedString())[0]));
				String unit = UnitUtil.stripNumberUnit(requestscpu.toSuffixedString())[1];
				if (unit == null) {
					configuration.setCpuRequestUnit("core");
				} else {
					configuration.setCpuRequestUnit(unit);
				}
			}
			if (requestsmemory != null) {
				configuration.setMemRequest(Integer.parseInt(UnitUtil.stripNumberUnit(requestsmemory.toSuffixedString())[0]));
				configuration.setMemRequestUnit(UnitUtil.stripNumberUnit(requestsmemory.toSuffixedString())[1]);
			}
			if (container.getEnv() != null) {
				List<com.ladeit.pojo.ao.configuration.Env> envs = container.getEnv().stream().map(e -> {
					com.ladeit.pojo.ao.configuration.Env envInner = new com.ladeit.pojo.ao.configuration.Env();
					envInner.setKey(e.getName());
					envInner.setValue(e.getValue());
					return envInner;
				}).collect(Collectors.toList());
				configuration.setEnvs(envs);
			}
			if (container.getLivenessProbe() != null) {
				LivenessProbe l = new LivenessProbe();
				l.setCommand(container.getLivenessProbe().getExec().getCommand().get(0));
				l.setFailureThreshold(container.getLivenessProbe().getFailureThreshold());
				l.setHeads(container.getLivenessProbe().getHttpGet().getHttpHeaders().stream().map(header -> {
					Map<String, String> map = new HashMap<>();
					map.put(header.getName(), header.getValue());
					return map;
				}).collect(Collectors.toList()));
				l.setInitialDelaySeconds(container.getLivenessProbe().getInitialDelaySeconds());
				l.setPath(container.getLivenessProbe().getHttpGet().getPath());
				l.setPeriodSeconds(container.getLivenessProbe().getPeriodSeconds());
				l.setPort(container.getLivenessProbe().getHttpGet().getPort().getIntValue());
				l.setProtocol(container.getLivenessProbe().getHttpGet().getScheme());
				l.setSuccessThreshold(container.getLivenessProbe().getSuccessThreshold());
				l.setTimeoutSeconds(container.getLivenessProbe().getTimeoutSeconds());
				configuration.setLivenessProbe(l);
			}
			configuration.setReplicas(dep.getSpec().getReplicas());
			configuration.setResourceQuota(rqs.isEmpty() ? false : true);
			configuration.setType("Deployment");
			if (dep.getSpec().getTemplate().getSpec().getVolumes() != null) {
				List<Volume> volumes = dep.getSpec().getTemplate().getSpec().getVolumes().stream().map(v1Volume -> {
					Volume v = new Volume();
					v.setName(v1Volume.getName());
					v.setPath(v1Volume.getHostPath().getPath());
					v.setType("PVC");
					return v;
				}).collect(Collectors.toList());
				configuration.setVolumes(volumes);
			}
		}
		if (sfRes.getResult() != null && !sfRes.getResult().isEmpty()) {
			V1StatefulSet dep = sfRes.getResult().get(0);
			V1Container container = dep.getSpec().getTemplate().getSpec().getContainers().get(0);
			configuration.setArgs(container.getArgs());
			configuration.setCommand(container.getCommand().get(0));
			Quantity limitscpu = container.getResources().getLimits().get("cpu");
			Quantity limitsmemory = container.getResources().getLimits().get("memory");
			Quantity requestscpu = container.getResources().getRequests().get("cpu");
			Quantity requestsmemory = container.getResources().getRequests().get("memory");

			if (limitscpu != null) {
				configuration.setCpuLimit(Integer.parseInt(UnitUtil.stripNumberUnit(limitscpu.toSuffixedString())[0]));
				String unit = UnitUtil.stripNumberUnit(limitscpu.toSuffixedString())[1];
				if (unit == null) {
					configuration.setCpuLimitUnit("core");
				} else {
					configuration.setCpuLimitUnit(unit);
				}
			}
			if (limitsmemory != null) {
				configuration.setMemLimit(Integer.parseInt(UnitUtil.stripNumberUnit(limitsmemory.toSuffixedString())[0]));
				configuration.setMemLimitUnit(UnitUtil.stripNumberUnit(limitsmemory.toSuffixedString())[1]);
			}
			if (requestscpu != null) {
				configuration.setCpuRequest(Integer.parseInt(UnitUtil.stripNumberUnit(requestscpu.toSuffixedString())[0]));
				String unit = UnitUtil.stripNumberUnit(requestscpu.toSuffixedString())[1];
				if (unit == null) {
					configuration.setCpuRequestUnit("core");
				} else {
					configuration.setCpuRequestUnit(unit);
				}
			}
			if (requestsmemory != null) {
				configuration.setMemRequest(Integer.parseInt(UnitUtil.stripNumberUnit(requestsmemory.toSuffixedString())[0]));
				configuration.setMemRequestUnit(UnitUtil.stripNumberUnit(requestsmemory.toSuffixedString())[1]);
			}
			if(container.getEnv() != null){
				List<com.ladeit.pojo.ao.configuration.Env> envs = container.getEnv().stream().map(e -> {
					com.ladeit.pojo.ao.configuration.Env envInner = new com.ladeit.pojo.ao.configuration.Env();
					envInner.setKey(e.getName());
					envInner.setValue(e.getValue());
					return envInner;
				}).collect(Collectors.toList());
				configuration.setEnvs(envs);
			}
			LivenessProbe l = new LivenessProbe();
			if(container.getLivenessProbe() != null){
				if(container.getLivenessProbe().getExec()!=null && container.getLivenessProbe().getExec().getCommand()!=null && !container.getLivenessProbe().getExec().getCommand().isEmpty()){
					l.setCommand(container.getLivenessProbe().getExec().getCommand().get(0));
				}
				l.setFailureThreshold(container.getLivenessProbe().getFailureThreshold());l.setHeads(container.getLivenessProbe().getHttpGet().getHttpHeaders().stream().map(header -> {
					Map<String, String> map = new HashMap<>();
					map.put(header.getName(), header.getValue());
					return map;
				}).collect(Collectors.toList()));
				if(container.getLivenessProbe().getHttpGet() != null){
					l.setPath(container.getLivenessProbe().getHttpGet().getPath());
					l.setPort(container.getLivenessProbe().getHttpGet().getPort().getIntValue());
					l.setProtocol(container.getLivenessProbe().getHttpGet().getScheme());
				}
				l.setInitialDelaySeconds(container.getLivenessProbe().getInitialDelaySeconds());
				l.setPeriodSeconds(container.getLivenessProbe().getPeriodSeconds());
				l.setSuccessThreshold(container.getLivenessProbe().getSuccessThreshold());
				l.setTimeoutSeconds(container.getLivenessProbe().getTimeoutSeconds());
			}
			configuration.setLivenessProbe(l);
			configuration.setReplicas(dep.getSpec().getReplicas());
			configuration.setResourceQuota(rqs.isEmpty() ? false : true);
			configuration.setType("Statefulset");
			if(dep.getSpec().getTemplate().getSpec().getVolumes() != null) {
				List<Volume> volumes = dep.getSpec().getTemplate().getSpec().getVolumes().stream().map(v1Volume -> {
					Volume v = new Volume();
					v.setName(v1Volume.getName());
					v.setPath(v1Volume.getHostPath().getPath());
					v.setType("PVC");
					return v;
				}).collect(Collectors.toList());
				configuration.setVolumes(volumes);
			}
		}
		if (rcRes.getResult() != null && !rcRes.getResult().isEmpty()) {
			V1ReplicationController dep = rcRes.getResult().get(0);
			V1Container container = dep.getSpec().getTemplate().getSpec().getContainers().get(0);
			configuration.setArgs(container.getArgs());
			configuration.setCommand(container.getCommand().get(0));
			Quantity limitscpu = container.getResources().getLimits().get("cpu");
			Quantity limitsmemory = container.getResources().getLimits().get("memory");
			Quantity requestscpu = container.getResources().getRequests().get("cpu");
			Quantity requestsmemory = container.getResources().getRequests().get("memory");
			if (limitscpu != null) {
				configuration.setCpuLimit(Integer.parseInt(UnitUtil.stripNumberUnit(limitscpu.toSuffixedString())[0]));
				String unit = UnitUtil.stripNumberUnit(limitscpu.toSuffixedString())[1];
				if (unit == null) {
					configuration.setCpuLimitUnit("core");
				} else {
					configuration.setCpuLimitUnit(unit);
				}
			}
			if (limitsmemory != null) {
				configuration.setMemLimit(Integer.parseInt(UnitUtil.stripNumberUnit(limitsmemory.toSuffixedString())[0]));
				configuration.setMemLimitUnit(UnitUtil.stripNumberUnit(limitsmemory.toSuffixedString())[1]);
			}
			if (requestscpu != null) {
				configuration.setCpuRequest(Integer.parseInt(UnitUtil.stripNumberUnit(requestscpu.toSuffixedString())[0]));
				String unit = UnitUtil.stripNumberUnit(requestscpu.toSuffixedString())[1];
				if (unit == null) {
					configuration.setCpuRequestUnit("core");
				} else {
					configuration.setCpuRequestUnit(unit);
				}
			}
			if (requestsmemory != null) {
				configuration.setMemRequest(Integer.parseInt(UnitUtil.stripNumberUnit(requestsmemory.toSuffixedString())[0]));
				configuration.setMemRequestUnit(UnitUtil.stripNumberUnit(requestsmemory.toSuffixedString())[1]);
			}
			List<com.ladeit.pojo.ao.configuration.Env> envs = container.getEnv().stream().map(e -> {
				com.ladeit.pojo.ao.configuration.Env envInner = new com.ladeit.pojo.ao.configuration.Env();
				envInner.setKey(e.getName());
				envInner.setValue(e.getValue());
				return envInner;
			}).collect(Collectors.toList());
			configuration.setEnvs(envs);
			LivenessProbe l = new LivenessProbe();
			l.setCommand(container.getLivenessProbe().getExec().getCommand().get(0));
			l.setFailureThreshold(container.getLivenessProbe().getFailureThreshold());
			l.setHeads(container.getLivenessProbe().getHttpGet().getHttpHeaders().stream().map(header -> {
				Map<String, String> map = new HashMap<>();
				map.put(header.getName(), header.getValue());
				return map;
			}).collect(Collectors.toList()));
			l.setInitialDelaySeconds(container.getLivenessProbe().getInitialDelaySeconds());
			l.setPath(container.getLivenessProbe().getHttpGet().getPath());
			l.setPeriodSeconds(container.getLivenessProbe().getPeriodSeconds());
			l.setPort(container.getLivenessProbe().getHttpGet().getPort().getIntValue());
			l.setProtocol(container.getLivenessProbe().getHttpGet().getScheme());
			l.setSuccessThreshold(container.getLivenessProbe().getSuccessThreshold());
			l.setTimeoutSeconds(container.getLivenessProbe().getTimeoutSeconds());
			configuration.setLivenessProbe(l);
			configuration.setReplicas(dep.getSpec().getReplicas());
			configuration.setResourceQuota(rqs.isEmpty() ? false : true);
			configuration.setType("Deployment");
			List<Volume> volumes = dep.getSpec().getTemplate().getSpec().getVolumes().stream().map(v1Volume -> {
				Volume v = new Volume();
				v.setName(v1Volume.getName());
				v.setPath(v1Volume.getHostPath().getPath());
				v.setType("PVC");
				return v;
			}).collect(Collectors.toList());
			configuration.setVolumes(volumes);
		}
		configuration.setHost(virtualService.getSpec().getHosts());
		List<com.ladeit.pojo.ao.configuration.Port> ports =
				svcRes.getResult().get(0).getSpec().getPorts().stream().map(v1ServicePort -> {
					com.ladeit.pojo.ao.configuration.Port p = new com.ladeit.pojo.ao.configuration.Port();
					p.setContainerPort(v1ServicePort.getTargetPort().getIntValue());
					p.setName(v1ServicePort.getName());
					p.setServicePort(v1ServicePort.getPort());
					return p;
				}).collect(Collectors.toList());
		configuration.setPorts(ports);
		result.setResult(configuration);
		return result;
	}

	/**
	 * 解析策略
	 *
	 * @param invokeList
	 * @param type
	 * @param name
	 * @param result
	 * @return void
	 * @author falcomlife
	 * @date 19-12-17
	 * @version 1.0.0
	 */
	private void resolvingPolicy(List invokeList, String type, String name, ExecuteResult<String> result) {
		String yaml = null;
		if (invokeList != null) {
			if ("replicationControllers".equals(type) || "ReplicationController".equals(type)) {
				List<V1ReplicationController> list = invokeList;
				for (V1ReplicationController content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("deployments".equals(type) || "Deployment".equals(type)) {
				List<V1Deployment> list = invokeList;
				for (V1Deployment content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("statefulSets".equals(type) || "StatefulSet".equals(type)) {
				List<V1StatefulSet> list = invokeList;
				for (V1StatefulSet content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("jobs".equals(type) || "Job".equals(type)) {
				List<V1Job> list = invokeList;
				for (V1Job content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("cronJobs".equals(type) || "CronJob".equals(type)) {
				List<V1beta1CronJob> list = invokeList;
				for (V1beta1CronJob content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("daemonSets".equals(type) || "DaemonSet".equals(type)) {
				List<V1DaemonSet> list = invokeList;
				for (V1DaemonSet content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("pods".equals(type) || "Pod".equals(type)) {
				List<V1Pod> list = invokeList;
				for (V1Pod content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("services".equals(type) || "Service".equals(type)) {
				List<V1Service> list = invokeList;
				for (V1Service content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("ingresses".equals(type) || "Ingress".equals(type)) {
				List<V1beta1Ingress> list = invokeList;
				for (V1beta1Ingress content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("configMaps".equals(type) || "ConfigMap".equals(type)) {
				List<V1ConfigMap> list = invokeList;
				for (V1ConfigMap content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("secrets".equals(type) || "Secret".equals(type)) {
				List<V1Secret> list = invokeList;
				for (V1Secret content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("serviceAccounts".equals(type) || "ServiceAccount".equals(type)) {
				List<V1ServiceAccount> list = invokeList;
				for (V1ServiceAccount content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("persistentVolumes".equals(type) || "PersistentVolume".equals(type)) {
				List<V1PersistentVolume> list = invokeList;
				for (V1PersistentVolume content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("persistentVolumeClaims".equals(type) || "PersistentVolumeClaim".equals(type)) {
				List<V1PersistentVolumeClaim> list = invokeList;
				for (V1PersistentVolumeClaim content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			} else if ("storageClasses".equals(type) || "StorageClass".equals(type)) {
				List<V1StorageClass> list = invokeList;
				for (V1StorageClass content : list) {
					if (name.equals(content.getMetadata().getName())) {
						yaml = Yaml.dump(content);
						break;
					}
				}
			}
			result.setResult(yaml);
		} else {
			String message = messageUtils.matchMessage("M0019", new Object[]{}, Boolean.TRUE);
			result.addWarningMessage(message);
			result.setCode(Code.NOTFOUND);
		}
	}

	/**
	 * 查询wordloads
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.WorkLoadAO>>
	 * @author falcomlife
	 * @date 19-12-7
	 * @version 1.0.0
	 */
	@Deprecated
	@Override
	public ExecuteResult<List<WorkLoadAO>> getWorkLoads(String serviceId) throws IOException {
		ExecuteResult<List<WorkLoadAO>> result = new ExecuteResult<>();
		ExecuteResult<List<V1DaemonSet>> daemonSets = this.k8sWorkLoadsManager.getDaemonSet(serviceId,
				this.getConfigByServiceId(serviceId));
		ExecuteResult<List<V1Deployment>> deployments = this.k8sWorkLoadsManager.getDeployment(serviceId,
				this.getConfigByServiceId(serviceId));
		ExecuteResult<List<V1Job>> jobs = this.k8sWorkLoadsManager.getJob(serviceId,
				this.getConfigByServiceId(serviceId));
		ExecuteResult<List<V1StatefulSet>> statefulSets = this.k8sWorkLoadsManager.getStatefulSet(serviceId,
				this.getConfigByServiceId(serviceId));
		ExecuteResult<List<V1ReplicationController>> replicationControllers =
				this.k8sWorkLoadsManager.getReplicationControllers(serviceId, this.getConfigByServiceId(serviceId));
		ExecuteResult<List<V1beta1CronJob>> cronJobs = this.k8sWorkLoadsManager.getCronJobs(serviceId,
				this.getConfigByServiceId(serviceId));
		ExecuteResult<List<V1Pod>> pods = this.k8sWorkLoadsManager.getPods(serviceId,
				this.getConfigByServiceId(serviceId));
		List<WorkLoadAO> w1 = daemonSets.getResult().stream().map(ds -> {
			WorkLoadAO workLoadAO = new WorkLoadAO();
			workLoadAO.setName(ds.getMetadata().getName());
			workLoadAO.setNamespace(ds.getMetadata().getNamespace());
			workLoadAO.setEnv(ds.getMetadata().getNamespace());
			workLoadAO.setStatus(ds.getStatus().getDesiredNumberScheduled().equals(ds.getStatus().getNumberReady()) ?
					"RUN" : "ERROR");
			workLoadAO.setDesire(ds.getStatus().getDesiredNumberScheduled());
			workLoadAO.setCount(ds.getStatus().getNumberReady());
			workLoadAO.setType(ds.getKind());
			return workLoadAO;
		}).collect(Collectors.toList());

		List<WorkLoadAO> w2 = deployments.getResult().stream().map(ds -> {
			WorkLoadAO workLoadAO = new WorkLoadAO();
			workLoadAO.setName(ds.getMetadata().getName());
			workLoadAO.setNamespace(ds.getMetadata().getNamespace());
			workLoadAO.setEnv(ds.getMetadata().getNamespace());
			workLoadAO.setStatus(ds.getStatus().getReplicas().equals(ds.getStatus().getReadyReplicas()) ? "RUN" :
					"ERROR");
			workLoadAO.setDesire(ds.getStatus().getReplicas());
			workLoadAO.setCount(ds.getStatus().getReadyReplicas());
			workLoadAO.setType(ds.getKind());
			return workLoadAO;
		}).collect(Collectors.toList());

		List<WorkLoadAO> w3 = jobs.getResult().stream().map(ds -> {
			WorkLoadAO workLoadAO = new WorkLoadAO();
			workLoadAO.setName(ds.getMetadata().getName());
			workLoadAO.setNamespace(ds.getMetadata().getNamespace());
			workLoadAO.setEnv(ds.getMetadata().getNamespace());
			workLoadAO.setType(ds.getKind());
			return workLoadAO;
		}).collect(Collectors.toList());

		List<WorkLoadAO> w4 = statefulSets.getResult().stream().map(ds -> {
			WorkLoadAO workLoadAO = new WorkLoadAO();
			workLoadAO.setName(ds.getMetadata().getName());
			workLoadAO.setNamespace(ds.getMetadata().getNamespace());
			workLoadAO.setEnv(ds.getMetadata().getNamespace());
			workLoadAO.setStatus(ds.getStatus().getReplicas().equals(ds.getStatus().getReadyReplicas()) ? "RUN" :
					"ERROR");
			workLoadAO.setDesire(ds.getStatus().getReplicas());
			workLoadAO.setCount(ds.getStatus().getReadyReplicas());
			workLoadAO.setType(ds.getKind());
			return workLoadAO;
		}).collect(Collectors.toList());

		List<WorkLoadAO> w5 = replicationControllers.getResult().stream().map(ds -> {
			WorkLoadAO workLoadAO = new WorkLoadAO();
			workLoadAO.setName(ds.getMetadata().getName());
			workLoadAO.setNamespace(ds.getMetadata().getNamespace());
			workLoadAO.setEnv(ds.getMetadata().getNamespace());
			workLoadAO.setStatus(ds.getStatus().getReplicas().equals(ds.getStatus().getReadyReplicas()) ? "RUN" :
					"ERROR");
			workLoadAO.setDesire(ds.getStatus().getReplicas());
			workLoadAO.setCount(ds.getStatus().getReadyReplicas());
			workLoadAO.setType(ds.getKind());
			return workLoadAO;
		}).collect(Collectors.toList());

		List<WorkLoadAO> w6 = cronJobs.getResult().stream().map(ds -> {
			WorkLoadAO workLoadAO = new WorkLoadAO();
			workLoadAO.setName(ds.getMetadata().getName());
			workLoadAO.setNamespace(ds.getMetadata().getNamespace());
			workLoadAO.setEnv(ds.getMetadata().getNamespace());
			workLoadAO.setType(ds.getKind());
			return workLoadAO;
		}).collect(Collectors.toList());

		List<WorkLoadAO> w7 = pods.getResult().stream().map(ds -> {
			WorkLoadAO workLoadAO = new WorkLoadAO();
			workLoadAO.setName(ds.getMetadata().getName());
			workLoadAO.setNamespace(ds.getMetadata().getNamespace());
			workLoadAO.setEnv(ds.getMetadata().getNamespace());
			workLoadAO.setType(ds.getKind());
			return workLoadAO;
		}).collect(Collectors.toList());
		w1.addAll(w2);
		w1.addAll(w3);
		w1.addAll(w4);
		w1.addAll(w5);
		w1.addAll(w6);
		w1.addAll(w7);
		result.setResult(w1);

		return result;
	}

	/**
	 * 查询service
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ServiceIngressAO>>
	 * @author falcomlife
	 * @date 19-12-7
	 * @version 1.0.0
	 */
	@Deprecated
	@Override
	public ExecuteResult<List<ServiceIngressAO>> getServices(String serviceId) throws IOException {
		ExecuteResult<List<ServiceIngressAO>> result = new ExecuteResult<>();
		ExecuteResult<List<V1Service>> service = this.k8sWorkLoadsManager.getService(serviceId,
				this.getConfigByServiceId(serviceId));
		List<ServiceIngressAO> w1 = service.getResult().stream().map(ds -> {
			ServiceIngressAO serviceIngress = new ServiceIngressAO();
			serviceIngress.setName(ds.getMetadata().getName());
			serviceIngress.setClusterIp(ds.getSpec().getClusterIP());
			List<V1ServicePort> ports = ds.getSpec().getPorts();
			List<ServicePort> servicePorts = ports.stream().map(port -> {
				ServicePort servicePort = new ServicePort();
				servicePort.setName(port.getName());
				if (port.getNodePort() != null) {
					servicePort.setNodePort(port.getNodePort().toString());
				}
				if (port.getPort() != null) {
					servicePort.setPort(port.getPort().toString());
				}
				servicePort.setProtocal(port.getProtocol());
				if (port.getTargetPort() != null) {
					servicePort.setTargetPort(port.getTargetPort().toString());
				}
				return servicePort;
			}).collect(Collectors.toList());
			serviceIngress.setServicePort(servicePorts);
			List<String> ingresslist = new ArrayList<>();
			ExecuteResult<List<V1beta1Ingress>> ingress = this.k8sWorkLoadsManager.getIngress(serviceId,
					this.getConfigByServiceId(serviceId));
			List<V1beta1Ingress> ingresses = ingress.getResult();
			for (V1beta1Ingress ingressInner : ingresses) {
				for (V1beta1IngressRule rule : ingressInner.getSpec().getRules()) {
					for (V1beta1HTTPIngressPath path : rule.getHttp().getPaths()) {
						if (path.getBackend().getServiceName().equals(ds.getMetadata().getName())) {
							ingresslist.add(ingressInner.getMetadata().getName());
						}
					}
				}
			}
			serviceIngress.setIngress(ingresslist);
			return serviceIngress;
		}).collect(Collectors.toList());
		result.setResult(w1);
		return result;
	}

	/**
	 * 查询分类组件
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.TypesResourceAO>>
	 * @author falcomlife
	 * @date 19-12-7
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<TypesResourceAO>> getTypesResources(String serviceId) throws InvocationTargetException,
			IllegalAccessException, NoSuchMethodException, IOException, ApiException {
		ExecuteResult<List<TypesResourceAO>> result = new ExecuteResult<>();
		ApiClient apiClient = this.clientGenerateByServiceId(serviceId);
		List<TypesResourceAO> listResourcesResult = new ArrayList<>();
		// workloads
		this.packageWorkloads(listResourcesResult, serviceId, this.getConfigByServiceId(serviceId));
		// services
		this.packageServices(listResourcesResult, serviceId, this.getConfigByServiceId(serviceId));
		// configmap
		this.packageConfigMaps(listResourcesResult, serviceId, this.getConfigByServiceId(serviceId));
		// PVC
		this.packagePvcs(listResourcesResult, serviceId, this.getConfigByServiceId(serviceId));

		result.setResult(listResourcesResult);
		return result;
	}

	/**
	 * 查询分类组件
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.TypesResourceAO>>
	 * @author falcomlife
	 * @date 19-12-7
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<QueryResourceAO>> getTypesResourcesName(String serviceId) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, IOException, ApiException {
		ExecuteResult<List<QueryResourceAO>> result = new ExecuteResult<>();
		List<TypesResourceAO> listResourcesResult = new ArrayList<>();
		// workloads
		this.packageWorkloads(listResourcesResult, serviceId, this.getConfigByServiceId(serviceId));
		// services
		this.packageServices(listResourcesResult, serviceId, this.getConfigByServiceId(serviceId));
		// configmap
		this.packageConfigMaps(listResourcesResult, serviceId, this.getConfigByServiceId(serviceId));
		// PVC
		this.packagePvcs(listResourcesResult, serviceId, this.getConfigByServiceId(serviceId));

		List<QueryResourceAO> listResourcesResultNew = new ArrayList<>();
		for (TypesResourceAO typesResourceAO : listResourcesResult) {
			String type = typesResourceAO.getType();
			if ("Deployment".equals(type)) {
				QueryResourceAO typesResourceAONew = new QueryResourceAO();
				typesResourceAONew.setName(typesResourceAO.getName());
				typesResourceAONew.setChildren(getInnerResource(typesResourceAO.getChildren()));
				listResourcesResultNew.add(typesResourceAONew);
			}
		}
		result.setResult(listResourcesResultNew);
		return result;
	}

	private List<QueryResourceAO> getInnerResource(List<InnerResource> childrens) {
		List<QueryResourceAO> childs = new ArrayList<QueryResourceAO>();
		for (InnerResource innerResource : childrens) {
			if (innerResource instanceof PodResource) {
				QueryResourceAO queryResourceAO = new QueryResourceAO();
				PodResource pod = (PodResource) innerResource;
				queryResourceAO.setName(pod.getName());
				queryResourceAO.setChildren(getInnerResource(pod.getChildren()));
				childs.add(queryResourceAO);
			} else if (innerResource instanceof ContainerResource) {
				QueryResourceAO queryResourceAO = new QueryResourceAO();
				ContainerResource con = (ContainerResource) innerResource;
				queryResourceAO.setName(con.getName());
				childs.add(queryResourceAO);
			} else {
			}
		}
		return childs;
	}

	private List<InnerResource> packagePodChildren(String serviceId) {
		List<InnerResource> l1 = new ArrayList<>();
		ExecuteResult<List<V1Pod>> servicePods = this.k8sWorkLoadsManager.getPods(serviceId,
				this.getConfigByServiceId(serviceId));
		for (V1Pod pod : servicePods.getResult()) {
			PodResource child = new PodResource();
			child.setName(pod.getMetadata().getName());
			child.setLabels(pod.getMetadata().getLabels());
			child.setNodeName(pod.getSpec().getNodeName());
			child.setStatus(pod.getStatus().getPhase());
			List<InnerResource> containerResources = pod.getSpec().getContainers().stream().map(container -> {
				ContainerResource grandson = new ContainerResource();
				grandson.setName(container.getName());
				grandson.setImage(container.getImage());
				grandson.setCommand(container.getCommand());
				grandson.setArgs(container.getArgs());
				return grandson;
			}).collect(Collectors.toList());
			child.setChildren(containerResources);
			l1.add(child);
		}
		return l1;
	}

	private void packagePvcs(List<TypesResourceAO> listResourcesResult, String serviceId, String config) throws ApiException {
		// services
		ExecuteResult<List<V1PersistentVolumeClaim>> pvcs = this.k8sWorkLoadsManager.getPvcs(serviceId, config);
		// 循环所有ingress
		for (V1PersistentVolumeClaim pvc : pvcs.getResult()) {
			// 封装返回对象并扔到List中
			TypesResourceAO typesResourceAO = new TypesResourceAO();
			typesResourceAO.setType(pvc.getKind());
			typesResourceAO.setName(pvc.getMetadata().getName());
			typesResourceAO.setLabels(pvc.getMetadata().getLabels());
			typesResourceAO.setCreateAt(pvc.getMetadata().getCreationTimestamp().toDate());
			StorageClassResource scr = this.packageStorageClassChildren(pvc.getSpec().getStorageClassName(),
					config);
			Map<String, Object> info = new HashMap<>();
			info.put("accessmodes", pvc.getSpec().getAccessModes());
			info.put("resources", pvc.getSpec().getResources().getRequests());
			typesResourceAO.setInfo(info);
			List<InnerResource> storageClasses = new ArrayList<>();
			storageClasses.add(scr);
			typesResourceAO.setChildren(storageClasses);
			listResourcesResult.add(typesResourceAO);
		}
	}

	private StorageClassResource packageStorageClassChildren(String storageClassName, String config) throws ApiException {
		if (StringUtils.isNotBlank(storageClassName)) {
			V1StorageClass storageClass = this.k8sWorkLoadsManager.getStorageClass(config, storageClassName);
			List<V1PersistentVolume> pvs = this.k8sWorkLoadsManager.getPv(config, storageClassName);
			StorageClassResource scr = new StorageClassResource();
			scr.setName(storageClassName);
			if (storageClass != null) {
				scr.setLabels(storageClass.getMetadata().getLabels());
			}
			if (pvs != null && pvs.size() != 0) {
				List<InnerResource> pvResources = pvs.stream().map(pv -> {
					PVResource pvResource = new PVResource();
					pvResource.setName(pv.getMetadata().getName());
					pvResource.setConfiguration(pv.getSpec().getVolumeMode());
					return pvResource;
				}).collect(Collectors.toList());
				scr.setChildren(pvResources);
			}
			scr.setType("StorageClass");
			return scr;
		}
		return null;
	}

	private void packageConfigMaps(List<TypesResourceAO> listResourcesResult, String serviceId, String config) {
		// services
		ExecuteResult<List<V1ConfigMap>> configMap = this.k8sWorkLoadsManager.getConfigMap(serviceId, config);
		// 循环所有ingress
		for (V1ConfigMap confimap : configMap.getResult()) {
			// 封装返回对象并扔到List中
			TypesResourceAO typesResourceAO = new TypesResourceAO();
			typesResourceAO.setType(confimap.getKind());
			typesResourceAO.setName(confimap.getMetadata().getName());
			typesResourceAO.setLabels(confimap.getMetadata().getLabels());
			typesResourceAO.setCreateAt(confimap.getMetadata().getCreationTimestamp().toDate());
			typesResourceAO.setInfo(confimap.getData());
			listResourcesResult.add(typesResourceAO);
		}
	}

	private void packageWorkloads(List<TypesResourceAO> listResourcesResult, String serviceId, String config) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		List<Object> listResources = new ArrayList<>();
		// workloads
		ExecuteResult<List<V1DaemonSet>> daemonSets = this.k8sWorkLoadsManager.getDaemonSet(serviceId, config);
		ExecuteResult<List<V1Deployment>> deployments = this.k8sWorkLoadsManager.getDeployment(serviceId, config);
		ExecuteResult<List<V1Job>> jobs = this.k8sWorkLoadsManager.getJob(serviceId, config);
		ExecuteResult<List<V1StatefulSet>> statefulSets = this.k8sWorkLoadsManager.getStatefulSet(serviceId,
				config);
		ExecuteResult<List<V1ReplicationController>> replicationControllers =
				this.k8sWorkLoadsManager.getReplicationControllers(serviceId, config);
		ExecuteResult<List<V1beta1CronJob>> cronJobs = this.k8sWorkLoadsManager.getCronJobs(serviceId, config);

		listResources.addAll(daemonSets.getResult());
		listResources.addAll(deployments.getResult());
		listResources.addAll(jobs.getResult());
		listResources.addAll(statefulSets.getResult());
		listResources.addAll(replicationControllers.getResult());
		listResources.addAll(cronJobs.getResult());

		ExecuteResult<List<V1Pod>> pods = this.k8sWorkLoadsManager.getPods(serviceId,
				this.getConfigByServiceId(serviceId));
		List<V1Pod> podResult = new ArrayList<>();
		// 循环所有不是pod类型的workloads
		for (Object resource : listResources) {
			Object spec = resource.getClass().getMethod("getSpec").invoke(resource);
			V1LabelSelector labelSelector = (V1LabelSelector) spec.getClass().getMethod("getSelector").invoke(spec);
			V1PodTemplateSpec template = (V1PodTemplateSpec) spec.getClass().getMethod("getTemplate").invoke(spec);
			V1ObjectMeta resourceMeta = (V1ObjectMeta) resource.getClass().getMethod("getMetadata").invoke(resource);
			String kind = resource.getClass().getMethod("getKind").invoke(resource).toString();
			V1ObjectMeta podMeta = (V1ObjectMeta) template.getClass().getMethod("getMetadata").invoke(template);
			DateTime dateTime =
					(DateTime) resourceMeta.getClass().getMethod("getCreationTimestamp").invoke(resourceMeta);
			// 封装返回对象并扔到List中
			TypesResourceAO typesResourceAO = new TypesResourceAO();
			typesResourceAO.setType(kind);
			typesResourceAO.setName(resourceMeta.getName());
			typesResourceAO.setLabels(resourceMeta.getLabels());
			typesResourceAO.setCreateAt(dateTime.toDate());
			if ("DaemonSet".equals(kind)) {
				V1DaemonSet ds = (V1DaemonSet) resource;
				Integer numberReady = ds.getStatus().getNumberReady();
				Integer desiredNumberScheduled = ds.getStatus().getDesiredNumberScheduled();
				typesResourceAO.setInfo(numberReady == null ? 0 : numberReady + "/" + desiredNumberScheduled == null ?
						0 : desiredNumberScheduled);
			} else if ("Deployment".equals(kind)) {
				V1Deployment dm = (V1Deployment) resource;
				Integer readyReplicas = dm.getStatus().getReadyReplicas();
				Integer replicas = dm.getStatus().getReplicas();
				typesResourceAO.setInfo(readyReplicas == null ? 0 : readyReplicas + "/" + replicas == null ? 0 :
						replicas);
			} else if ("Job".equals(kind)) {
				// TODO 放入info
			} else if ("CronJob".equals(kind)) {
				// TODO 放入info
			} else if ("StatefulSet".equals(kind)) {
				V1StatefulSet ss = (V1StatefulSet) resource;
				Integer readyReplicas = ss.getStatus().getReadyReplicas();
				Integer replicas = ss.getStatus().getReplicas();
				typesResourceAO.setInfo(readyReplicas == null ? 0 : readyReplicas + "/" + replicas == null ? 0 :
						replicas);
			} else if ("ReplicationController".equals(kind)) {
				V1ReplicationController rc = (V1ReplicationController) resource;
				Integer readyReplicas = rc.getStatus().getReadyReplicas();
				Integer replicas = rc.getStatus().getReplicas();
				typesResourceAO.setInfo(readyReplicas == null ? 0 : readyReplicas + "/" + replicas == null ? 0 :
						replicas);
			}
			typesResourceAO.setChildren(this.packagePodChildren(serviceId));
			listResourcesResult.add(typesResourceAO);
			for (V1Pod v1pod : pods.getResult()) {
				if (v1pod.getMetadata().getOwnerReferences() == null) {
					podResult.add(v1pod);
				}
			}
		}
		// 循环所有pod类型的workloads
		for (V1Pod podIn : podResult) {
			TypesResourceAO typesResourceAO = new TypesResourceAO();
			typesResourceAO.setType("Pod");
			typesResourceAO.setName(podIn.getMetadata().getName());
			typesResourceAO.setLabels(podIn.getMetadata().getLabels());
			if (podIn.getStatus().getStartTime() != null) {
				typesResourceAO.setCreateAt(podIn.getStatus().getStartTime().toDate());
			}
			typesResourceAO.setInfo(podIn.getSpec().getNodeName() + ":" + podIn.getStatus().getPhase());
			typesResourceAO.setChildren(this.packagePodChildren(serviceId));
			listResourcesResult.add(typesResourceAO);


		}
	}

	private void packageServices(List<TypesResourceAO> listResourcesResult, String serviceId, String config) {
		// services
		ExecuteResult<List<V1Service>> services = this.k8sWorkLoadsManager.getService(serviceId, config);
		ExecuteResult<List<V1beta1Ingress>> ingresses = this.k8sWorkLoadsManager.getIngress(serviceId, config);

		// 循环所有ingress
		for (V1beta1Ingress ingress : ingresses.getResult()) {
			// 封装返回对象并扔到List中
			TypesResourceAO typesResourceAO = new TypesResourceAO();
			typesResourceAO.setType(ingress.getKind());
			typesResourceAO.setName(ingress.getMetadata().getName());
			typesResourceAO.setLabels(ingress.getMetadata().getLabels());
			typesResourceAO.setCreateAt(ingress.getMetadata().getCreationTimestamp().toDate());
			typesResourceAO.setInfo(ingress.getSpec().getRules());
			listResourcesResult.add(typesResourceAO);
		}
		// 循环所有service
		services.getResult().stream().forEach(service -> {
			TypesResourceAO typesResourceAO = new TypesResourceAO();
			typesResourceAO.setName(service.getMetadata().getName());
			List<Map<String, String>> info = new ArrayList<>();
			service.getSpec().getPorts().stream().forEach(port -> {
				Map<String, String> map = new LinkedHashMap<>();
				map.put("name", port.getName());
				if (port.getPort() != null) {
					map.put("port", port.getPort().toString());
				}
				map.put("protocol", port.getProtocol());
				if (port.getTargetPort() != null) {
					map.put("targetPort", port.getTargetPort().toString());
				}
				if (port.getNodePort() != null) {
					map.put("nodePort", port.getNodePort().toString());
				}
				info.add(map);
			});
			typesResourceAO.setInfo(info);
			typesResourceAO.setCreateAt(service.getMetadata().getCreationTimestamp().toDate());
			typesResourceAO.setType(service.getKind());
			typesResourceAO.setLabels(service.getMetadata().getLabels());
			listResourcesResult.add(typesResourceAO);
		});
	}

	/**
	 * 根据类型和uid查询对象
	 *
	 * @param config
	 * @param uid
	 * @param type
	 * @return com.ladeit.common.ExecuteResult<java.lang.Object>
	 * @author falcomlife
	 * @date 20-4-8
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<JSONObject> getResourceByUid(String config, String uid, String type) throws IOException {
		ExecuteResult<JSONObject> result = new ExecuteResult<>();
		List<Object> list = this.k8sWorkLoadsManager.getAll(config, type);
		Object equalsObj = null;
		for (Object o : list) {
			JSONObject jo = JSON.parseObject(new io.kubernetes.client.JSON().serialize(o));
			if (!jo.getJSONArray("items").isEmpty()) {
				Optional optional = jo.getJSONArray("items").stream().filter(i -> {
					JSONObject joo = JSON.parseObject(JSON.toJSONString(i));
					String id = joo.getJSONObject("metadata").getString("uid");
					return uid.equals(id);
				}).findFirst();
				boolean isPresent = optional.isPresent();
				if (isPresent) {
					equalsObj = optional.get();
				}
			}
		}
		if (equalsObj == null) {
			result.setCode(Code.NOTFOUND);
			result.addWarningMessage("资源未找到");
			return result;
		} else {
			result.setResult((JSONObject) equalsObj);
			return result;
		}
	}

	/**
	 * 根据releaseId查询资源
	 *
	 * @param config
	 * @param releaseId
	 * @param type
	 * @return com.ladeit.common.ExecuteResult<com.alibaba.fastjson.JSONObject>
	 * @author falcomlife
	 * @date 20-4-14
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<JSONObject> getResourceByReleaseIdSelector(String config, String releaseId, String type) throws IOException {
		ExecuteResult<JSONObject> result = new ExecuteResult<>();
		List<Object> list = this.k8sWorkLoadsManager.getAll(config, type);
		Object equalsObj = null;
		for (Object o : list) {
			JSONObject jo = JSON.parseObject(new io.kubernetes.client.JSON().serialize(o));
			if (!jo.getJSONArray("items").isEmpty()) {
				Optional optional = jo.getJSONArray("items").stream().filter(i -> {
					JSONObject joo = JSON.parseObject(JSON.toJSONString(i));
					String id = joo.getJSONObject("metadata").getJSONObject("labels").getString("releaseId");
					return releaseId.equals(id);
				}).findFirst();
				boolean isPresent = optional.isPresent();
				if (isPresent) {
					equalsObj = optional.get();
				}
			}
		}
		if (equalsObj == null) {
			result.setCode(Code.NOTFOUND);
			result.addWarningMessage("未找到匹配的资源");
			return result;
		} else {
			result.setResult((JSONObject) equalsObj);
			return result;
		}
	}

	/**
	 * 生成k8sapiclient
	 *
	 * @param id
	 * @return io.kubernetes.client.ApiClient
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public ApiClient clientGenerateByClusterId(String id) throws IOException {
		Cluster cluster = this.clusterService.getClusterById(id);
		return K8sClientUtil.get(cluster.getK8sKubeconfig());
	}

	/**
	 * 生成k8sapiclient
	 *
	 * @param id
	 * @return io.kubernetes.client.ApiClient
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public ApiClient clientGenerateByEnvId(String id) throws IOException {
		Env env = this.envService.getEnvById(id);
		Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
		ByteArrayInputStream inputStream = new ByteArrayInputStream(cluster.getK8sKubeconfig().getBytes());
		return Config.fromConfig(inputStream);
	}

	/**
	 * 生成k8sapiclient
	 *
	 * @param id
	 * @return io.kubernetes.client.ApiClient
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public ApiClient clientGenerateByServiceId(String id) throws IOException {
		Env env = this.envService.getEnvById(this.serviceService.getById(id).getResult().getEnvId());
		Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
		ByteArrayInputStream inputStream = new ByteArrayInputStream(cluster.getK8sKubeconfig().getBytes());
		return Config.fromConfig(inputStream);
	}

	/**
	 * 得到k8s config
	 *
	 * @param id
	 * @return io.kubernetes.client.ApiClient
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public String getConfigByServiceId(String id) {
		Env env = this.envService.getEnvById(this.serviceService.getById(id).getResult().getEnvId());
		Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
		return cluster.getK8sKubeconfig();
	}

	/**
	 * 获取服务下所有的yaml
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-16
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> getAllYamlInService(String serviceId) throws IOException {
		ExecuteResult<String> result = new ExecuteResult<>();
		StringBuffer yaml = new StringBuffer();
		boolean have = false;
		Env env = this.envService.getEnvById(this.serviceService.getById(serviceId).getResult().getEnvId());
		Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
		ExecuteResult<Release> releaseRes = this.releaseService.getInUseReleaseByServiceId(serviceId);
		Release release = releaseRes.getResult();
		List<Object> objects = this.k8sWorkLoadsManager.getAll(cluster.getK8sKubeconfig(), null);
		for (Object o : objects) {
			JSONObject jo = JSON.parseObject(new io.kubernetes.client.JSON().serialize(o));
			if (!jo.getJSONArray("items").isEmpty()) {
				jo.getJSONArray("items").stream().forEach(i -> {
					JSONObject joo = JSON.parseObject(JSON.toJSONString(i));
					if(joo.getJSONObject("metadata").getJSONObject("labels")!=null){
						String id = joo.getJSONObject("metadata").getJSONObject("labels").getString("releaseId");
						if (release.getId().equals(id)) {
							yaml.append("---\n").append(Yaml.dump(i));
						}
					}
				});
			}
		}
		if (yaml.length() != 0) {
			result.setResult(yaml.substring(4, yaml.length()));
		}
		return result;
	}
}
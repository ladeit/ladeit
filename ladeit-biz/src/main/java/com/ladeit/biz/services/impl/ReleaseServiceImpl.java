package com.ladeit.biz.services.impl;

import com.ladeit.biz.annotation.Authority;
import com.ladeit.biz.dao.OperationDao;
import com.ladeit.biz.dao.ReleaseDao;
import com.ladeit.biz.manager.IstioManager;
import com.ladeit.biz.manager.K8sClusterManager;
import com.ladeit.biz.manager.K8sContainerManager;
import com.ladeit.biz.manager.K8sWorkLoadsManager;
import com.ladeit.biz.moniter.K8sEventMoniterListener;
import com.ladeit.biz.moniter.K8sEventSource;
import com.ladeit.biz.services.*;
import com.ladeit.biz.shiro.SecurityUtils;
import com.ladeit.biz.utils.CommonConsant;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.biz.utils.Producer;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.ao.configuration.LivenessProbe;
import com.ladeit.pojo.doo.*;
import com.ladeit.util.ListUtil;
import com.ladeit.util.k8s.UnitUtil;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: ladeit
 * @description: ReleaseServiceImpl
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
@org.springframework.stereotype.Service
@Slf4j
public class ReleaseServiceImpl implements ReleaseService {

	@Autowired
	private ReleaseDao releaseDao;
	@Autowired
	private ServiceService serviceService;
	@Autowired
	private CandidateService candidateService;
	@Autowired
	private K8sContainerManager k8sContainerManager;
	@Autowired
	private EnvService envService;
	@Autowired
	private ImageService imageService;
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private OperationService operationService;
	@Autowired
	private IstioManager istioManager;
	@Autowired
	private K8sWorkLoadsManager k8sWorkLoadsManager;
	@Autowired
	private K8sClusterManager k8sClusterManager;
	@Autowired
	private OperationDao operationDao;
	@Autowired
	private Producer producer;
	@Autowired
	private MessageService messageService;
	@Autowired
	private ServiceGroupService serviceGroupService;
	@Autowired
	private UserService userService;
	@Autowired
	private MessageUtils messageUtils;
	@Autowired
	private ResourceService resourceService;

	/**
	 * 新建release
	 *
	 * @param release
	 * @param service
	 * @param candidate
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	@Override
	@Authority(type = "service", level = "X")
	@Transactional(rollbackForClassName = {"java.lang.Exception", "java.lang.RuntimeException"})
	public ExecuteResult<String> newRelease(String serviceId, Release release, Service service, Candidate candidate,
											ResourceAO resourceAO, ConfigurationAO configuration) throws IOException,
			ApiException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		return newReleaseOper(serviceId, release, service, candidate, resourceAO, configuration);
	}


	/**
	 * 自动新建release
	 *
	 * @param release
	 * @param service
	 * @param candidate
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	@Override
	@Transactional(rollbackForClassName = {"java.lang.Exception", "java.lang.RuntimeException"})
	public ExecuteResult<String> newReleaseAuto(String serviceId, Release release, Service service,
												Candidate candidate, ResourceAO resourceAO,
												ConfigurationAO configuration) throws IOException, ApiException,
			InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		return newReleaseOper(serviceId, release, service, candidate, resourceAO, configuration);
	}


	/**
	 * 新建release
	 *
	 * @param release
	 * @param service
	 * @param candidate
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	public ExecuteResult<String> newReleaseOper(String serviceId, Release release, Service service,
												Candidate candidate,
												ResourceAO resource, ConfigurationAO configuration) throws IOException,
			ApiException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		ExecuteResult<String> result = new ExecuteResult<>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		Date date = new Date();
		String releaseid = UUID.randomUUID().toString();
		release.setId(releaseid);
		candidate.setId(UUID.randomUUID().toString());
		candidate.setReleaseId(release.getId());
		release.setStatus(0);
		release.setType(0);
		release.setServiceId(service.getId());
		release.setCreateBy(user.getUsername());
		release.setCreateById(user.getId());
		release.setDeployStartAt(date);
		release.setServiceStartAt(date);
		// 状态0，发布完成
		service.setStatus(Service.SERVICE_STATUS_4);
		// 查询image
		Image image = this.imageService.getImageById(candidate.getImageId());
		candidate.setName(image.getVersion());
		candidate.setStatus(1);
		candidate.setType(0);
		candidate.setImageId(image.getId());
		candidate.setServiceId(service.getId());
		service.setImageVersion(image.getVersion());
		service.setImageId(image.getId());
		service.setReleaseAt(date);
		release.setImageId(image.getId());
		Operation operation = new Operation();
		operation.setDeployId(UUID.randomUUID().toString());
		operation.setTarget("release");
		operation.setTargetId(releaseid);
		// First deployment
		operation.setEventLog("首次发布");
		operation.setCreateAt(new Date());
		operation.setCreateBy(user.getUsername());
		operation.setCreateById(user.getId());
		operationDao.insert(operation);
		// 更新service状态
		this.serviceService.updateById(service);
		// 插入release数据
		this.releaseDao.insert(release);
		// 插入candiate数据
		this.candidateService.insert(candidate);
		// 插入热力信息
		this.serviceService.insertHeatMap(service.getId());
		// 插入日志
		// 进行k8s操作
		// 首先查询env
		String envid = service.getEnvId();
		Env env = this.envService.getEnvById(envid);
		Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
		if (release.getIsDefault()) {
			// 采用极简发布方式,默认配置
			this.k8sContainerManager.applyYaml(cluster.getK8sKubeconfig(),
					this.packageDeployment(new V1Deployment(), service.getId(), release.getId(), service.getName(),
							env, image, configuration));

			this.k8sContainerManager.applyYaml(cluster.getK8sKubeconfig(),
					this.packageService(null, service.getId(), service.getName(), env, image, configuration));
			// 创建Gateway
			Gateway gateway = this.packageGateway(service.getId(), service.getName(), env.getNamespace(),
					configuration.getHost());
			// 创建virtualservice
			VirtualService virtualServiceCreate = this.packageVirtualService(service.getId(), release.getId(),
					service.getName(), env.getNamespace(), release.getType(), image.getVersion(), configuration);
			// 创建destinationrule
			DestinationRule destinationRuleCreate = this.packageDestinationrule(service.getId(), release.getId(),
					image.getVersion(), service.getName(), env.getNamespace(), release.getType());
			this.istioManager.createGateway(cluster.getK8sKubeconfig(), gateway);
			this.istioManager.createVirtualServices(cluster.getK8sKubeconfig(), virtualServiceCreate);
			this.istioManager.createDestinationrules(cluster.getK8sKubeconfig(), destinationRuleCreate);
			this.producer.putCandidate(release.getId(), service.getId(), candidate.getId(), image.getId(), null,
					null, image.getVersion(), 8, null, false);
		} else {
			// 采用以前的配置，根据选中的资源进行发布
			// 获取所有资源名称和命名空间
			Method[] methods = resource.getClass().getMethods();
			for (Method method : methods) {
				if (method.getName().startsWith("get")) {
					Object obj = method.invoke(resource);
					if (obj != null) {
						if (obj instanceof Class) {
							continue;
						}
						List<Map<String, String>> list = (List<Map<String, String>>) obj;
						for (Map<String, String> map : list) {
							String namespace = map.get("namespace");
							String name = map.get("name");
							String yaml = map.get("yaml");
							if (StringUtils.isBlank(yaml)) {
								// 如果是有名字没有yaml,这时使用老的配置文件
								Object o = this.k8sWorkLoadsManager.getByNameNamespace(service.getId(),
										cluster.getK8sKubeconfig(),
										method.getName().substring(3), namespace, name);
								Class clazz = o.getClass();
								YamlContentAO yamlAo = new YamlContentAO();
								yamlAo.setKindType(clazz.getSimpleName());
								yamlAo.setContent(Yaml.dump(o));
								yamlAo.setNameSpace(env.getNamespace());
								yamlAo.setServiceId(service.getId());
								String message = this.k8sContainerManager.createByYaml(yamlAo,
										cluster.getK8sKubeconfig(), name);
								if (!"SUCCESS".equals(message)) {
									result.addWarningMessage(message);
									result.setCode(Code.K8SWARN);
								}
							} else if (StringUtils.isNotBlank(yaml)) {
								// 如果是有yaml没有名字，这是使用人工编写的新的配置文件。
								this.applyYaml(yaml, env.getNamespace(), cluster.getK8sKubeconfig(), service.getId(),
										image.getVersion(), result);
							}
						}
					}
				}
			}
			this.producer.putCandidate(release.getId(), service.getId(), candidate.getId(), image.getId(), null,
					null, image.getVersion(), 8, null, false);
		}
		ExecuteResult<Service> s = this.serviceService.getById(service.getId());
		ExecuteResult<ServiceGroup> sgr = serviceGroupService.getGroupById(s.getResult().getServiceGroupId());
		ServiceGroup sg = sgr.getResult();
		Message message = new Message();
		message.setId(UUID.randomUUID().toString());
		message.setCreateAt(new Date());
		// sg.getName() + "/" + s.getResult().getName() + " is deploying..."
		message.setContent(sg.getName() + "/" + s.getResult().getName() + " is deploying...");
		message.setLevel("NORMAL");
		message.setOperuserId(user.getId());
		message.setServiceGroupId(s.getResult().getServiceGroupId());
		message.setServiceId(s.getResult().getId());
		message.setTargetId(releaseid);
		// sg.getName() + "/" + s.getResult().getName() + " is deploying..."
		message.setTitle(sg.getName() + "/" + s.getResult().getName() + " is deploying...");
		message.setType(CommonConsant.MESSAGE_TYPE_2);
		message.setMessageType(CommonConsant.MESSAGE_TYPE_S);
		Map<String, Object> param = new HashMap<>();
		param.put("imageVersioin", image.getVersion());
		param.put("releaseName", release.getName());
		param.put("operChannel", release.getOperChannel());
		message.setParams(param);
		this.messageService.insertMessage(message, Boolean.TRUE);
		this.messageService.insertSlackMessage(message);
		this.producer.putCandidate(release.getId(), s.getResult().getId(), candidate.getId(), image.getId(), null,
				null, image.getVersion(), 10, null, false);
		return result;
	}

	/**
	 * 通过string更新yaml
	 *
	 * @param yaml
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-12-16
	 * @version 1.0.0
	 */
	public void applyYaml(String yaml, String namespace, String kubeconfig, String serviceId, String version,
						  ExecuteResult<String> result) throws IOException {
		Map item = (Map) (new org.yaml.snakeyaml.Yaml().load(yaml));
		String kind = (String) item.get("kind");
		Object metadataObj = item.get("metadata");
		Object specObj = item.get("spec");
		String name = null;
		if (metadataObj != null) {
			// 获取资源的名字
			Map<String, Object> metadata = (Map<String, Object>) metadataObj;
			Object nameobj = metadata.get("name");
			if (nameobj != null) {
				name = nameobj.toString();
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
		}
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
						labels.put("version", version);
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
		yamlAo.setVersion(version);
		try {
			this.k8sContainerManager.createByYaml(yamlAo, kubeconfig, name);
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
	}


	private Gateway packageGateway(String serviceId, String name, String namespace, List<String> hosts) {
		ObjectMeta meta = new ObjectMeta();
		meta.setName("gateway-" + name);
		meta.setNamespace(namespace);
		Map<String, String> label = new HashMap<>();
		label.put("serviceId", serviceId);
		meta.setLabels(label);
		GatewaySpec gatewaySpec = new GatewaySpec();
		List<Server> servers = new ArrayList<>();
		Server server = new Server();
		Port port = new Port();
		port.setNumber(80);
		port.setName("http");
		port.setProtocol("http");
		server.setPort(port);
		server.setHosts(hosts);
		servers.add(server);
		gatewaySpec.setServers(servers);
		return new GatewayBuilder().withApiVersion("networking.istio" +
				".io/v1alpha3").withMetadata(meta).withSpec(gatewaySpec).build();
	}

	/**
	 * 封装Destinationrule
	 *
	 * @param version
	 * @param name
	 * @param namespace
	 * @return me.snowdrop.istio.api.networking.v1alpha3.DestinationRule
	 * @author falcomlife
	 * @date 19-11-20
	 * @version 1.0.0
	 */
	private DestinationRule packageDestinationrule(String serviceId, String releaseId, String version, String name,
												   String namespace, Integer type) {
		ObjectMeta meta = new ObjectMeta();
		meta.setName("dest-" + name);
		meta.setNamespace(namespace);
		Map<String, String> label = new HashMap<>();
		label.put("serviceId", serviceId);
		meta.setLabels(label);
		DestinationRuleSpec destinationRuleSpec = new DestinationRuleSpec();
		destinationRuleSpec.setHost(name);
		List<Subset> subsets = new ArrayList<>();
		Subset subset = new Subset();
		subset.setName(version);
		Map<String, String> labels = new HashMap<>();
		labels.put("version", version);
		subset.setLabels(labels);
		subsets.add(subset);
		destinationRuleSpec.setSubsets(subsets);
		TrafficPolicy trafficPolicy = new TrafficPolicy();
		LoadBalancerSettings loadBalancerSettings = new LoadBalancerSettings();
		SimpleLbPolicy simpleLbPolicy = new SimpleLbPolicy();
		simpleLbPolicy.setSimple(SimpleLB.RANDOM);
		loadBalancerSettings.setLbPolicy(simpleLbPolicy);
		trafficPolicy.setLoadBalancer(loadBalancerSettings);
		TLSSettings tlsSettings = new TLSSettings();
		tlsSettings.setMode(TLSSettingsMode.ISTIO_MUTUAL);
		trafficPolicy.setTls(tlsSettings);
		destinationRuleSpec.setTrafficPolicy(trafficPolicy);
		return new DestinationRuleBuilder().withApiVersion("networking.istio" +
				".io/v1alpha3").withMetadata(meta).withSpec(destinationRuleSpec).build();
	}

	/**
	 * 封装virtualservice
	 *
	 * @param name
	 * @param namespace
	 * @return me.snowdrop.istio.api.networking.v1alpha3.VirtualService
	 * @author falcomlife
	 * @date 19-11-20
	 * @version 1.0.0
	 */
	private VirtualService packageVirtualService(String serviceId, String releaseId, String name, String namespace,
												 Integer type, String version, ConfigurationAO configuration) {
		ObjectMeta meta = new ObjectMeta();
		meta.setName("virtualservice-" + name);
		meta.setNamespace(namespace);
		Map<String, String> label = new HashMap<>();
		label.put("serviceId", serviceId);
		meta.setLabels(label);
		VirtualServiceSpec virtualServiceSpec = new VirtualServiceSpec();
		virtualServiceSpec.setHosts(configuration.getHost());
		List<String> gateways = new ArrayList<>();
		gateways.add("gateway-" + name);
		virtualServiceSpec.setGateways(gateways);
		List<HTTPRoute> httpRoutes = new ArrayList<>();
		HTTPRoute httpRoute = new HTTPRoute();
		httpRoutes.add(httpRoute);
		List<HTTPRouteDestination> httpRouteDestinations = new ArrayList<>();
		HTTPRouteDestination httpRouteDestination = new HTTPRouteDestination();
		httpRouteDestination.setWeight(100);
		httpRouteDestinations.add(httpRouteDestination);
		Destination destination = new Destination();
		destination.setHost(name);
		destination.setSubset(version);
		PortSelector portSelector = new PortSelector();
		NumberPort port = new NumberPort();
		if (configuration.getPorts() != null) {
			port.setNumber(configuration.getPorts().get(0).getServicePort());
		}
		portSelector.setPort(port);
		destination.setPort(portSelector);
		httpRouteDestination.setDestination(destination);
		httpRoute.setRoute(httpRouteDestinations);
		virtualServiceSpec.setHttp(httpRoutes);
		return new VirtualServiceBuilder().withApiVersion("networking.istio" +
				".io/v1alpha3").withMetadata(meta).withSpec(virtualServiceSpec).build();
	}

	/**
	 * 封装deployment
	 *
	 * @param name
	 * @param env
	 * @param image
	 * @return io.kubernetes.client.models.V1Deployment
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	private V1Deployment packageDeployment(V1Deployment deployment, String serviceId, String releaseId, String name,
										   Env env, Image image,
										   ConfigurationAO configuration) {

		V1ObjectMeta meta = new V1ObjectMeta();
		deployment.setMetadata(meta);
		Map<String, String> selectorlables = new HashMap<>();
		Map<String, String> lables = new HashMap<>();
		meta.setLabels(lables);
		V1DeploymentSpec spec = new V1DeploymentSpec();
		spec.setReplicas(configuration.getReplicas());
		deployment.setSpec(spec);
		V1LabelSelector selector = new V1LabelSelector();
		spec.setSelector(selector);
		selector.setMatchLabels(selectorlables);
		V1PodTemplateSpec template = new V1PodTemplateSpec();
		spec.setTemplate(template);
		V1ObjectMeta podMeta = new V1ObjectMeta();
		podMeta.setLabels(lables);
		template.metadata(podMeta);
		V1PodSpec podSpec = new V1PodSpec();
		template.setSpec(podSpec);
		List<V1Container> containers = new ArrayList<>();
		podSpec.setContainers(containers);
		if (configuration.getVolumes() != null) {
			podSpec.setVolumes(configuration.getVolumes().stream().map(volume -> {
				V1Volume v1Volume = new V1Volume();
				V1PersistentVolumeClaimVolumeSource pvc = new V1PersistentVolumeClaimVolumeSource();
				pvc.setClaimName(volume.getName());
				v1Volume.setPersistentVolumeClaim(pvc);
				v1Volume.setName(volume.getName());
				return v1Volume;
			}).collect(Collectors.toList()));
		}
		V1Container container = new V1Container();
		if (configuration.getPorts() != null) {
			container.setPorts(configuration.getPorts().stream().map(port -> {
				if (StringUtils.isNotBlank(port.getName()) && port.getContainerPort() != 0 && port.getServicePort() != 0) {
					V1ContainerPort v1ContainerPort = new V1ContainerPort();
					v1ContainerPort.setContainerPort(port.getContainerPort());
					v1ContainerPort.setName(port.getName());
					return v1ContainerPort;
				} else {
					return null;
				}
			}).collect(Collectors.toList()));
		}
		if (configuration.getEnvs() != null) {
			container.setEnv(configuration.getEnvs().stream().map(envVar -> {
				if (envVar.getKey() != null && envVar.getValue() != null) {
					V1EnvVar v1EnvVar = new V1EnvVar();
					v1EnvVar.setName(envVar.getKey());
					v1EnvVar.setValue(envVar.getValue());
					return v1EnvVar;
				} else {
					return null;
				}
			}).collect(Collectors.toList()));
		}
		if (StringUtils.isNotBlank(configuration.getCommand())) {
			List<String> commands = new ArrayList<>();
			commands.add(configuration.getCommand());
			container.setCommand(commands);
		}
		container.setArgs(configuration.getArgs());
		container.setName(name);
		container.setImage(image.getImage());
		if (configuration.getVolumes() != null) {
			container.setVolumeMounts(configuration.getVolumes().stream().map(volume -> {
				V1VolumeMount v1VolumeMount = new V1VolumeMount();
				v1VolumeMount.setMountPath(volume.getPath());
				v1VolumeMount.setName(volume.getName());
				return v1VolumeMount;
			}).collect(Collectors.toList()));
		}
		if (configuration.getLivenessProbe() != null) {
			LivenessProbe livenessProbe = configuration.getLivenessProbe();
			V1Probe v1Probe = new V1Probe();
			container.setLivenessProbe(v1Probe);
			if ("command".equals(livenessProbe.getType())) {
				V1ExecAction execAction = new V1ExecAction();
				ArrayList commandList = new ArrayList();
				execAction.setCommand(commandList);
				commandList.add(livenessProbe.getCommand());
				v1Probe.setExec(execAction);
			} else if ("httpget".equals(livenessProbe.getType())) {
				V1HTTPGetAction v1HTTPGetAction = new V1HTTPGetAction();
				v1HTTPGetAction.setHost("localhost");
				v1HTTPGetAction.setPath(livenessProbe.getPath());
				IntOrString intOrString = new IntOrString(livenessProbe.getPort());
				v1HTTPGetAction.setPort(intOrString);
				v1HTTPGetAction.setScheme(livenessProbe.getProtocol());
				v1Probe.setHttpGet(v1HTTPGetAction);
			}
			v1Probe.setFailureThreshold(livenessProbe.getFailureThreshold());
			v1Probe.setSuccessThreshold(livenessProbe.getSuccessThreshold());
			v1Probe.setInitialDelaySeconds(livenessProbe.getInitialDelaySeconds());
			v1Probe.setPeriodSeconds(livenessProbe.getPeriodSeconds());
			v1Probe.setTimeoutSeconds(livenessProbe.getTimeoutSeconds());
		}
		if (configuration.getResourceQuota() != null && configuration.getResourceQuota()) {
			V1ResourceRequirements resourceRequirements = new V1ResourceRequirements();
			container.setResources(resourceRequirements);
			Map<String, Quantity> limit = new HashMap<>();
			Map<String, Quantity> request = new HashMap<>();
			resourceRequirements.setLimits(limit);
			resourceRequirements.setRequests(request);
			Quantity cpulimit = new Quantity(UnitUtil.unitConverter(configuration.getCpuLimitUnit(),
					configuration.getCpuLimit(), 1), Quantity.Format.BINARY_SI);
			Quantity memlimit = new Quantity(UnitUtil.unitConverter(configuration.getMemLimitUnit(),
					configuration.getMemLimit(), 2), Quantity.Format.BINARY_SI);
			Quantity cpurequest = new Quantity(UnitUtil.unitConverter(configuration.getCpuRequestUnit(),
					configuration.getCpuRequest(), 1), Quantity.Format.BINARY_SI);
			Quantity memrequest = new Quantity(UnitUtil.unitConverter(configuration.getMemRequestUnit(),
					configuration.getMemRequest(), 2), Quantity.Format.BINARY_SI);
			limit.put("cpu", cpulimit);
			limit.put("memory", memlimit);
			request.put("cpu", cpurequest);
			request.put("memory", memrequest);
			container.setResources(resourceRequirements);
		}
		meta.setName(name + "-" + image.getVersion());
		meta.setNamespace(env.getNamespace());
		Map<String, String> serviceLabel = new HashMap<>();
		serviceLabel.put("serviceId", serviceId);
		meta.setLabels(serviceLabel);
		spec.setProgressDeadlineSeconds(3);
		lables.put("app", name);
		lables.put("version", image.getVersion());
		lables.put("serviceId", serviceId);
		lables.put("releaseId", releaseId);
		selectorlables.put("app", name);
		selectorlables.put("serviceId", serviceId);
		podMeta.setName(name);
		containers.add(container);
		return deployment;
	}

	public V1Service packageService(V1Service service, String serviceId, String name, Env env, Image image,
									ConfigurationAO configuration) {
		if (service == null) {
			service = new V1Service();
		}
		V1ObjectMeta meta = new V1ObjectMeta();
		V1ServiceSpec spec = new V1ServiceSpec();
		Map<String, String> lables = new HashMap<>();
		Map<String, String> selector = new HashMap<>();
		service.setMetadata(meta);
		service.setSpec(spec);
		spec.setSelector(selector);
		if (configuration.getPorts() != null) {
			spec.setPorts(configuration.getPorts().stream().map(port -> {
				V1ServicePort v1ServicePort = new V1ServicePort();
				v1ServicePort.setName(port.getName());
				IntOrString containerPort = new IntOrString(port.getContainerPort());
				v1ServicePort.setTargetPort(containerPort);
				v1ServicePort.setPort(port.getServicePort());
				return v1ServicePort;
			}).collect(Collectors.toList()));
		}
		selector.put("app", name);
		service.setKind("Service");
		lables.put("app", name);
		lables.put("serviceId", serviceId);
		meta.setName(name);
		meta.setNamespace(env.getNamespace());
		meta.setLabels(lables);
		return service;
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
	private ApiClient clientGenerate(String id) throws IOException {
		Cluster cluster = this.clusterService.getClusterById(id);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(cluster.getK8sKubeconfig().getBytes());
		return Config.fromConfig(inputStream);
	}

	/**
	 * 根据serviceid得到服役中的release
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Release>
	 * @author falcomlife
	 * @date 19-11-11
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<Release> getInUseReleaseByServiceId(String serviceId) {
		ExecuteResult<Release> result = new ExecuteResult<>();
		Release release = this.releaseDao.getInUseReleaseByServiceId(serviceId);
		result.setResult(release);
		return result;
	}

	/**
	 * 查询某个release
	 *
	 * @param releaseId,releaseName
	 * @return com.ladeit.common.ExecuteResult<java.util.List                                                               <                                                               com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<ReleaseAO>> queryReleaseInfo(String releaseId, String releaseName) {
		ExecuteResult<List<ReleaseAO>> result = new ExecuteResult<List<ReleaseAO>>();
		List<Release> releases = releaseDao.getReleaseListByIdAndName(releaseId, releaseName);
		List<ReleaseAO> releaseAOS = new ListUtil<Release, ReleaseAO>().copyList(releases,
				ReleaseAO.class);
		result.setResult(releaseAOS);
		return result;
	}

	/**
	 * 根据releaseID查询某个release
	 *
	 * @param releaseId
	 * @return com.ladeit.common.ExecuteResult<java.util.List                                                               <                                                               com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<QueryReleaseAO> queryOneReleaseInfo(String releaseId) {
		ExecuteResult<QueryReleaseAO> result = new ExecuteResult<QueryReleaseAO>();
		Release release = releaseDao.getInUseReleaseByReleaseId(releaseId);
		if (release == null) {
			result.setCode(Code.NOTFOUND);
			return result;
		}
		QueryReleaseAO queryReleaseAO = new QueryReleaseAO();
		BeanUtils.copyProperties(release, queryReleaseAO);
		List<Operation> operations = operationDao.getOperListByTargetAndId("release", releaseId);
		List<OperationAO> operationAOs = new ListUtil<Operation, OperationAO>().copyList(operations,
				OperationAO.class);
		queryReleaseAO.setOperations(operationAOs);
		result.setResult(queryReleaseAO);
		return result;
	}

	/**
	 * 查询服务发布信息
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List                                                               <                                                               com.ladeit.pojo.ao.ReleaseAO>>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<Pager<ReleaseAO>> queryReleases(String serviceId, int currentPage, int pageSize) {
		ExecuteResult<Pager<ReleaseAO>> result = new ExecuteResult<Pager<ReleaseAO>>();

		List<Release> releases = releaseDao.getReleaseListPager(serviceId, currentPage, pageSize);
		int count = releaseDao.getReleaseListCount(serviceId);
		List<ReleaseAO> releaseAOS = new ListUtil<Release, ReleaseAO>().copyList(releases,
				ReleaseAO.class);
		Pager<ReleaseAO> pager = new Pager<>();
		pager.setPageNum(currentPage);
		pager.setPageSize(pageSize);
		pager.setRecords(releaseAOS);
		pager.setTotalRecord(count);
		result.setResult(pager);
		return result;
	}


	/**
	 * 首页升级服务
	 *
	 * @param release
	 * @param service
	 * @param candidate
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	@Override
	@Authority(type = "service", level = "X")
	@Transactional
	public ExecuteResult<String> refreshRelease(String serviceId, Release release, Service service,
												Candidate candidate,
												TopologyAO topology, Boolean auto, ConfigurationAO configuration) throws IOException, ApiException {
		return refreshReleaseOper(serviceId, release, service, candidate, topology, auto, configuration);
	}

	/**
	 * 自动升级服务
	 *
	 * @param release
	 * @param service
	 * @param candidate
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> refreshReleaseAuto(String serviceId, Release release, Service service,
													Candidate candidate,
													TopologyAO topology, Boolean auto, ConfigurationAO configuration) throws IOException,
			ApiException {
		return refreshReleaseOper(serviceId, release, service, candidate, topology, auto, configuration);
	}


	/**
	 * 首页升级服务
	 *
	 * @param release
	 * @param service
	 * @param candidate
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	public ExecuteResult<String> refreshReleaseOper(String serviceId, Release release, Service service,
													Candidate candidate,
													TopologyAO topology, Boolean auto, ConfigurationAO configuration) throws IOException,
			ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		String envid = service.getEnvId();
		Env env = this.envService.getEnvById(envid);
		String config = this.resourceService.getConfigByServiceId(serviceId);
		Image image = this.imageService.getImageById(candidate.getImageId());
		User user = SecurityUtils.getSubject()==null?null:(User) SecurityUtils.getSubject().getPrincipal();
		// 先查询servcie，比对状态，如果正处于发版状态，不能发版。这里查询到的servcies，下面给message传递数据也能用到
		ExecuteResult<Service> s = this.serviceService.getById(service.getId());
		Service servcieNow = s.getResult();
		if (servcieNow == null) {
			result.setCode(Code.NOTFOUND);
			String message = messageUtils.matchMessage("M0016", new Object[]{}, Boolean.TRUE);
			result.addWarningMessage(message);

			return result;
		}
		if (!"-1".equals(servcieNow.getStatus()) && !"0".equals(servcieNow.getStatus()) && !"8".equals(servcieNow.getStatus())) {
			result.setCode(Code.STATUS_ERROR);
			String message = messageUtils.matchMessage("M0017", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		// 查询现在可能部署的workload资源
		ExecuteResult<List<V1Deployment>> deployments =
				this.k8sWorkLoadsManager.getDeployment(service.getId(), config);
		ExecuteResult<List<V1StatefulSet>> statefulSets =
				this.k8sWorkLoadsManager.getStatefulSet(service.getId(), config);
		ExecuteResult<List<V1ReplicationController>> replicationControllers =
				this.k8sWorkLoadsManager.getReplicationControllers(service.getId(), config);
		Boolean deploymentflag = deployments.getResult() == null || deployments.getResult().size() == 0;
		Boolean statefulSetflag = statefulSets.getResult() == null || statefulSets.getResult().size() == 0;
		Boolean replicationControllerflag =
				replicationControllers.getResult() == null || replicationControllers.getResult().size() == 0;
		if (deploymentflag && statefulSetflag && replicationControllerflag) {
			result.setCode(Code.NOTFOUND);
			String message = messageUtils.matchMessage("M0018", new Object[]{}, Boolean.TRUE);
			result.addWarningMessage(message);
			return result;
		}
		if (release.getType().equals(8)) {
			// 滚动更新
			this.rollingUpdate(service, image, release, deploymentflag, statefulSetflag, replicationControllerflag,
					deployments.getResult(), statefulSets.getResult(),
					replicationControllers.getResult(), config, env, candidate, topology, configuration);
		} else if (release.getType().equals(4)) {
			// ab测试
			this.abTest(service, image, release, deploymentflag, statefulSetflag, replicationControllerflag,
					deployments.getResult(), statefulSets.getResult(),
					replicationControllers.getResult(), config, env, candidate, topology, configuration);
		}
		// 放入message，在redis访入发布信息
		ExecuteResult<ServiceGroup> sgr = serviceGroupService.getGroupById(s.getResult().getServiceGroupId());
		ServiceGroup sg = sgr.getResult();
		Message message = new Message();
		message.setId(UUID.randomUUID().toString());
		message.setCreateAt(new Date());
		message.setContent(sg.getName() + "/" + servcieNow.getName() + " is deploying...");
		message.setLevel("NORMAL");
		if (user == null) {
			user = this.userService.getUserByUsername("bot");
		}
		message.setOperuserId(user.getId());
		message.setServiceGroupId(servcieNow.getServiceGroupId());
		message.setServiceId(servcieNow.getId());
		message.setTargetId(servcieNow.getServiceGroupId());
		message.setTitle(sg.getName() + "/" + servcieNow.getName() + " is deploying...");
		message.setType(CommonConsant.MESSAGE_TYPE_2);
		message.setMessageType(CommonConsant.MESSAGE_TYPE_S);
		Map<String, Object> param = new HashMap<>();
		param.put("imageVersioin", image.getVersion());
		param.put("releaseName", release.getName());
		param.put("operChannel", release.getOperChannel());
		message.setParams(param);
		this.messageService.insertMessage(message, auto != null && !auto ? true : false);
		this.messageService.insertSlackMessage(message);
		this.producer.putCandidate(release.getId(), servcieNow.getId(), candidate.getId(), image.getId(), null,
				null, image.getVersion(), 10, null, auto != null && auto ? true : false);
		return result;
	}

	/**
	 * 滚动更新逻辑
	 *
	 * @param service
	 * @param image
	 * @param release
	 * @param deploymentflag
	 * @param statefulSetflag
	 * @param replicationControllerflag
	 * @param deployment
	 * @param statefulSet
	 * @param replicationController
	 * @param config
	 * @param env
	 * @param candidate
	 * @param topology
	 * @return void
	 * @author falcomlife
	 * @date 20-3-18
	 * @version 1.0.0
	 */
	@Override
	public void rollingUpdate(Service service, Image image, Release release, Boolean deploymentflag,
							  Boolean statefulSetflag, Boolean replicationControllerflag,
							  List<V1Deployment> deployment,
							  List<V1StatefulSet> statefulSet, List<V1ReplicationController> replicationController,
							  String config, Env env, Candidate candidate, TopologyAO topology,
							  ConfigurationAO configuration) throws ApiException, IOException {
		User user = SecurityUtils.getSubject()==null?null:(User) SecurityUtils.getSubject().getPrincipal();
		Date date = new Date();
		// 现根据service查询现在的release
		ExecuteResult<Service> serviceExecuteResult = this.serviceService.getById(service.getId());
		// 维护service
		service.setStatus(Service.SERVICE_STATUS_4);
		service.setImageVersion(image.getVersion());
		service.setImageId(image.getId());
		this.serviceService.updateById(service);
		// 更新热力信息
		this.serviceService.insertHeatMap(service.getId());
		// 维护release
		String releaseid = UUID.randomUUID().toString();
		release.setId(releaseid);
		release.setStatus(0);
		release.setType(8);
		release.setServiceId(service.getId());
		if (user == null) {
			user = this.userService.getUserByUsername("bot");
		}
		release.setCreateBy(user.getUsername());
		release.setCreateById(user.getId());
		release.setDeployStartAt(date);
		release.setDeployFinishAt(date);
		release.setServiceStartAt(date);
		release.setImageId(image.getId());
		this.releaseDao.insert(release);
		// 维护操作表
		Operation operation = new Operation();
		operation.setDeployId(UUID.randomUUID().toString());
		operation.setTarget("release");
		operation.setTargetId(releaseid);
		// rolling
		operation.setEventLog("滚动发布");
		this.operationService.insert(operation);
		// 操作k8s
		String uid = null;
		if (!deploymentflag) {
			V1Deployment v1Deployment = null;
			if (release.getNewYaml() != null && release.getNewYaml()) {
				String resourcename = deployment.get(0).getMetadata().getName();
				V1Deployment deploymentnew = this.packageDeployment(deployment.get(0), service.getId(), releaseid,
						service.getName(), env, image, configuration);
				deploymentnew.getMetadata().setName(resourcename);
				deploymentnew.getSpec().getTemplate().getMetadata().getLabels().put("version", image.getVersion());
				deploymentnew.getSpec().getTemplate().getMetadata().getLabels().put("releaseId", releaseid);
				v1Deployment = this.k8sWorkLoadsManager.replaceDeployment(config, deploymentnew);
				ExecuteResult<List<V1Service>> v1Service = this.k8sWorkLoadsManager.getService(service.getId(),
						config);
				if (v1Service.getCode() == Code.SUCCESS) {
					V1Service finalService = this.packageService(null, service.getId(), service.getName(), env,
							image, configuration);
					finalService.getMetadata().setResourceVersion(v1Service.getResult().get(0).getMetadata().getResourceVersion());
					finalService.getSpec().setClusterIP(v1Service.getResult().get(0).getSpec().getClusterIP());
					this.k8sWorkLoadsManager.replaceService(config, finalService);
				}
				// 创建Gateway
				Gateway gateway = this.packageGateway(service.getId(), service.getName(), env.getNamespace(),
						configuration.getHost());
				// 创建virtualservice
				VirtualService virtualServiceCreate = this.packageVirtualService(service.getId(), release.getId(),
						service.getName(), env.getNamespace(), release.getType(), image.getVersion(), configuration);
				// 创建destinationrule
				DestinationRule destinationRuleCreate = this.packageDestinationrule(service.getId(), release.getId(),
						image.getVersion(), service.getName(), env.getNamespace(), release.getType());
				this.istioManager.createGateway(config, gateway);
				this.istioManager.createVirtualServices(config, virtualServiceCreate);
				this.istioManager.createDestinationrules(config, destinationRuleCreate);
			} else {
				deployment.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).setImage(image.getImage());
				deployment.get(0).getSpec().getTemplate().getMetadata().getLabels().put("version", image.getVersion());
				deployment.get(0).getSpec().getTemplate().getMetadata().getLabels().put("releaseId", releaseid);
				v1Deployment = this.k8sWorkLoadsManager.replaceDeployment(config, deployment.get(0));
			}
			uid = v1Deployment.getMetadata().getUid();
			K8sEventSource source = new K8sEventSource(this.k8sClusterManager,
					this.clusterService.getClusterById(env.getClusterId()).getK8sKubeconfig(), env.getNamespace(),
					"Deployment", deployment.get(0).getMetadata().getName());
			K8sEventMoniterListener k8sEventMoniterListener =
					k8sEventEvent -> System.out.println(k8sEventEvent.getType() + "," + k8sEventEvent.getStatus() + ","
							+ k8sEventEvent.getMessage());
			source.addMoniter(k8sEventMoniterListener);
			source.startAction();
		} else if (!statefulSetflag) {
			statefulSet.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).setImage(image.getImage());
			statefulSet.get(0).getSpec().getSelector().putMatchLabelsItem("version", image.getVersion());
			statefulSet.get(0).getSpec().getTemplate().getMetadata().getLabels().put("version", image.getVersion());
			statefulSet.get(0).getSpec().getTemplate().getMetadata().getLabels().put("releaseId", releaseid);
			this.k8sWorkLoadsManager.replaceStatefulSet(config, statefulSet.get(0));
		} else if (!replicationControllerflag) {
			replicationController.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).setImage(image.getImage());
			replicationController.get(0).getSpec().getSelector().put("version", image.getVersion());
			replicationController.get(0).getSpec().getTemplate().getMetadata().getLabels().put("version",
					image.getVersion());
			replicationController.get(0).getSpec().getTemplate().getMetadata().getLabels().put("releaseId", releaseid);
			this.k8sWorkLoadsManager.replaceReplicationController(config, replicationController.get(0));
		}
		this.producer.putCandidate(release.getId(), service.getId(), candidate.getId(), image.getId(), uid, topology,
				image.getVersion(), 8, null, false);
	}

	/**
	 * ab测试逻辑
	 *
	 * @param service
	 * @param image
	 * @param release
	 * @param deploymentflag
	 * @param statefulSetflag
	 * @param replicationControllerflag
	 * @param deployment
	 * @param statefulSet
	 * @param replicationController
	 * @param config
	 * @param env
	 * @param candidate
	 * @param topology
	 * @return void
	 * @author falcomlife
	 * @date 20-3-18
	 * @version 1.0.0
	 */
	@Override
	public void abTest(Service service, Image image, Release release, Boolean deploymentflag,
					   Boolean statefulSetflag, Boolean replicationControllerflag, List<V1Deployment> deployment,
					   List<V1StatefulSet> statefulSet, List<V1ReplicationController> replicationController,
					   String config, Env env, Candidate candidate, TopologyAO topology,
					   ConfigurationAO configuration) throws ApiException {
		User user = SecurityUtils.getSubject()==null?null:(User) SecurityUtils.getSubject().getPrincipal();
		Date date = new Date();
		// 现根据service查询现在的release
		ExecuteResult<Service> serviceExecuteResult = this.serviceService.getById(service.getId());
		Release releaseInUse =
				this.releaseDao.getInUseReleaseByServiceId(serviceExecuteResult.getResult().getId());
		// 把原来的release改成退役
		// 维护service
		service.setStatus(Service.SERVICE_STATUS_4);
		service.setImageVersion(image.getVersion());
		service.setImageId(image.getId());
		this.serviceService.updateById(service);
		// 更新热力信息
		this.serviceService.insertHeatMap(service.getId());
		// 维护release
		String releaseid = UUID.randomUUID().toString();
		release.setId(releaseid);
		release.setStatus(0);
		release.setType(4);
		release.setServiceId(service.getId());
		if (user == null) {
			user = this.userService.getUserByUsername("bot");
		}
		release.setCreateBy(user.getUsername());
		release.setCreateById(user.getId());
		release.setDeployStartAt(date);
		release.setDeployFinishAt(date);
		release.setServiceStartAt(date);
		release.setImageId(image.getId());
		this.releaseDao.insert(release);
		// 维护candidate
		candidate.setId(UUID.randomUUID().toString());
		candidate.setReleaseId(release.getId());
		candidate.setServiceId(service.getId());
		candidate.setImageId(image.getId());
		candidate.setName(image.getVersion());
		candidate.setStatus(1);
		this.candidateService.insert(candidate);
		// 维护操作表
		Operation operation = new Operation();
		operation.setDeployId(UUID.randomUUID().toString());
		operation.setTarget("release");
		operation.setTargetId(releaseid);
		// A/B Test
		operation.setEventLog("AB测试发布");
		this.operationService.insert(operation);
		// 操作k8s
		Map<String, String> labels = new HashMap<>();
		labels.put("releaseId", releaseid);
		labels.put("serviceId", service.getId());
		labels.put("app", service.getName());
		labels.put("version", image.getVersion());
		String uid = null;
		if (!deploymentflag) {
			String name = service.getName() + "-" + image.getVersion();
			uid = deployment.get(0).getMetadata().getUid();
			deployment.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).setImage(image.getImage());
			deployment.get(0).getMetadata().setLabels(labels);
			deployment.get(0).getSpec().getSelector().setMatchLabels(labels);
			deployment.get(0).getSpec().getTemplate().getMetadata().setLabels(labels);
			deployment.get(0).getMetadata().setName(name);
			deployment.get(0).getSpec().getTemplate().getMetadata().getLabels().put("version", image.getVersion());
			this.k8sWorkLoadsManager.createDeployment(config, deployment.get(0));
		} else if (!statefulSetflag) {
			String name = statefulSet.get(0).getMetadata().getName() + "-" + image.getVersion();
			uid = statefulSet.get(0).getMetadata().getUid();
			statefulSet.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).setImage(image.getImage());
			statefulSet.get(0).getMetadata().setLabels(labels);
			statefulSet.get(0).getSpec().getSelector().setMatchLabels(labels);
			statefulSet.get(0).getSpec().getTemplate().getMetadata().setLabels(labels);
			statefulSet.get(0).getMetadata().setName(name);
			statefulSet.get(0).getSpec().getTemplate().getMetadata().getLabels().put("version", image.getVersion());
			this.k8sWorkLoadsManager.createStatefulSet(config, statefulSet.get(0));
		} else if (!replicationControllerflag) {
			uid = replicationController.get(0).getMetadata().getUid();
			String name =
					replicationController.get(0).getMetadata().getName() + "-" + image.getVersion();
			replicationController.get(0).getSpec().getTemplate().getSpec().getContainers().get(0).setImage(image.getImage());
			replicationController.get(0).getMetadata().setLabels(labels);
			replicationController.get(0).getSpec().setSelector(labels);
			replicationController.get(0).getSpec().getTemplate().getMetadata().setLabels(labels);
			replicationController.get(0).getMetadata().setName(name);
			replicationController.get(0).getSpec().getTemplate().getMetadata().getLabels().put("version",
					image.getVersion());
			this.k8sWorkLoadsManager.createReplicationController(config, replicationController.get(0));
		}
		// 调用生产者产生数据
		this.producer.putCandidate(release.getId(), service.getId(), candidate.getId(), image.getId(), uid,
				topology, image.getVersion(), 4, null, false);
	}

	/**
	 * 根据serviceid查询release数据，按照部署时间从大到小排列
	 *
	 * @param id
	 * @return java.util.List<com.ladeit.pojo.doo.Release>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public List<Release> getReleaseList(String id) {
		return this.releaseDao.getReleaseList(id);
	}

	/**
	 * 修改状态，同事修改时间
	 *
	 * @param r1
	 * @return void
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public void updateStatus(Release r1) {
		this.releaseDao.updateStatus(r1);
	}

	/**
	 * 查询发布中的release
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Release>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<Release> getInUpdateRelease(String serviceId) {
		ExecuteResult<Release> result = new ExecuteResult<>();
		result.setResult(this.releaseDao.getInUpdateRelease(serviceId));
		return result;
	}

}

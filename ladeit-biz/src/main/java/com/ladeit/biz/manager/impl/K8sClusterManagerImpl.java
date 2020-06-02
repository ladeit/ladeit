package com.ladeit.biz.manager.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.ladeit.biz.manager.K8sClusterManager;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.util.k8s.K8sClientUtil;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.*;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class K8sClusterManagerImpl implements K8sClusterManager {

	/**
	 * 测试集群链接状态
	 *
	 * @param config
	 * @return boolean
	 * @author falcomlife
	 * @date 20-3-16
	 * @version 1.0.0
	 */
	@Override
	public boolean connectTest(String config) throws ApiException, ClassCastException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1NodeList nodelist = coreApi.listNode(null, null, null, null, null, null, null, null, null);
		return nodelist.getItems().isEmpty();
	}

	/**
	 * 查询namespace
	 *
	 * @param config
	 * @author falcomlife
	 * @date 19-9-20
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1Namespace>> listNamespace(String config) {
		ExecuteResult<List<V1Namespace>> executeResult = new ExecuteResult<>();
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		try {
			V1NamespaceList v1NamespaceList = coreApi.listNamespace(false, null, null, null, null, null, null, null,
					false);
			List<V1Namespace> items = v1NamespaceList.getItems();
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
		} catch (ApiException e) {
			executeResult.setCode(Code.FAILED);
			// Error getting namespace.
			executeResult.addErrorMessage("错误信息：获取Namespace列表");
			log.error(e.getMessage(), e);
		}
		return executeResult;
	}

	/**
	 * 得到replicasets
	 *
	 * @param serviceId
	 * @param config
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1ReplicationController> getReplicationControllers(String serviceId, String config,
																   String namespace) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1ReplicationControllerList replicationControllerList = coreApi.listNamespacedReplicationController(namespace,
				null, null, null, null, null, null, null, null, null);
		return replicationControllerList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("ReplicationController");
			r.setApiVersion("apps/v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * deployment
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1Deployment>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1Deployment> getDeployments(String serviceId, String config, String namespace) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		V1DeploymentList v1DeploymentList = appsV1Api.listNamespacedDeployment(namespace, null, null, null, null, null
				, null, null, null, null);
		return v1DeploymentList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
				r.getMetadata().setLabels(labels);
			}
			r.setKind("Deployment");
			r.setApiVersion("apps/v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * statefulset
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1StatefulSet>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1StatefulSet> getStatefulsets(String serviceId, String config, String namespace) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		V1StatefulSetList v1StatefulSetList = appsV1Api.listNamespacedStatefulSet(namespace, null, null, null, null,
				null, null, null, null, null);
		return v1StatefulSetList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("Statefulset");
			r.setApiVersion("apps/v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * cronjob
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1beta1CronJob>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1beta1CronJob> getCronJobs(String serviceId, String config, String namespace) throws ApiException {
		BatchV1beta1Api batchV1beta1Api = (BatchV1beta1Api) K8sClientUtil.get(config, BatchV1beta1Api.class);
		V1beta1CronJobList v1beta1CronJobList = batchV1beta1Api.listNamespacedCronJob(namespace, null, null, null,
				null, null
				, null, null, null, null);
		return v1beta1CronJobList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("CronJob");
			r.setApiVersion("v1beta1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * job
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1Job>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1Job> getJobs(String serviceId, String config, String namespace) throws ApiException {
		BatchV1Api batchV1Api = (BatchV1Api) K8sClientUtil.get(config, BatchV1Api.class);
		V1JobList v1JobList = batchV1Api.listNamespacedJob(namespace, null, null, null, null, null
				, null, null, null, null);
		return v1JobList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("Job");
			r.setApiVersion("v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * daemonset
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1DaemonSet>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1DaemonSet> getDaemonSets(String serviceId, String config, String namespace) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		V1DaemonSetList v1DaemonSets = appsV1Api.listNamespacedDaemonSet(namespace, null, null, null, null,
				null, null, null, null, null);
		return v1DaemonSets.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("DaemonSet");
			r.setApiVersion("v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * pod
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1Pod>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1Pod> getPods(String serviceId, String config, String namespace) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1PodList v1PodList = coreApi.listNamespacedPod(namespace,
				null, null, null, null, null, null, null, null, null);
		return v1PodList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("Pod");
			r.setApiVersion("v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * service
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1Service>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1Service> getServices(String serviceId, String config, String namespace) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1ServiceList v1ServiceList = coreApi.listNamespacedService(namespace,
				null, null, null, null, null, null, null, null, null);
		return v1ServiceList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("Service");
			r.setApiVersion("v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * ingress
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1beta1Ingress>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1beta1Ingress> getIngresses(String serviceId, String config, String namespace) throws ApiException {
		ExtensionsV1beta1Api extensionsV1beta1Api = (ExtensionsV1beta1Api) K8sClientUtil.get(config,
				ExtensionsV1beta1Api.class);
		V1beta1IngressList v1beta1IngressList = extensionsV1beta1Api.listNamespacedIngress(namespace,
				null, null, null, null, null, null, null, null, null);
		return v1beta1IngressList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("Ingress");
			r.setApiVersion("networking.k8s.io/v1beta1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * configmap
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1ConfigMap>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1ConfigMap> getConfigMaps(String serviceId, String config, String namespace) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1ConfigMapList v1ConfigMapList = coreApi.listNamespacedConfigMap(namespace,
				null, null, null, null, null, null, null, null, null);
		return v1ConfigMapList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("ConfigMap");
			r.setApiVersion("v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * secret
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1Secret>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1Secret> getSecrets(String serviceId, String config, String namespace) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1SecretList v1SecretList = coreApi.listNamespacedSecret(namespace,
				null, null, null, null, null, null, null, null, null);
		return v1SecretList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("Secret");
			r.setApiVersion("v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * serviceaccount
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1ServiceAccount>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1ServiceAccount> getServiceAccounts(String serviceId, String config, String namespace) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1ServiceAccountList v1ServiceAccountList = coreApi.listNamespacedServiceAccount(namespace,
				null, null, null, null, null, null, null, null, null);
		return v1ServiceAccountList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("ServiceAccount");
			r.setApiVersion("v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * persistentVolume
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1PersistentVolume>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1PersistentVolume> getPersistentVolumes(String serviceId, String config, String namespace) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1PersistentVolumeList v1PersistentVolumeList = coreApi.listPersistentVolume(
				null, null, null, null, null, null, null, null, null);
		return v1PersistentVolumeList.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("PersistentVolume");
			r.setApiVersion("v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * persistentVolumeClaim
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1PersistentVolumeClaim>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1PersistentVolumeClaim> getPersistentVolumeClaims(String serviceId, String config,
																   String namespace) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1PersistentVolumeClaimList v1PersistentVolumeClaims = coreApi.listNamespacedPersistentVolumeClaim(namespace,
				null, null, null, null, null, null, null, null, null);
		return v1PersistentVolumeClaims.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("PersistentVolumeClaim");
			r.setApiVersion("v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * storageclass
	 *
	 * @param config
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1StorageClass>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1StorageClass> getStorageClasses(String serviceId, String config, String namespace) throws ApiException {
		StorageV1Api storageV1Api = (StorageV1Api) K8sClientUtil.get(config, StorageV1Api.class);
		V1StorageClassList v1StorageClasses = storageV1Api.listStorageClass(
				null, null, null, null, null, null, null, null, null);
		return v1StorageClasses.getItems().stream().map(r -> {
			Map<String, String> labels = new HashMap<>();
			if (StringUtils.isNotBlank(serviceId)) {
				labels.put("serviceId", serviceId);
			}
			r.getMetadata().setLabels(labels);
			r.setKind("StorageClass");
			r.setApiVersion("storage.k8s.io/v1");
			return r;
		}).collect(Collectors.toList());
	}

	/**
	 * 查询events
	 *
	 * @param config
	 * @param namespace
	 * @param fieldSelector
	 * @return java.util.List<io.kubernetes.client.models.V1beta1Event>
	 * @author falcomlife
	 * @date 20-1-15
	 * @version 1.0.0
	 */
	@Override
	public List<V1beta1Event> getResourceEvent(String config, String namespace, String fieldSelector) throws ApiException {
		EventsV1beta1Api eventsV1beta1Api = (EventsV1beta1Api) K8sClientUtil.get(config, EventsV1beta1Api.class);
		V1beta1EventList events = eventsV1beta1Api.listNamespacedEvent(namespace, null, null, null, fieldSelector,
				null,
				null, null, null, null);
		return events.getItems();
	}

	/**
	 * 查询namespace
	 *
	 * @param config
	 * @param namespace
	 * @return io.kubernetes.client.models.V1Namespace
	 * @author falcomlife
	 * @date 20-3-12
	 * @version 1.0.0
	 */
	@Override
	public V1Namespace getNamespace(String config, String namespace) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1NamespaceList list = coreApi.listNamespace(null, null, null, null, null, null, null, null, null);
		return list.getItems().stream().filter(ns -> ns.getMetadata().getName().equals(namespace)).findFirst().get();
	}

	/**
	 * 创建资源配额
	 *
	 * @param
	 * @return void
	 * @author falcomlife
	 * @date 20-3-16
	 * @version 1.0.0
	 */
	@Override
	public void createResourceQuota(String namespace, BigDecimal limitcpu, BigDecimal limitmemory,
									BigDecimal requestcpu,
									BigDecimal requestmemory, String config) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1ResourceQuota resourceQuota = new V1ResourceQuota();
		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setNamespace(namespace);
		meta.setName("resource-quota");
		resourceQuota.setMetadata(meta);
		V1ResourceQuotaSpec spec = new V1ResourceQuotaSpec();
		Map<String, Quantity> hard = new HashMap<>();
		if (limitcpu != null) {
			Quantity quantitylimitcpu = new Quantity(limitcpu, Quantity.Format.BINARY_SI);
			hard.put("limits.cpu", quantitylimitcpu);
		}
		if (limitmemory != null) {
			Quantity quantitylimitmemory = new Quantity(limitmemory, Quantity.Format.BINARY_SI);
			hard.put("limits.memory", quantitylimitmemory);
		}
		if (requestcpu != null) {
			Quantity quantityrequestcpu = new Quantity(requestcpu, Quantity.Format.BINARY_SI);
			hard.put("requests.cpu", quantityrequestcpu);
		}
		if (requestmemory != null) {
			Quantity quantityrequestmemory = new Quantity(requestmemory, Quantity.Format.BINARY_SI);
			hard.put("requests.memory", quantityrequestmemory);
		}
		spec.setHard(hard);
		resourceQuota.setSpec(spec);
		V1ResourceQuotaList v1ResourceQuotaList = coreApi.listNamespacedResourceQuota(namespace, null, null, null,
				null, null, null, null, null, null);
		if (v1ResourceQuotaList.getItems().isEmpty()) {
			coreApi.createNamespacedResourceQuota(namespace, resourceQuota, null, null, null);
		} else {
			V1ResourceQuota rq = v1ResourceQuotaList.getItems().get(0);
			resourceQuota.getMetadata().setName(rq.getMetadata().getName());
			coreApi.replaceNamespacedResourceQuota(rq.getMetadata().getName(), namespace, resourceQuota, null, null);
		}
	}

	/**
	 * 查询resourcequota
	 *
	 * @param namespace
	 * @param config
	 * @return java.util.List<io.kubernetes.client.models.V1ResourceQuota>
	 * @author falcomlife
	 * @date 20-3-25
	 * @version 1.0.0
	 */
	@Override
	public List<V1ResourceQuota> getResourceQuota(String namespace, String config) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1ResourceQuotaList v1ResourceQuotaList = coreApi.listNamespacedResourceQuota(namespace, null, null, null,
				null, null, null, null, null, null);
		return v1ResourceQuotaList.getItems();
	}


	/**
	 * 查询所有resourcequota
	 *
	 * @param config
	 * @return java.util.List<io.kubernetes.client.models.V1ResourceQuota>
	 * @author falcomlife
	 * @date 20-3-25
	 * @version 1.0.0
	 */
	@Override
	public List<V1ResourceQuota> getAllResourceQuota(String config) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1ResourceQuotaList v1ResourceQuotaList = coreApi.listResourceQuotaForAllNamespaces(null, null, null, null,
				null, null, null, null, null);
		return v1ResourceQuotaList.getItems();
	}

	/**
	 * 删除resourcequota
	 *
	 * @param name
	 * @param namespace
	 * @param config
	 * @return void
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public void deleteResourceQuota(String name, String namespace, String config) {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1DeleteOptions body = new V1DeleteOptions();
		try {
			coreApi.deleteNamespacedResourceQuota(name, namespace, body, null, null,
					null, null, null);
		} catch (Exception e) {
			// TODO 抛异常，但是资源可以删除
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 创建namespace
	 *
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 20-5-25
	 * @version 1.0.0
	 */
	@Override
	public V1Namespace createNamespace(V1Namespace namespace, String config) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		return coreApi.createNamespace(namespace, null, null, null);
	}


	/**
	 * 删除namespace
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 20-6-2
	 * @version 1.0.0
	 */
	@Override
	public void deleteNamespace(String k8sKubeconfig, String namespace) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(k8sKubeconfig, CoreV1Api.class);
		V1DeleteOptions v1DeleteOptions = new V1DeleteOptions();
		try {
			coreApi.deleteNamespace(namespace, v1DeleteOptions, null, null, null, null, null);
		} catch (JsonSyntaxException e) {
			// TODO 抛异常，但是资源可以删除
			log.error(e.getMessage(), e);
		}
	}
}

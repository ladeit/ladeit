package com.ladeit.biz.manager.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ladeit.biz.manager.IstioManager;
import com.ladeit.biz.manager.K8sClusterManager;
import com.ladeit.biz.manager.K8sWorkLoadsManager;
import com.ladeit.biz.utils.ClassUtil;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.util.k8s.K8sClientUtil;
import com.ladeit.util.k8s.LabelToString;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.*;
import io.kubernetes.client.models.*;
import io.kubernetes.client.proto.V1;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class K8sWorkLoadsManagerImpl implements K8sWorkLoadsManager {

	@Autowired
	private K8sClusterManager k8sClusterManager;
	@Autowired
	private IstioManager istioManager;

	/**
	 * 查询pod
	 *
	 * @param labelSelector
	 * @param config
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1Pod>>
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1Pod>> getPods(String labelSelector, String config) {
		ExecuteResult<List<V1Pod>> executeResult = new ExecuteResult<>();
		CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		try {
			V1PodList listPods =
					v1Api.listPodForAllNamespaces(null, null, null,
							"serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1Pod> items = listPods.getItems();
			items.stream().forEach(d -> {
				d.setKind("Pod");
				d.setApiVersion("v1");
			});
			//log.info("获取CronJob：{}", items);
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting Pod.
			executeResult.addErrorMessage("错误信息：获取Pod失败");
		}
		return executeResult;
	}

	/**
	 * 查询pod
	 *
	 * @param namespace
	 * @param config
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1Pod>>
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1Pod>> getPodsInNamespace(String namespace, String config) {
		ExecuteResult<List<V1Pod>> executeResult = new ExecuteResult<>();
		CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		try {
			V1PodList listPods =
					v1Api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null);
			//log.info("获取CronJob：{}", items);
			executeResult.setResult(listPods.getItems());
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting Pod.
			executeResult.addErrorMessage("错误信息：获取Pod失败");
		}
		return executeResult;
	}

	/**
	 * 通过releaseid查询pod
	 *
	 * @param releaseid
	 * @param config
	 * @return java.util.List<io.kubernetes.client.models.V1Pod>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public List<V1Pod> getPodsByReleaseId(String releaseid, String config) throws ApiException {
		CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1PodList listPods =
				v1Api.listPodForAllNamespaces(null, null, null,
						"releaseId=" + releaseid, null, null, null, 3000, null);
		List<V1Pod> items = listPods.getItems();
		items.stream().forEach(d -> {
			d.setKind("Pod");
			d.setApiVersion("v1");
		});
		return listPods.getItems();
	}

	/**
	 * 查询cronjob
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1beta1CronJob>>
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1beta1CronJob>> getCronJobs(String labelSelector, String config) {
		ExecuteResult<List<V1beta1CronJob>> executeResult = new ExecuteResult<>();
		BatchV1beta1Api v1Api = (BatchV1beta1Api) K8sClientUtil.get(config, BatchV1beta1Api.class);

		try {
			V1beta1CronJobList listCronJobForAllNamespaces =
					v1Api.listCronJobForAllNamespaces(null, null, null,
							"serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1beta1CronJob> items = listCronJobForAllNamespaces.getItems();
			items.stream().forEach(d -> {
				d.setKind("CronJob");
				d.setApiVersion("v1beta1");
			});
			//log.info("获取CronJob：{}", items);
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting CronJob.
			executeResult.addErrorMessage("错误信息：获取CronJob失败");
		}
		return executeResult;
	}

	/**
	 * 查询V1ReplicationController
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1ReplicationController>>
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1ReplicationController>> getReplicationControllers(String labelSelector,
																				  String config) {
		ExecuteResult<List<V1ReplicationController>> executeResult = new ExecuteResult<>();
		CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		try {
			V1ReplicationControllerList listReplicationControllerForAllNamespaces =
					v1Api.listReplicationControllerForAllNamespaces(null, null, null,
							"serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1ReplicationController> items = listReplicationControllerForAllNamespaces.getItems();
			items.stream().forEach(d -> {
				d.setKind("ReplicationController");
				d.setApiVersion("v1");
			});
			//log.info("获取ReplicationController：{}", items);
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting ReplicationController.
			executeResult.addErrorMessage("错误信息：获取ReplicationController失败");
		}
		return executeResult;
	}


	/**
	 * 获取DaemonSet
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1DaemonSet>>
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1DaemonSet>> getDaemonSet(String labelSelector, String config) {
		ExecuteResult<List<V1DaemonSet>> executeResult = new ExecuteResult<>();
		AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		try {
			V1DaemonSetList listDaemonSetForAllNamespaces = v1Api.listDaemonSetForAllNamespaces(null, null, null,
					"serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1DaemonSet> items = listDaemonSetForAllNamespaces.getItems();
			items.stream().forEach(d -> {
				d.setKind("DaemonSet");
				d.setApiVersion("v1");
			});
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting DaemonSet.
			executeResult.addErrorMessage("错误信息：获取DaemonSet失败");
		}
		return executeResult;
	}

	/**
	 * 获取Deployment
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1Deployment>>
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1Deployment>> getDeployment(String labelSelector, String config) {
		ExecuteResult<List<V1Deployment>> executeResult = new ExecuteResult<>();
		AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		try {
			V1DeploymentList listDeploymentForAllNamespaces = v1Api.listDeploymentForAllNamespaces(null, null, null,
					"serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1Deployment> items = listDeploymentForAllNamespaces.getItems();
			items.stream().forEach(d -> {
				d.setKind("Deployment");
				d.setApiVersion("v1");
			});
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting Deployment.
			executeResult.addErrorMessage("错误信息：获取Deployment失败");
		}
		return executeResult;
	}

	/**
	 * 获取Deployment
	 *
	 * @param namespace
	 * @param name
	 * @param config
	 * @return com.ladeit.common.ExecuteResult<java.util.List < io.kubernetes.client.models.V1Deployment>>
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public V1Deployment getDeploymentByName(String namespace, String name, String config) throws ApiException {
		AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		V1DeploymentList listDeploymentForAllNamespaces = v1Api.listNamespacedDeployment(namespace, null, null,
				null, null, null, null, null, null, null);
		if (listDeploymentForAllNamespaces.getItems().isEmpty()) {
			return null;
		} else {
			List<V1Deployment> deployments =
					listDeploymentForAllNamespaces.getItems().stream().filter(deployment -> deployment.getMetadata().getName().equals(name)).collect(Collectors.toList());
			return !deployments.isEmpty() ? deployments.get(0) : null;
		}
	}

	/**
	 * 获取Job
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1Job>>
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1Job>> getJob(String labelSelector, String config) {
		ExecuteResult<List<V1Job>> executeResult = new ExecuteResult<>();
		BatchV1Api v1Api = (BatchV1Api) K8sClientUtil.get(config, BatchV1Api.class);
		try {
			V1JobList listDeploymentForAllNamespaces = v1Api.listJobForAllNamespaces(null, null, null,
					"serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1Job> items = listDeploymentForAllNamespaces.getItems();
			items.stream().forEach(d -> {
				d.setKind("Job");
				d.setApiVersion("v1");
			});
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting Deployment.
			executeResult.addErrorMessage("错误信息：获取Deployment失败");
		}
		return executeResult;
	}

	/**
	 * 获取StatefulSet
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kub`ernetes.client.models.V1StatefulSet>>
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1StatefulSet>> getStatefulSet(String labelSelector, String config) {
		ExecuteResult<List<V1StatefulSet>> executeResult = new ExecuteResult<>();
		AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		try {
			V1StatefulSetList listStatefulSetForAllNamespaces = v1Api.listStatefulSetForAllNamespaces(null, null, null
					, "serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1StatefulSet> items = listStatefulSetForAllNamespaces.getItems();
			items.stream().forEach(d -> {
				d.setKind("StatefulSet");
				d.setApiVersion("v1");
			});
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting StatefulSet.
			executeResult.addErrorMessage("错误信息：获取StatefulSet失败");
		}
		return executeResult;
	}

	/**
	 * 获取service
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1Service>>
	 * @author falcomlife
	 * @date 19-12-4
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1Service>> getService(String labelSelector, String config) {
		ExecuteResult<List<V1Service>> executeResult = new ExecuteResult<>();
		CoreV1Api coreV1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		try {
			V1ServiceList servicelist = coreV1Api.listServiceForAllNamespaces(null, null, null
					, "serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1Service> items = servicelist.getItems();
			items.stream().forEach(d -> {
				d.setKind("Service");
				d.setApiVersion("v1");
			});
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting Service.
			executeResult.addErrorMessage("错误信息：获取service失败");
		}
		return executeResult;
	}

	/**
	 * 获取ingress
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1beta1Ingress>>
	 * @author falcomlife
	 * @date 19-12-4
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1beta1Ingress>> getIngress(String labelSelector, String config) {
		ExecuteResult<List<V1beta1Ingress>> executeResult = new ExecuteResult<>();
		ExtensionsV1beta1Api coreV1Api = (ExtensionsV1beta1Api) K8sClientUtil.get(config, ExtensionsV1beta1Api.class);
		try {
			V1beta1IngressList ingressList = coreV1Api.listIngressForAllNamespaces(null, null, null
					, "serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1beta1Ingress> items = ingressList.getItems();
			items.stream().forEach(d -> {
				d.setKind("Ingress");
				d.setApiVersion("v1beta1");
			});
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting Ingress.
			executeResult.addErrorMessage("错误信息：获取ingress失败");
		}
		return executeResult;
	}

	/**
	 * 获取configmap
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1Service>>
	 * @author falcomlife
	 * @date 19-12-9
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1ConfigMap>> getConfigMap(String labelSelector, String config) {
		ExecuteResult<List<V1ConfigMap>> executeResult = new ExecuteResult<>();
		CoreV1Api coreV1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		try {
			V1ConfigMapList ingressList = coreV1Api.listConfigMapForAllNamespaces(null, null, null
					, "serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1ConfigMap> items = ingressList.getItems();
			items.stream().forEach(d -> {
				d.setKind("ConfigMap");
				d.setApiVersion("v1");
			});
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting ConfigMap.
			executeResult.addErrorMessage("错误信息：获取ConfigMap失败");
		}
		return executeResult;
	}

	/**
	 * 获取pvc
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List.kubernetes.client.models.V1PersistentVolumeClaim>>
	 * @author falcomlife
	 * @date 19-12-9
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<V1PersistentVolumeClaim>> getPvcs(String labelSelector, String config) {
		ExecuteResult<List<V1PersistentVolumeClaim>> executeResult = new ExecuteResult<>();
		CoreV1Api coreV1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		try {
			V1PersistentVolumeClaimList persistentVolumeClaimList =
					coreV1Api.listPersistentVolumeClaimForAllNamespaces(null, null, null
							, "serviceId=" + labelSelector, null, null, null, 3000, null);
			List<V1PersistentVolumeClaim> items = persistentVolumeClaimList.getItems();
			items.stream().forEach(d -> {
				d.setKind("PersistentVolumeClaim");
				d.setApiVersion("v1");
			});
			executeResult.setResult(items);
			executeResult.setCode(Code.SUCCESS);
			return executeResult;
		} catch (ApiException e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
			executeResult.setCode(Code.FAILED);
			// Error getting ConfigMap.
			executeResult.addErrorMessage("错误信息：获取ConfigMap失败");
		}
		return executeResult;
	}

	/**
	 * 根据名字查询storageclass
	 *
	 * @param storageClassName
	 * @return io.kubernetes.client.models.V1StorageClass
	 * @author falcomlife
	 * @date 19-12-12
	 * @version 1.0.0
	 */
	@Override
	public V1StorageClass getStorageClass(String config, String storageClassName) throws ApiException {
		StorageV1Api storageV1Api = (StorageV1Api) K8sClientUtil.get(config, StorageV1Api.class);
		V1StorageClassList list = storageV1Api.listStorageClass(null, null, null, null, null, null, null, null, null);
		List<V1StorageClass> storageClassList = list.getItems().stream().filter(storageClass -> {
			return storageClass.getMetadata().getName().equals(storageClassName);
		}).collect(Collectors.toList());
		if (storageClassList.size() != 0) {
			return storageClassList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 查询pv
	 *
	 * @param config
	 * @param storageClassName
	 * @return io.kubernetes.client.models.V1PersistentVolume
	 * @author falcomlife
	 * @date 19-12-12
	 * @version 1.0.0
	 */
	@Override
	public List<V1PersistentVolume> getPv(String config, String storageClassName) throws ApiException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1PersistentVolumeList v1PersistentVolumeList = coreApi.listPersistentVolume(null, null, null, null, null,
				null, null, null, null);
		return v1PersistentVolumeList.getItems().stream().filter(pv -> {
			return storageClassName.equals(pv.getSpec().getStorageClassName());
		}).collect(Collectors.toList());
	}

	/**
	 * 更新release(滚动更新)
	 *
	 * @param deployment
	 * @return void
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	@Override
	public V1Deployment replaceDeployment(String config, V1Deployment deployment) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		String name = deployment.getMetadata().getName();
		String namespace = deployment.getMetadata().getNamespace();
		deployment.setStatus(null);
		deployment.setApiVersion(null);
		deployment.setKind(null);
		deployment.getMetadata().setUid(null);
		deployment.getMetadata().setSelfLink(null);
		deployment.getMetadata().setResourceVersion(null);
		deployment.getMetadata().setGenerateName(null);
		deployment.getMetadata().setCreationTimestamp(null);
		return appsV1Api.replaceNamespacedDeployment(name, namespace, deployment, null, null);
	}

	/**
	 * 替换service
	 *
	 * @param config
	 * @param service
	 * @return void
	 * @author falcomlife
	 * @date 20-4-17
	 * @version 1.0.0
	 */
	@Override
	public void replaceService(String config, V1Service service) throws ApiException {
		CoreV1Api coreV1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		coreV1Api.replaceNamespacedService(service.getMetadata().getName(), service.getMetadata().getNamespace(),
				service, null, null);
	}

	/**
	 * 更新statefulset
	 *
	 * @param config
	 * @param statefulSet
	 * @return void
	 * @author falcomlife
	 * @date 19-12-24
	 * @version 1.0.0
	 */
	@Override
	public V1StatefulSet replaceStatefulSet(String config, V1StatefulSet statefulSet) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		statefulSet.setStatus(null);
		statefulSet.setApiVersion(null);
		statefulSet.setKind(null);
		statefulSet.getMetadata().setUid(null);
		statefulSet.getMetadata().setSelfLink(null);
		statefulSet.getMetadata().setResourceVersion(null);
		statefulSet.getMetadata().setGenerateName(null);
		statefulSet.getMetadata().setCreationTimestamp(null);
		return appsV1Api.replaceNamespacedStatefulSet(statefulSet.getMetadata().getName(),
				statefulSet.getMetadata().getNamespace(), statefulSet, null, null);
	}

	/**
	 * 更新replicationController
	 *
	 * @param config
	 * @param replicationController
	 * @return void
	 * @author falcomlife
	 * @date 19-12-24
	 * @version 1.0.0
	 */
	@Override
	public void replaceReplicationController(String config, V1ReplicationController replicationController) throws ApiException {
		CoreV1Api appsV1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		replicationController.setStatus(null);
		replicationController.setApiVersion(null);
		replicationController.setKind(null);
		replicationController.getMetadata().setUid(null);
		replicationController.getMetadata().setSelfLink(null);
		replicationController.getMetadata().setResourceVersion(null);
		replicationController.getMetadata().setGenerateName(null);
		replicationController.getMetadata().setCreationTimestamp(null);
		appsV1Api.replaceNamespacedReplicationController(replicationController.getMetadata().getName(),
				replicationController.getMetadata().getNamespace(), replicationController, null, null);
	}

	/**
	 * 新建release(滚动更新)
	 *
	 * @param deployment
	 * @return void
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	@Override
	public void createDeployment(String config, V1Deployment deployment) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		deployment.setStatus(null);
		deployment.setApiVersion(null);
		deployment.setKind(null);
		deployment.getMetadata().setAnnotations(null);
		deployment.getMetadata().setUid(null);
		deployment.getMetadata().setCreationTimestamp(null);
		deployment.getMetadata().setResourceVersion(null);
		deployment.getMetadata().setSelfLink(null);
		appsV1Api.createNamespacedDeployment(deployment.getMetadata().getNamespace(), deployment, null, null, null);
	}

	/**
	 * 运行yaml
	 *
	 * @param config
	 * @param service
	 * @author falcomlife
	 * @date 19-11-18
	 * @version 1.0.0
	 */
	@Override
	public void createService(String config, V1Service service) throws ApiException {
		CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		v1Api.createNamespacedService(service.getMetadata().getNamespace(), service, true, null, null);
	}

	/**
	 * 创建statefulset
	 *
	 * @param config
	 * @param statefulSet
	 * @return void
	 * @author falcomlife
	 * @date 19-12-24
	 * @version 1.0.0
	 */
	@Override
	public void createStatefulSet(String config, V1StatefulSet statefulSet) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		statefulSet.setStatus(null);
		statefulSet.setApiVersion(null);
		statefulSet.setKind(null);
		statefulSet.getMetadata().setAnnotations(null);
		statefulSet.getMetadata().setUid(null);
		statefulSet.getMetadata().setCreationTimestamp(null);
		statefulSet.getMetadata().setResourceVersion(null);
		statefulSet.getMetadata().setSelfLink(null);
		appsV1Api.createNamespacedStatefulSet(statefulSet.getMetadata().getNamespace(), statefulSet, null, null, null);
	}

	/**
	 * 创建replicationController
	 *
	 * @param config
	 * @param replicationController
	 * @return void
	 * @author falcomlife
	 * @date 19-12-24
	 * @version 1.0.0
	 */
	@Override
	public void createReplicationController(String config, V1ReplicationController replicationController) throws ApiException {
		CoreV1Api coreV1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		replicationController.setStatus(null);
		replicationController.setApiVersion(null);
		replicationController.setKind(null);
		coreV1Api.createNamespacedReplicationController(replicationController.getMetadata().getNamespace(),
				replicationController, null, null, null);
	}

	/**
	 * 根据namespace和name查询数据
	 *
	 * @param context
	 * @param namespace
	 * @param name
	 * @return Object
	 * @author falcomlife
	 * @date 19-11-26
	 * @version 1.0.0
	 */
	@Override
	public Object getByNameNamespace(String serviceId, String context, String resourcename, String namespace,
									 String name) throws IOException,
			InvocationTargetException, IllegalAccessException, NoSuchMethodException, ApiException {
		if (resourcename.equals("replicationControllers")) {
			return k8sClusterManager.getReplicationControllers(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("Deployments")) {
			return k8sClusterManager.getDeployments(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("StatefulSets")) {
			return k8sClusterManager.getStatefulsets(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("Jobs")) {
			return k8sClusterManager.getJobs(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("Cronjobs")) {
			return k8sClusterManager.getCronJobs(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("DaemonSets")) {
			return k8sClusterManager.getDaemonSets(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("Pods")) {
			return k8sClusterManager.getPods(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("Services")) {
			return k8sClusterManager.getServices(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("Ingresses")) {
			return k8sClusterManager.getIngresses(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("ConfigMaps")) {
			return k8sClusterManager.getConfigMaps(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("Secrets")) {
			return k8sClusterManager.getSecrets(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("ServiceAccounts")) {
			return k8sClusterManager.getServiceAccounts(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("PersistentVolumes")) {
			return k8sClusterManager.getPersistentVolumes(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("PersistentVolumeClaims")) {
			return k8sClusterManager.getPersistentVolumeClaims(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else if (resourcename.equals("StorageClasses")) {
			return k8sClusterManager.getStorageClasses(serviceId, context, namespace).stream().filter(r -> name.equals(r.getMetadata().getName())).findAny().orElse(null);
		} else {
			return null;
		}
	}

	/**
	 * 伸缩deployment的pod数量
	 *
	 * @param config
	 * @param namespace
	 * @param name
	 * @return void
	 * @author falcomlife
	 * @date 19-12-20
	 * @version 1.0.0
	 */
	@Override
	public V1Scale scaleDeployment(String config, String namespace, String name, V1Deployment deployment) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		ArrayList<JsonObject> arr = new ArrayList<>();
		String jsonStr =
				"{\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":" + deployment.getSpec().getReplicas() +
						"}";
		arr.add(((JsonElement) new Gson().fromJson(jsonStr, JsonElement.class)).getAsJsonObject());
		return appsV1Api.patchNamespacedDeploymentScale(name, namespace, arr, null, null);
	}

	/**
	 * 伸缩replicationController的pod数量
	 *
	 * @param config
	 * @param namespace
	 * @param name
	 * @param replicationControllers
	 * @return io.kubernetes.client.models.V1Scale
	 * @author falcomlife
	 * @date 19-12-20
	 * @version 1.0.0
	 */
	@Override
	public V1Scale scaleReplicationControllers(String config, String namespace, String name,
											V1ReplicationController replicationControllers) throws ApiException {
		CoreV1Api coreV1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		ArrayList<JsonObject> arr = new ArrayList<>();
		String jsonStr =
				"{\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":" + replicationControllers.getSpec().getReplicas() + "}";
		arr.add(((JsonElement) new Gson().fromJson(jsonStr, JsonElement.class)).getAsJsonObject());
		return coreV1Api.patchNamespacedReplicationControllerScale(name, namespace, arr, null,
				null);
	}

	/**
	 * 伸缩replicaSet的pod数量
	 *
	 * @param config
	 * @param namespace
	 * @param name
	 * @param replicaSet
	 * @return io.kubernetes.client.models.V1Scale
	 * @author falcomlife
	 * @date 19-12-20
	 * @version 1.0.0
	 */
	@Override
	public V1Scale scaleReplicaSet(String config, String namespace, String name, V1ReplicaSet replicaSet) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		ArrayList<JsonObject> arr = new ArrayList<>();
		String jsonStr =
				"{\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":" + replicaSet.getSpec().getReplicas() +
						"}";
		arr.add(((JsonElement) new Gson().fromJson(jsonStr, JsonElement.class)).getAsJsonObject());
		return appsV1Api.patchNamespacedReplicaSetScale(name, namespace, arr, null, null);
	}

	/**
	 * 伸缩statefulSet的pod数量
	 *
	 * @param config
	 * @param namespace
	 * @param name
	 * @param statefulSet
	 * @return io.kubernetes.client.models.V1Scale
	 * @author falcomlife
	 * @date 19-12-20
	 * @version 1.0.0
	 */
	@Override
	public V1Scale scaleStatefulSet(String config, String namespace, String name, V1StatefulSet statefulSet) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		ArrayList<JsonObject> arr = new ArrayList<>();
		String jsonStr =
				"{\"op\":\"replace\",\"path\":\"/spec/replicas\",\"value\":" + statefulSet.getSpec().getReplicas() +
						"}";
		arr.add(((JsonElement) new Gson().fromJson(jsonStr, JsonElement.class)).getAsJsonObject());
		return appsV1Api.patchNamespacedStatefulSetScale(name, namespace, arr, null, null);
	}

	/**
	 * 根据labels和namespace删除所有的资源
	 *
	 * @param serviceId
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 20-1-10
	 * @version 1.0.0
	 */
	@Override
	public void deleteResources(String serviceId, String namespace, String config, String name) throws IOException {
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		BatchV1beta1Api batchV1beta1Api = (BatchV1beta1Api) K8sClientUtil.get(config, BatchV1beta1Api.class);
		BatchV1Api batchV1Api = (BatchV1Api) K8sClientUtil.get(config, BatchV1Api.class);
		ExtensionsV1beta1Api extensionsV1beta1Api = (ExtensionsV1beta1Api) K8sClientUtil.get(config,
				ExtensionsV1beta1Api.class);
		StorageV1Api storageV1Api = (StorageV1Api) K8sClientUtil.get(config, StorageV1Api.class);
		V1DeleteOptions body = new V1DeleteOptions();
		try {
			// 删除replicationcontroller
			V1ReplicationControllerList replicationControllerList =
					coreApi.listNamespacedReplicationController(namespace,
							null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (replicationControllerList.getItems() != null && replicationControllerList.getItems().size() != 0) {
				replicationControllerList.getItems().stream().forEach(replicationController -> {
					try {
						coreApi.deleteNamespacedReplicationController(replicationController.getMetadata().getName(),
								namespace,
								body, null, null, null, null, null);
					} catch (ApiException e) {
						log.warn(e.getMessage(), e);
					}
				});
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除deployment
			V1DeploymentList deploymentList = appsV1Api.listNamespacedDeployment(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (deploymentList.getItems() != null && deploymentList.getItems().size() != 0) {
				deploymentList.getItems().stream().forEach(deployment -> {
					try {
						appsV1Api.deleteNamespacedDeployment(deployment.getMetadata().getName(), namespace, body, null
								, null,
								null, null, null);
					} catch (ApiException e) {
						log.warn(e.getMessage(), e);
					}
				});

			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除statefulset
			V1StatefulSetList statefulSetList = appsV1Api.listNamespacedStatefulSet(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (statefulSetList.getItems() != null && statefulSetList.getItems().size() != 0) {
				statefulSetList.getItems().stream().forEach(statefulSet -> {
					try {
						appsV1Api.deleteNamespacedStatefulSet(statefulSet.getMetadata().getName(), namespace, body,
								null,
								null,
								null, null, null);
					} catch (ApiException e) {
						log.warn(e.getMessage(), e);
					}
				});
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除cronjobs
			V1beta1CronJobList cronJobList = batchV1beta1Api.listNamespacedCronJob(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (cronJobList.getItems() != null && cronJobList.getItems().size() != 0) {
				cronJobList.getItems().stream().forEach(cronJob -> {
					try {
						batchV1beta1Api.deleteNamespacedCronJob(cronJob.getMetadata().getName(), namespace, body, null,
								null, null, null, null);
					} catch (ApiException e) {
						log.warn(e.getMessage(), e);
					}
				});

			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除job
			V1JobList jobList = batchV1Api.listNamespacedJob(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (jobList.getItems() != null && jobList.getItems().size() != 0) {
				jobList.getItems().stream().forEach(job -> {
					try {
						batchV1Api.deleteNamespacedJob(job.getMetadata().getName(), namespace, body, null, null, null,
								null,
								null);
					} catch (ApiException e) {
						log.warn(e.getMessage(), e);
					}
				});
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除daemonset
			V1DaemonSetList daemonSetList = appsV1Api.listNamespacedDaemonSet(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (daemonSetList.getItems() != null && daemonSetList.getItems().size() != 0) {
				daemonSetList.getItems().stream().forEach(daemonSet -> {
					try {
						appsV1Api.deleteNamespacedDaemonSet(daemonSet.getMetadata().getName(), namespace, body, null,
								null,
								null,
								null, null);
					} catch (ApiException e) {
						log.warn(e.getMessage(), e);
					}
				});
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除pod
			V1PodList podList = coreApi.listNamespacedPod(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (podList.getItems() != null && podList.getItems().size() != 0) {
				podList.getItems().stream().forEach(pod -> {
					try {
						coreApi.deleteNamespacedPod(pod.getMetadata().getName(), namespace, body, null, null, null,
								null,
								null);
					} catch (ApiException e) {
						log.warn(e.getMessage(), e);
					}
				});
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除service
			V1ServiceList serviceList = coreApi.listNamespacedService(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (serviceList.getItems() != null && serviceList.getItems().size() != 0) {
				V1Service service = serviceList.getItems().get(0);
				coreApi.deleteNamespacedService(service.getMetadata().getName(), namespace, body, null, null, null,
						null,
						null);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除ingress
			V1beta1IngressList ingressList = extensionsV1beta1Api.listNamespacedIngress(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (ingressList.getItems() != null && ingressList.getItems().size() != 0) {
				V1beta1Ingress ingress = ingressList.getItems().get(0);
				extensionsV1beta1Api.deleteNamespacedIngress(ingress.getMetadata().getName(), namespace, body, null,
						null,
						null, null, null);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除configmap
			V1ConfigMapList configMapList = coreApi.listNamespacedConfigMap(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (configMapList.getItems() != null && configMapList.getItems().size() != 0) {
				V1ConfigMap configMap = configMapList.getItems().get(0);
				coreApi.deleteNamespacedConfigMap(configMap.getMetadata().getName(), namespace, body, null, null, null,
						null, null);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除secret
			V1SecretList secretList = coreApi.listNamespacedSecret(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (secretList.getItems() != null && secretList.getItems().size() != 0) {
				V1Secret secret = secretList.getItems().get(0);
				coreApi.deleteNamespacedSecret(secret.getMetadata().getName(), namespace, body, null, null, null, null,
						null);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除serviceaccount
			V1ServiceAccountList serviceAccountList = coreApi.listNamespacedServiceAccount(namespace,
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (serviceAccountList.getItems() != null && serviceAccountList.getItems().size() != 0) {
				V1ServiceAccount v1ServiceAccount = serviceAccountList.getItems().get(0);
				coreApi.deleteNamespacedServiceAccount(v1ServiceAccount.getMetadata().getName(), namespace, body, null,
						null, null, null, null);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除persistentVolume
			V1PersistentVolumeList persistentVolumeList = coreApi.listPersistentVolume(
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (persistentVolumeList.getItems() != null && persistentVolumeList.getItems().size() != 0) {
				V1PersistentVolume v1PersistentVolume = persistentVolumeList.getItems().get(0);
				coreApi.deletePersistentVolume(v1PersistentVolume.getMetadata().getName(), body, null, null, null,
						null,
						null);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除persistentVolume
			V1PersistentVolumeClaimList persistentVolumeClaimList =
					coreApi.listNamespacedPersistentVolumeClaim(namespace,
							null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (persistentVolumeClaimList.getItems() != null && persistentVolumeClaimList.getItems().size() != 0) {
				V1PersistentVolumeClaim v1PersistentVolumeClaim = persistentVolumeClaimList.getItems().get(0);
				coreApi.deleteNamespacedPersistentVolumeClaim(namespace,
						v1PersistentVolumeClaim.getMetadata().getName(), body, null, null, null, null, null);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除persistentVolume
			V1StorageClassList storageClassList = storageV1Api.listStorageClass(
					null, null, null, null, "serviceId=" + serviceId, null, null, null, null);
			if (storageClassList.getItems() != null && storageClassList.getItems().size() != 0) {
				V1StorageClass v1StorageClass = storageClassList.getItems().get(0);
				storageV1Api.deleteStorageClass(v1StorageClass.getMetadata().getName(), body, null, null, null, null,
						null);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		this.istioManager.deleteVirtualservice(config, "virtualservice-" + name, namespace);
		this.istioManager.deleteDestinationrules(config, "dest-" + name, namespace);
		this.istioManager.deleteGateway(config, "gateway-" + name, namespace);
	}

	/**
	 * 根据selector查询资源
	 *
	 * @param config
	 * @param namespace
	 * @param selector
	 * @return java.util.List<java.lang.String>
	 * @author falcomlife
	 * @date 20-4-6
	 * @version 1.0.0
	 */
	@Override
	public List<String> getResourceBySelector(String config, String namespace, String selector) {
		List<String> result = new ArrayList<>();
		CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		BatchV1beta1Api batchV1beta1Api = (BatchV1beta1Api) K8sClientUtil.get(config, BatchV1beta1Api.class);
		BatchV1Api batchV1Api = (BatchV1Api) K8sClientUtil.get(config, BatchV1Api.class);
		ExtensionsV1beta1Api extensionsV1beta1Api = (ExtensionsV1beta1Api) K8sClientUtil.get(config,
				ExtensionsV1beta1Api.class);
		StorageV1Api storageV1Api = (StorageV1Api) K8sClientUtil.get(config, StorageV1Api.class);
		try {
			// 删除replicationcontroller
			V1ReplicationControllerList replicationControllerList =
					coreApi.listNamespacedReplicationController(namespace,
							null, null, null, null, selector, null, null, null, null);
			if (replicationControllerList.getItems() != null) {
				result.addAll(replicationControllerList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除deployment
			V1DeploymentList deploymentList = appsV1Api.listNamespacedDeployment(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (deploymentList.getItems() != null) {
				result.addAll(deploymentList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除statefulset
			V1StatefulSetList statefulSetList = appsV1Api.listNamespacedStatefulSet(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (statefulSetList.getItems() != null) {
				result.addAll(statefulSetList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除replicationcontroller
			V1ReplicaSetList replicaSetList =
					appsV1Api.listNamespacedReplicaSet(namespace,
							null, null, null, null, selector, null, null, null, null);
			if (replicaSetList.getItems() != null) {
				result.addAll(replicaSetList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除cronjobs
			V1beta1CronJobList cronJobList = batchV1beta1Api.listNamespacedCronJob(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (cronJobList.getItems() != null) {
				result.addAll(cronJobList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除job
			V1JobList jobList = batchV1Api.listNamespacedJob(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (jobList.getItems() != null) {
				result.addAll(jobList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除daemonset
			V1DaemonSetList daemonSetList = appsV1Api.listNamespacedDaemonSet(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (daemonSetList.getItems() != null) {
				result.addAll(daemonSetList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除pod
			V1PodList podList = coreApi.listNamespacedPod(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (podList.getItems() != null) {
				result.addAll(podList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除service
			V1ServiceList serviceList = coreApi.listNamespacedService(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (serviceList.getItems() != null) {
				result.addAll(serviceList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除ingress
			V1beta1IngressList ingressList = extensionsV1beta1Api.listNamespacedIngress(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (ingressList.getItems() != null) {
				result.addAll(ingressList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除configmap
			V1ConfigMapList configMapList = coreApi.listNamespacedConfigMap(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (configMapList.getItems() != null) {
				result.addAll(configMapList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除secret
			V1SecretList secretList = coreApi.listNamespacedSecret(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (secretList.getItems() != null) {
				result.addAll(secretList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除serviceaccount
			V1ServiceAccountList serviceAccountList = coreApi.listNamespacedServiceAccount(namespace,
					null, null, null, null, selector, null, null, null, null);
			if (serviceAccountList.getItems() != null) {
				result.addAll(serviceAccountList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除persistentVolume
			V1PersistentVolumeList persistentVolumeList = coreApi.listPersistentVolume(
					null, null, null, null, selector, null, null, null, null);
			if (persistentVolumeList.getItems() != null) {
				result.addAll(persistentVolumeList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除persistentVolume
			V1PersistentVolumeClaimList persistentVolumeClaimList =
					coreApi.listNamespacedPersistentVolumeClaim(namespace,
							null, null, null, null, selector, null, null, null, null);
			if (persistentVolumeClaimList.getItems() != null) {
				result.addAll(persistentVolumeClaimList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		try {
			// 删除persistentVolume
			V1StorageClassList storageClassList = storageV1Api.listStorageClass(
					null, null, null, null, selector, null, null, null, null);
			if (storageClassList.getItems() != null) {
				result.addAll(storageClassList.getItems().stream().map(r -> r.getMetadata().getUid()).collect(Collectors.toList()));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 根据uid查询资源
	 *
	 * @param type
	 * @return java.util.List<java.lang.Object>
	 * @author falcomlife
	 * @date 20-4-8
	 * @version 1.0.0
	 */
	@Override
	public List<Object> getAll(String config, String type) throws IOException {
		List<Object> result = new ArrayList<>();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(config.getBytes());
		ApiClient apiClient = Config.fromConfig(inputStream);
		Configuration.setDefaultApiClient(apiClient);
		Set<Class<?>> classes = ClassUtil.getClasses("io.kubernetes.client.apis");
		classes.stream().filter(c ->
				c.getName().contains("AppsV1Api") || c.getName().contains("BatchV1Api") || c.getName().contains(
						"BatchV2alpha1Api") || c.getName().contains("CoordinationV1beta1Api") || c.getName().contains(
						"CoreV1Api") || c.getName().contains("EventsV1beta1Api") || c.getName().contains(
						"ExtensionsV1beta1Api")
		).forEach(c -> {
			List<Method> methods = null;

			if (StringUtils.isNotBlank(type)) {
				methods =
						Arrays.stream(c.getMethods()).filter(m -> m.getName().contains("list") && m.getName().contains(
								"ForAllNamespaces") && !m.getName().contains("Async") && !m.getName().contains(
								"HttpInfo") && !m.getName().contains("Call") && m.getName().contains(type)).collect(Collectors.toList());
			} else {
				methods =
						Arrays.stream(c.getMethods()).filter(m -> m.getName().contains("list") && m.getName().contains(
								"ForAllNamespaces") && !m.getName().contains("Async") && !m.getName().contains(
								"HttpInfo") && !m.getName().contains("Call")).collect(Collectors.toList());
			}
			methods.stream().forEach(m -> {
				try {
					Object o = c.newInstance();
					Object res = m.invoke(o, null, null, null, null, null, null, null, null, null);
					result.add(res);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			});
		});
		return result;
	}

	/**
	 * 重启服务
	 *
	 * @param serviceId
	 * @return void
	 * @author falcomlife
	 * @date 20-5-16
	 * @version 1.0.0
	 */
	@Override
	public void restart(String config, String serviceId) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		appsV1Api.listDeploymentForAllNamespaces(null, null, null, "serviceId=" + serviceId, null, null, null, null,
				null).getItems().stream().forEach(deployment -> {
			int count = deployment.getSpec().getReplicas();
			try {
				deployment.getSpec().setReplicas(0);
				this.scaleDeployment(config, deployment.getMetadata().getNamespace(),
						deployment.getMetadata().getName(), deployment);
				deployment.getSpec().setReplicas(count);
				this.scaleDeployment(config, deployment.getMetadata().getNamespace(),
						deployment.getMetadata().getName(), deployment);
			} catch (ApiException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
		appsV1Api.listStatefulSetForAllNamespaces(null, null, null, "serviceId=" + serviceId, null, null, null, null,
				null).getItems().stream().forEach(deployment -> {
			int count = deployment.getSpec().getReplicas();
			try {
				deployment.getSpec().setReplicas(0);
				this.scaleStatefulSet(config, deployment.getMetadata().getNamespace(),
						deployment.getMetadata().getName(), deployment);
				deployment.getSpec().setReplicas(count);
				this.scaleStatefulSet(config, deployment.getMetadata().getNamespace(),
						deployment.getMetadata().getName(), deployment);
			} catch (ApiException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	/**
	 * 根据名称获取sservice
	 *
	 * @param namespace
	 * @param name
	 * @param config
	 * @return io.kubernetes.client.models.V1Service
	 * @author falcomlife
	 * @date 20-5-18
	 * @version 1.0.0
	 */
	@Override
	public V1Service getServiceByName(String namespace, String name, String config) throws ApiException {
		CoreV1Api coreV1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		V1ServiceList v1ServiceList = coreV1Api.listNamespacedService(namespace, null, null,
				null, null, null, null, null, null, null);
		if (v1ServiceList.getItems().isEmpty()) {
			return null;
		} else {
			List<V1Service> services =
					v1ServiceList.getItems().stream().filter(service -> service.getMetadata().getName().equals(name)).collect(Collectors.toList());
			return !services.isEmpty() ? services.get(0) : null;
		}
	}

	/**
	 * 根据名称获取statefulset
	 *
	 * @param namespace
	 * @param name
	 * @param config
	 * @return io.kubernetes.client.models.V1StatefulSet
	 * @author falcomlife
	 * @date 20-5-18
	 * @version 1.0.0
	 */
	@Override
	public V1StatefulSet getStatefulSetByName(String namespace, String name, String config) throws ApiException {
		AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		V1StatefulSetList v1StatefulSetList = appsV1Api.listNamespacedStatefulSet(namespace, null, null,
				null, null, null, null, null, null, null);
		if (v1StatefulSetList.getItems().isEmpty()) {
			return null;
		} else {
			List<V1StatefulSet> statefulSets =
					v1StatefulSetList.getItems().stream().filter(statefulSet -> statefulSet.getMetadata().getName().equals(name)).collect(Collectors.toList());
			return !statefulSets.isEmpty() ? statefulSets.get(0) : null;
		}
	}

	/**
	* 获取所有的pod
	* @author falcomlife
	* @date 20-6-2
	* @version 1.0.0
	* @return java.util.List<io.kubernetes.client.models.V1Pod>
	* @param config
	*/
	@Override
	public List<V1Pod> getAllPods(String config) throws ApiException {
		CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		return v1Api.listPodForAllNamespaces(null,null,null,null,null,null,null,null,null).getItems();
	}
}

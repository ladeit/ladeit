package com.ladeit.biz.manager.impl;


import com.ladeit.biz.manager.IstioManager;
import com.ladeit.biz.manager.K8sContainerManager;
import com.ladeit.common.enu.KindType;
import com.ladeit.common.holder.K8sHolder;
import com.ladeit.pojo.ao.YamlContentAO;
import com.ladeit.util.k8s.K8sClientUtil;
import com.ladeit.util.k8s.LabelToString;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.kubernetes.client.ApiCallback;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.*;
import io.kubernetes.client.models.*;
import io.kubernetes.client.proto.V1;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import com.ladeit.util.istio.YAML;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class K8sContainerManagerImpl implements K8sContainerManager {

	@Autowired
	private IstioManager istioManager;

	/**
	 * 运行YAML
	 *
	 * @param yamlContent
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public String replaceByYaml(YamlContentAO yamlContent, String config, String name, ApiCallback apiCallback) throws IOException,
			ApiException {
		if (KindType.V1ReplicationController.toString().equals(yamlContent.getKindType())) {
			CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1ReplicationController v1ReplicationController = Yaml.loadAs(yamlContent.getContent(),
					V1ReplicationController.class);
			v1Api.replaceNamespacedReplicationController(v1ReplicationController.getMetadata().getName(),
					yamlContent.getNameSpace(), v1ReplicationController, null, null);
		} else if (KindType.V1Deployment.toString().equals(yamlContent.getKindType())) {
			AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
			V1Deployment v1Deployment = Yaml.loadAs(yamlContent.getContent(), V1Deployment.class);
			v1Api.replaceNamespacedDeploymentAsync(v1Deployment.getMetadata().getName(),
					yamlContent.getNameSpace(), v1Deployment, null, null, apiCallback);
		} else if (KindType.V1Statefulset.toString().equals(yamlContent.getKindType())) {
			AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
			V1StatefulSet v1StatefulSet = Yaml.loadAs(yamlContent.getContent(), V1StatefulSet.class);
			v1Api.replaceNamespacedStatefulSet(v1StatefulSet.getMetadata().getName(), yamlContent.getNameSpace(),
					v1StatefulSet, null, null);
		} else if (KindType.V1Replicaset.toString().equals(yamlContent.getKindType())) {
			AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
			V1ReplicaSet v1ReplicaSet = Yaml.loadAs(yamlContent.getContent(), V1ReplicaSet.class);
			v1Api.replaceNamespacedReplicaSet(v1ReplicaSet.getMetadata().getName(), yamlContent.getNameSpace(),
					v1ReplicaSet, null, null);
		} else if (KindType.V1Job.toString().equals(yamlContent.getKindType())) {
			BatchV1Api batchV1Api = (BatchV1Api) K8sClientUtil.get(config, BatchV1Api.class);
			V1Job v1Job = Yaml.loadAs(yamlContent.getContent(), V1Job.class);
			batchV1Api.replaceNamespacedJob(v1Job.getMetadata().getName(), yamlContent.getNameSpace(), v1Job, null
					, null);
		} else if (KindType.V1beta1CronJob.toString().equals(yamlContent.getKindType())) {
			BatchV1beta1Api batchV1beta1Api = (BatchV1beta1Api) K8sClientUtil.get(config, BatchV1beta1Api.class);
			V1beta1CronJob v1beta1CronJob = Yaml.loadAs(yamlContent.getContent(), V1beta1CronJob.class);
			batchV1beta1Api.replaceNamespacedCronJob(v1beta1CronJob.getMetadata().getName(),
					yamlContent.getNameSpace(), v1beta1CronJob, null, null);
		} else if (KindType.V1Daemonset.toString().equals(yamlContent.getKindType())) {
			AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
			V1DaemonSet v1DaemonSet = Yaml.loadAs(yamlContent.getContent(), V1DaemonSet.class);
			appsV1Api.replaceNamespacedDaemonSet(v1DaemonSet.getMetadata().getName(), yamlContent.getNameSpace(),
					v1DaemonSet, null, null);
		} else if (KindType.V1Pod.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1Pod v1Pod = Yaml.loadAs(yamlContent.getContent(), V1Pod.class);
			coreApi.replaceNamespacedPod(v1Pod.getMetadata().getName(), yamlContent.getNameSpace(), v1Pod, null,
					null);
		} else if (KindType.V1Service.toString().equals(yamlContent.getKindType())) {
			CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1Service v1Service = Yaml.loadAs(yamlContent.getContent(), V1Service.class);
			v1Api.replaceNamespacedService(v1Service.getMetadata().getName(), yamlContent.getNameSpace(),
					v1Service, null, null);

		} else if (KindType.V1beta1Ingress.toString().equals(yamlContent.getKindType())) {
			ExtensionsV1beta1Api extensionsV1beta1Api = (ExtensionsV1beta1Api) K8sClientUtil.get(config,
					ExtensionsV1beta1Api.class);
			V1beta1Ingress v1beta1Ingress = Yaml.loadAs(yamlContent.getContent(), V1beta1Ingress.class);
			extensionsV1beta1Api.replaceNamespacedIngress(v1beta1Ingress.getMetadata().getName(),
					yamlContent.getNameSpace(), v1beta1Ingress, null, null);
		} else if (KindType.V1ConfigMap.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1ConfigMap v1ConfigMap = Yaml.loadAs(yamlContent.getContent(), V1ConfigMap.class);
			coreApi.replaceNamespacedConfigMap(v1ConfigMap.getMetadata().getName(), yamlContent.getNameSpace(),
					v1ConfigMap, null, null);
		} else if (KindType.V1Secret.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1Secret v1Secret = Yaml.loadAs(yamlContent.getContent(), V1Secret.class);
			coreApi.replaceNamespacedSecret(v1Secret.getMetadata().getName(), yamlContent.getNameSpace(), v1Secret
					, null, null);
		} else if (KindType.V1ServiceAccount.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1ServiceAccount v1ServiceAccount = Yaml.loadAs(yamlContent.getContent(), V1ServiceAccount.class);
			coreApi.replaceNamespacedServiceAccount(v1ServiceAccount.getMetadata().getName(),
					yamlContent.getNameSpace(), v1ServiceAccount, null, null);
		} else if (KindType.V1PersistentVolume.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1PersistentVolume v1PersistentVolume = Yaml.loadAs(yamlContent.getContent(),
					V1PersistentVolume.class);
			coreApi.replacePersistentVolume(name, v1PersistentVolume, null,
					null);
		} else if (KindType.V1PersistentVolumeClaim.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1PersistentVolumeClaim v1PersistentVolumeClaim = Yaml.loadAs(yamlContent.getContent(),
					V1PersistentVolumeClaim.class);
			coreApi.replaceNamespacedPersistentVolumeClaim(v1PersistentVolumeClaim.getMetadata().getName(),
					yamlContent.getNameSpace(), v1PersistentVolumeClaim, null, null);
		} else if (KindType.V1StorageClass.toString().equals(yamlContent.getKindType())) {
			StorageV1Api storageV1Api = (StorageV1Api) K8sClientUtil.get(config, StorageV1Api.class);
			V1StorageClass v1StorageClass = Yaml.loadAs(yamlContent.getContent(), V1StorageClass.class);
			storageV1Api.replaceStorageClass(name, v1StorageClass, null, null);
		}
		return "success";
	}

	/**
	 * 通过YAML文件创建
	 *
	 * @param yamlContent
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public String createByYaml(YamlContentAO yamlContent, String config, String name) throws IOException,
			ApiException {
		if (KindType.V1ReplicationController.toString().equals(yamlContent.getKindType())) {
			CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1ReplicationController v1ReplicationController = Yaml.loadAs(yamlContent.getContent(),
					V1ReplicationController.class);
			v1ReplicationController.getMetadata().setNamespace(yamlContent.getNameSpace());
			v1Api.createNamespacedReplicationController(yamlContent.getNameSpace(), v1ReplicationController,
					true, null, null);
		} else if (KindType.V1Deployment.toString().equals(yamlContent.getKindType())) {
			AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
			V1Deployment v1Deployment = Yaml.loadAs(yamlContent.getContent(), V1Deployment.class);
			v1Deployment.getMetadata().setNamespace(yamlContent.getNameSpace());
			v1Api.createNamespacedDeployment(yamlContent.getNameSpace(), v1Deployment, true, null, null);
		} else if (KindType.V1Statefulset.toString().equals(yamlContent.getKindType())) {
			AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
			V1StatefulSet v1StatefulSet = Yaml.loadAs(yamlContent.getContent(), V1StatefulSet.class);
			v1StatefulSet.getMetadata().setNamespace(yamlContent.getNameSpace());
			v1Api.createNamespacedStatefulSet(yamlContent.getNameSpace(), v1StatefulSet, true, null, null);
		} else if (KindType.V1Replicaset.toString().equals(yamlContent.getKindType())) {
			AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
			V1ReplicaSet v1ReplicaSet = Yaml.loadAs(yamlContent.getContent(), V1ReplicaSet.class);
			v1ReplicaSet.getMetadata().setNamespace(yamlContent.getNameSpace());
			v1Api.createNamespacedReplicaSet(yamlContent.getNameSpace(), v1ReplicaSet, true, null, null);
		} else if (KindType.V1Job.toString().equals(yamlContent.getKindType())) {
			BatchV1Api batchV1Api = (BatchV1Api) K8sClientUtil.get(config, BatchV1Api.class);
			V1Job v1Job = Yaml.loadAs(yamlContent.getContent(), V1Job.class);
			v1Job.getMetadata().setNamespace(yamlContent.getNameSpace());
			batchV1Api.createNamespacedJob(yamlContent.getNameSpace(), v1Job, true, null, null);
		} else if (KindType.V1beta1CronJob.toString().equals(yamlContent.getKindType())) {
			BatchV1beta1Api batchV1beta1Api = (BatchV1beta1Api) K8sClientUtil.get(config, BatchV1beta1Api.class);
			V1beta1CronJob v1beta1CronJob = Yaml.loadAs(yamlContent.getContent(), V1beta1CronJob.class);
			batchV1beta1Api.createNamespacedCronJob(yamlContent.getNameSpace(), v1beta1CronJob, true, null,
					null);
		} else if (KindType.V1Daemonset.toString().equals(yamlContent.getKindType())) {
			AppsV1Api appsV1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
			V1DaemonSet v1DaemonSet = Yaml.loadAs(yamlContent.getContent(), V1DaemonSet.class);
			v1DaemonSet.getMetadata().setNamespace(yamlContent.getNameSpace());
			appsV1Api.createNamespacedDaemonSet(yamlContent.getNameSpace(), v1DaemonSet, true, null, null);
		} else if (KindType.V1Pod.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1Pod v1Pod = Yaml.loadAs(yamlContent.getContent(), V1Pod.class);
			v1Pod.getMetadata().setNamespace(yamlContent.getNameSpace());
			coreApi.createNamespacedPod(yamlContent.getNameSpace(), v1Pod, true, null, null);
		} else if (KindType.V1Service.toString().equals(yamlContent.getKindType())) {
			CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1Service v1Service = Yaml.loadAs(yamlContent.getContent(), V1Service.class);
			v1Service.getMetadata().setNamespace(yamlContent.getNameSpace());
			v1Api.createNamespacedService(yamlContent.getNameSpace(), v1Service, true, null, null);
		} else if (KindType.V1beta1Ingress.toString().equals(yamlContent.getKindType())) {
			ExtensionsV1beta1Api extensionsV1beta1Api = (ExtensionsV1beta1Api) K8sClientUtil.get(config,
					ExtensionsV1beta1Api.class);
			V1beta1Ingress v1beta1Ingress = Yaml.loadAs(yamlContent.getContent(), V1beta1Ingress.class);
			v1beta1Ingress.getMetadata().setNamespace(yamlContent.getNameSpace());
			extensionsV1beta1Api.createNamespacedIngress(yamlContent.getNameSpace(), v1beta1Ingress, true,
					null, null);
		} else if (KindType.V1ConfigMap.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1ConfigMap v1ConfigMap = Yaml.loadAs(yamlContent.getContent(), V1ConfigMap.class);
			v1ConfigMap.getMetadata().setNamespace(yamlContent.getNameSpace());
			coreApi.createNamespacedConfigMap(yamlContent.getNameSpace(), v1ConfigMap, true, null, null);
		} else if (KindType.V1Secret.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1Secret v1Secret = Yaml.loadAs(yamlContent.getContent(), V1Secret.class);
			v1Secret.getMetadata().setNamespace(yamlContent.getNameSpace());
			if (v1Secret.getStringData() != null) {
				v1Secret.getStringData().put("namespace",
						Base64.getEncoder().encodeToString(yamlContent.getNameSpace().getBytes()));
			}
			coreApi.createNamespacedSecret(yamlContent.getNameSpace(), v1Secret, true, null, null);
		} else if (KindType.V1ServiceAccount.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1ServiceAccount v1ServiceAccount = Yaml.loadAs(yamlContent.getContent(), V1ServiceAccount.class);
			v1ServiceAccount.getMetadata().setNamespace(yamlContent.getNameSpace());
			coreApi.createNamespacedServiceAccount(yamlContent.getNameSpace(), v1ServiceAccount, true, null,
					null);
		} else if (KindType.V1PersistentVolume.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1PersistentVolume v1PersistentVolume = Yaml.loadAs(yamlContent.getContent(), V1PersistentVolume.class);
			coreApi.createPersistentVolume(v1PersistentVolume, true, null, null);
		} else if (KindType.V1PersistentVolumeClaim.toString().equals(yamlContent.getKindType())) {
			CoreV1Api coreApi = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
			V1PersistentVolumeClaim v1PersistentVolumeClaim = Yaml.loadAs(yamlContent.getContent(),
					V1PersistentVolumeClaim.class);
			v1PersistentVolumeClaim.getMetadata().setNamespace(yamlContent.getNameSpace());
			coreApi.createNamespacedPersistentVolumeClaim(yamlContent.getNameSpace(), v1PersistentVolumeClaim, true,
					null, null);
		} else if (KindType.V1StorageClass.toString().equals(yamlContent.getKindType())) {
			StorageV1Api storageV1Api = (StorageV1Api) K8sClientUtil.get(config, StorageV1Api.class);
			V1StorageClass v1StorageClass = Yaml.loadAs(yamlContent.getContent(), V1StorageClass.class);
			storageV1Api.createStorageClass(v1StorageClass, true, null, null);
		} else if ("VirtualService".equals(yamlContent.getKindType())) {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(yamlContent.getContent().getBytes());
			VirtualService v = YAML.loadIstioResource(inputStream, VirtualService.class);
			this.istioManager.createVirtualServices(config, v);
		} else if ("DestinationRule".equals(yamlContent.getKindType())) {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(yamlContent.getContent().getBytes());
			DestinationRule d = YAML.loadIstioResource(inputStream, DestinationRule.class);
			this.istioManager.createDestinationrules(config, d);
		} else if ("Gateway".equals(yamlContent.getKindType())) {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(yamlContent.getContent().getBytes());
			DestinationRule d = YAML.loadIstioResource(inputStream, DestinationRule.class);
			this.istioManager.createDestinationrules(config, d);
		}
		return "success";
	}

	/**
	 * 净化资源
	 *
	 * @param resource
	 * @return void
	 * @author falcomlife
	 * @date 19-11-27
	 * @version 1.0.0
	 */
	private Object purifyResource(Object resource) throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException {
		Object meta = resource.getClass().getMethod("getMetadata").invoke(resource);
		meta.getClass().getMethod("setNamespace").invoke(meta, null);
		meta.getClass().getMethod("setAnnotations").invoke(meta, null);
		meta.getClass().getMethod("setUid").invoke(meta, null);
		meta.getClass().getMethod("setSelfLink").invoke(meta, null);
		meta.getClass().getMethod("setResourceVersion").invoke(meta, null);
		meta.getClass().getMethod("setCreationTimestamp").invoke(meta, null);
		return meta;
	}

	/**
	 * 运行deployment
	 *
	 * @param deployment
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public String applyYaml(String config, V1Deployment deployment) throws ApiException {
		AppsV1Api v1Api = (AppsV1Api) K8sClientUtil.get(config, AppsV1Api.class);
		String labels = LabelToString.tran(deployment.getMetadata().getLabels());
		V1DeploymentList deploymentList = v1Api.listNamespacedDeployment(deployment.getMetadata().getNamespace(), null
				, null, null, null, labels, null, null, null, null);
		Boolean flag = false;
		for (V1Deployment deploymentInner : deploymentList.getItems()) {
			if (deploymentInner.getMetadata().getName().equals(deployment.getMetadata().getName())) {
				flag = true;
			}
		}
		if (flag) {
			v1Api.replaceNamespacedDeployment(deployment.getMetadata().getName(),
					deployment.getMetadata().getNamespace(), deployment, null, null);
		} else {
			v1Api.createNamespacedDeployment(deployment.getMetadata().getNamespace(), deployment, true, null,
					null);
		}
		return "SUCCESS";
	}

	/**
	 * 运行yaml
	 *
	 * @param config
	 * @param service
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-11-18
	 * @version 1.0.0
	 */
	@Override
	public String applyYaml(String config, V1Service service) throws ApiException {
		CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		String labels = LabelToString.tran(service.getMetadata().getLabels());
		V1ServiceList v1ServiceList = v1Api.listNamespacedService(service.getMetadata().getNamespace(), null
				, null, null, null, labels, null, null, null, null);
		Boolean flag = false;
		V1Service serviceNow = null;
		for (V1Service serviceInner : v1ServiceList.getItems()) {
			if (serviceInner.getMetadata().getName().equals(service.getMetadata().getName())) {
				serviceNow = serviceInner;
				flag = true;
			}
		}
		if (flag) {
			service.getSpec().setClusterIP(serviceNow.getSpec().getClusterIP());
			V1DeleteOptions deleteOptions = new V1DeleteOptions();
			v1Api.deleteNamespacedService(service.getMetadata().getName(),
					service.getMetadata().getNamespace(), deleteOptions, null, null, null, null, null);
			v1Api.createNamespacedService(service.getMetadata().getNamespace(), service, true, null,
					null);
		} else {
			v1Api.createNamespacedService(service.getMetadata().getNamespace(), service, true, null,
					null);
		}
		return "SUCCESS";
	}

	/**
	 * 直接运行yaml文件
	 *
	 * @param yaml
	 * @param config
	 * @return void
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	@Override
	public void applyYaml(String yaml, String config) throws IOException {
		DefaultKubernetesClient client = this.getKubernetesClient(config);
		Yaml.load(yaml);
		client.resource(yaml).createOrReplace();
	}

	/**
	 * 根据uid查询pod
	 *
	 * @param uid
	 * @return
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	@Override
	public V1Pod getPodByUid(String uid, String config) throws ApiException {
		CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		return v1Api.listPodForAllNamespaces(null, "metadata.uid=" + uid, null, null, null, null, null, null, null).getItems().get(0);

	}


	/**
	 * 查询pod list
	 *
	 * @param namespace
	 * @param config
	 * @return java.util.List<io.kubernetes.client.models.V1Pod>
	 * @author falcomlife
	 * @date 20-4-11
	 * @version 1.0.0
	 */
	@Override
	public List<V1Pod> getPodList(String namespace, String config) throws ApiException {
		CoreV1Api v1Api = (CoreV1Api) K8sClientUtil.get(config, CoreV1Api.class);
		return v1Api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null).getItems();
	}

	/**
	 * 生成istioclient
	 *
	 * @param content
	 * @return me.snowdrop.istio.client.IstioClient
	 * @author falcomlife
	 * @date 19-11-19
	 * @version 1.0.0
	 */
	private DefaultKubernetesClient getKubernetesClient(String content) throws IOException {
		io.fabric8.kubernetes.client.Config config = io.fabric8.kubernetes.client.Config.fromKubeconfig(content);
		DefaultKubernetesClient k8sClient = new DefaultKubernetesClient(config);
		return k8sClient;
	}
}

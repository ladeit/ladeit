package com.ladeit.biz.manager;

import com.ladeit.common.ExecuteResult;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.*;

import java.math.BigDecimal;
import java.util.List;

public interface K8sClusterManager {

	/**
	 * 测试集群链接状态
	 *
	 * @param config
	 * @return boolean
	 * @author falcomlife
	 * @date 20-3-16
	 * @version 1.0.0
	 */
	boolean connectTest(String config) throws ApiException;

	/**
	 * 查询namespace
	 *
	 * @param clusterconfig
	 * @author falcomlife
	 * @date 19-9-20
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1Namespace>> listNamespace(String clusterconfig);

	/**
	 * 得到replicasets
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1ReplicationController> getReplicationControllers(String serviceId, String k8sKubeconfig, String namespace) throws ApiException
	;

	/**
	 * deployment
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1Deployment>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1Deployment> getDeployments(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * statefulset
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1StatefulSet>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1StatefulSet> getStatefulsets(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * cronjob
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1beta1CronJob>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1beta1CronJob> getCronJobs(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * job
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1Job>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1Job> getJobs(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * daemonset
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1DaemonSet>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1DaemonSet> getDaemonSets(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * pod
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1Pod>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1Pod> getPods(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * service
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1Service>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1Service> getServices(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * ingress
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1beta1Ingress>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1beta1Ingress> getIngresses(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * configmap
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1ConfigMap>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1ConfigMap> getConfigMaps(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * secret
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1Secret>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1Secret> getSecrets(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * serviceaccount
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1ServiceAccount>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1ServiceAccount> getServiceAccounts(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * persistentVolume
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1PersistentVolume>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1PersistentVolume> getPersistentVolumes(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * persistentVolumeClaim
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1PersistentVolumeClaim>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1PersistentVolumeClaim> getPersistentVolumeClaims(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * storageclass
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return java.util.List<io.kubernetes.client.models.V1StorageClass>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	List<V1StorageClass> getStorageClasses(String serviceId, String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * 查询events
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @param fieldSelector
	 * @return java.util.List<io.kubernetes.client.models.V1beta1Event>
	 * @author falcomlife
	 * @date 20-1-15
	 * @version 1.0.0
	 */
	List<V1beta1Event> getResourceEvent(String k8sKubeconfig, String namespace, String fieldSelector) throws ApiException;

	/**
	 * 查询namespace
	 *
	 * @param k8sKubeconfig
	 * @param namespace
	 * @return io.kubernetes.client.models.V1Namespace
	 * @author falcomlife
	 * @date 20-3-12
	 * @version 1.0.0
	 */
	V1Namespace getNamespace(String k8sKubeconfig, String namespace) throws ApiException;

	/**
	 * 创建资源配额
	 *
	 * @param namespace
	 * @param limitcpu
	 * @param limitmemory
	 * @param requestcpu
	 * @param requestmemory
	 * @param config
	 * @return void
	 * @author falcomlife
	 * @date 20-3-16
	 * @version 1.0.0
	 */
	void createResourceQuota(String namespace, BigDecimal limitcpu, BigDecimal limitmemory, BigDecimal requestcpu,
							 BigDecimal requestmemory, String config) throws ApiException;

	/**
	 * 查看resourcequota
	 *
	 * @param namespace
	 * @param config
	 * @return io.kubernetes.client.models.V1ResourceQuota
	 * @author falcomlife
	 * @date 20-3-25
	 * @version 1.0.0
	 */
	List<V1ResourceQuota> getResourceQuota(String namespace, String config) throws ApiException;

	/**
	 * 删除resourcequota
	 *
	 * @param namespace
	 * @param config
	 * @return io.kubernetes.client.models.V1ResourceQuota
	 * @author falcomlife
	 * @date 20-3-25
	 * @version 1.0.0
	 */
	void deleteResourceQuota(String name, String namespace, String config) throws ApiException;

	/**
	 * 创建namespace
	 *
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 20-5-25
	 * @version 1.0.0
	 */
	V1Namespace createNamespace(V1Namespace namespace, String config) throws ApiException;
}


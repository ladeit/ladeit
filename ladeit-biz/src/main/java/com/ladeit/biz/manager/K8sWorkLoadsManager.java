package com.ladeit.biz.manager;

import com.ladeit.common.ExecuteResult;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface K8sWorkLoadsManager {

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
	ExecuteResult<List<V1Pod>> getPods(String labelSelector, String config);

	/**
	 * 通过releaseid查询pod
	 *
	 * @param releaseid
	 * @return java.util.List<io.kubernetes.client.models.V1Pod>
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	List<V1Pod> getPodsByReleaseId(String releaseid, String config) throws ApiException;

	/**
	 * 查询CronJob
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1beta1CronJob>>
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1beta1CronJob>> getCronJobs(String labelSelector, String config);

	/**
	 * 查询replicationController
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1ReplicationController>>
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1ReplicationController>> getReplicationControllers(String labelSelector, String config);

	/**
	 * 得到daemonset
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1DaemonSet>>
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1DaemonSet>> getDaemonSet(String labelSelector, String config);

	/**
	 * 得到deployment
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1Deployment>>
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1Deployment>> getDeployment(String labelSelector, String config);

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
	V1Deployment getDeploymentByName(String namespace, String name, String config) throws ApiException;

	/**
	 * 得到job
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1Job>>
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1Job>> getJob(String labelSelector, String config);

	/**
	 * 得到statefulset
	 *
	 * @param labelSelector
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1StatefulSet>>
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1StatefulSet>> getStatefulSet(String labelSelector, String config);

	/**
	 * 新建release(滚动更新)
	 *
	 * @param config
	 * @param deployment
	 * @return void
	 * @author falcomlife
	 * @date 19-12-23
	 * @version 1.0.0
	 */
	void createDeployment(String config, V1Deployment deployment) throws ApiException;

	/**
	 * 运行service
	 *
	 * @param config
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	void createService(String config, V1Service service) throws ApiException;

	/**
	 * 通过name和namespace查询数据
	 *
	 * @param
	 * @return java.util.List<io.kubernetes.client.models.V1ReplicationController>
	 * @author falcomlife
	 * @date 19-11-26
	 * @version 1.0.0
	 */
	Object getByNameNamespace(String serviceId, String context, String resourcename, String namespace, String name) throws IOException,
			InvocationTargetException, IllegalAccessException, NoSuchMethodException, ApiException;

	/**
	 * 查询service
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1Service>>
	 * @author falcomlife
	 * @date 19-12-4
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1Service>> getService(String serviceId, String config);

	/**
	 * 查询ingress
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1beta1Ingress>>
	 * @author falcomlife
	 * @date 19-12-4
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1beta1Ingress>> getIngress(String serviceId, String config);

	/**
	 * 查询configmap
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1Service>>
	 * @author falcomlife
	 * @date 19-12-9
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1ConfigMap>> getConfigMap(String serviceId, String config);

	/**
	 * 查询pvc
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<io.kubernetes.client.models.V1ConfigMap>>
	 * @author falcomlife
	 * @date 19-12-9
	 * @version 1.0.0
	 */
	ExecuteResult<List<V1PersistentVolumeClaim>> getPvcs(String serviceId, String config);

	/**
	 * 根据名字查询storageclass
	 *
	 * @param storageClassName
	 * @return io.kubernetes.client.models.V1StorageClass
	 * @author falcomlife
	 * @date 19-12-12
	 * @version 1.0.0
	 */
	V1StorageClass getStorageClass(String config, String storageClassName) throws ApiException;

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
	List<V1PersistentVolume> getPv(String config, String storageClassName) throws ApiException;

	/**
	 * 更新release
	 *
	 * @param deployment
	 * @return void
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	V1Deployment replaceDeployment(String config, V1Deployment deployment) throws ApiException;

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
	void replaceService(String config, V1Service service) throws ApiException;

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
	V1StatefulSet replaceStatefulSet(String config, V1StatefulSet statefulSet) throws ApiException;

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
	void replaceReplicationController(String config, V1ReplicationController replicationController) throws ApiException;

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
	V1Scale scaleDeployment(String config, String namespace, String name, V1Deployment deployment) throws ApiException;

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
	V1Scale scaleReplicationControllers(String config, String namespace, String name,
										V1ReplicationController replicationControllers) throws ApiException;

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
	V1Scale scaleReplicaSet(String config, String namespace, String name, V1ReplicaSet replicaSet) throws ApiException;

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
	V1Scale scaleStatefulSet(String config, String namespace, String name, V1StatefulSet statefulSet) throws ApiException;

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
	void createStatefulSet(String config, V1StatefulSet statefulSet) throws ApiException;

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
	void createReplicationController(String config, V1ReplicationController replicationController) throws ApiException;

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
	void deleteResources(String serviceId, String namespace, String config, String name) throws IOException;

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
	List<String> getResourceBySelector(String config, String namespace, String selector);

	/**
	 * 根据uid查询资源
	 *
	 * @param type
	 * @return java.util.List<java.lang.Object>
	 * @author falcomlife
	 * @date 20-4-8
	 * @version 1.0.0
	 */
	List<Object> getAll(String config, String type) throws IOException;

	/**
	 * 重启服务
	 *
	 * @param serviceId
	 * @return void
	 * @author falcomlife
	 * @date 20-5-16
	 * @version 1.0.0
	 */
	void restart(String config, String serviceId) throws ApiException;

	/**
	 * 根据名称查询service
	 *
	 * @param namespace
	 * @param name
	 * @param k8sKubeconfig
	 * @return void
	 * @author falcomlife
	 * @date 20-5-18
	 * @version 1.0.0
	 */
	V1Service getServiceByName(String namespace, String name, String k8sKubeconfig) throws ApiException;

	/**
	 * 获取statefulset
	 *
	 * @param namespace
	 * @param name
	 * @param config
	 * @return io.kubernetes.client.models.V1StatefulSet
	 * @author falcomlife
	 * @date 20-5-18
	 * @version 1.0.0
	 */
	V1StatefulSet getStatefulSetByName(String namespace, String name, String config) throws ApiException;
}

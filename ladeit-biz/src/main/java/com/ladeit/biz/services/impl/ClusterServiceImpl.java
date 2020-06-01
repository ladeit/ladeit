package com.ladeit.biz.services.impl;

import com.alibaba.fastjson.JSONObject;
import com.ladeit.biz.annotation.Authority;
import com.ladeit.biz.dao.*;
import com.ladeit.biz.manager.K8sClusterManager;
import com.ladeit.biz.services.ClusterService;
import com.ladeit.biz.services.EnvService;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.doo.*;
import com.ladeit.util.ListUtil;
import com.ladeit.util.auth.TokenUtil;
import com.ladeit.util.k8s.UnitUtil;
import io.ebean.SqlRow;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.*;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @program: ladeit
 * @description: ClusterServiceImpl
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
@Service
public class ClusterServiceImpl implements ClusterService {

	@Autowired
	private ClusterDao clusterDao;
	@Autowired
	private K8sClusterManager clusterManager;
	@Autowired
	private UserClusterRelationDao userClusterRelationDao;
	@Autowired
	private UserEnvRelationDao userEnvRelationDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private ServiceGroupServiceImpl serviceGroupService;
	@Autowired
	private EnvDao envDao;
	@Autowired
	private ServiceDao serviceDao;
	@Autowired
	private ServiceGroupDao serviceGroupDao;
	@Autowired
	private MessageUtils messageUtils;
	@Autowired
	private EnvService envService;
	@Resource(name = "globalOkHttpClient")
	private OkHttpClient okHttpClient;

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return com.ladeit.pojo.doo.Cluster
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public Cluster getClusterById(String id) {
		return clusterDao.getClusterById(id);
	}

	/**
	 * 创建cluster
	 *
	 * @param cluster
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@Override
	@Transactional
	public ExecuteResult<String> createCluster(Cluster cluster) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		Cluster clusterexit = clusterDao.getClusterOneByName(cluster.getK8sName());
		if (clusterexit != null) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0003", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		// 首先尝试是否能连接上集群，如果不行就直接返回错误
		boolean notconnect = false;
		try {
			notconnect = this.clusterManager.connectTest(cluster.getK8sKubeconfig());
		} catch (ClassCastException e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0004", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		} catch (Exception e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0005", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		if (notconnect) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0005", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		String clusterId = UUID.randomUUID().toString();
		cluster.setId(clusterId);
		cluster.setCreateAt(new Date());
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		cluster.setCreateBy(user.getUsername());
		cluster.setCreateById(user.getId());
		cluster.setIsdel(false);
		String inviteCode = null;
		try {
			inviteCode = TokenUtil.createToken("clusterinvitecode" + System.currentTimeMillis());
		} catch (Exception e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0006", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		cluster.setInviteCode(inviteCode);
		this.clusterDao.createCluster(cluster);
		//集群创建人作为集群的默认管理员
		UserClusterRelation userClusterRelation = new UserClusterRelation();
		userClusterRelation.setId(UUID.randomUUID().toString());
		userClusterRelation.setAccessLevel("RW");
		userClusterRelation.setClusterId(clusterId);
		userClusterRelation.setUserId(user.getId());
		userClusterRelation.setCreateAt(new Date());
		userClusterRelationDao.insert(userClusterRelation);
		ExecuteResult<List<V1Namespace>> namespaces = this.clusterManager.listNamespace(cluster.getK8sKubeconfig());
		if (namespaces.getResult() != null && !namespaces.getResult().isEmpty()) {
			for (V1Namespace namespace : namespaces.getResult()) {
				Env env = new Env();
				env.setId(namespace.getMetadata().getUid());
				env.setClusterId(clusterId);
				env.setNamespace(namespace.getMetadata().getName());
				env.setEnvName(namespace.getMetadata().getName());
				List<V1ResourceQuota> rqs = this.clusterManager.getResourceQuota(namespace.getMetadata().getName(),
						cluster.getK8sKubeconfig());
				for (V1ResourceQuota v1ResourceQuota : rqs) {
					String[] cpurequest = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("requests" +
							".cpu"), "cpu");
					String[] memrequest = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("requests" +
							".memory"), "mem");
					String[] cpulimit = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("limits.cpu"),
							"cpu");
					String[] memlimit =
							UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("limits.memory"), "mem");
					env.setCpuRequest(StringUtils.isNotBlank(cpurequest[0]) ? Integer.parseInt(cpurequest[0]) : null);
					env.setCpuRequestUnit(cpurequest[1]);
					env.setMemRequest(StringUtils.isNotBlank(memrequest[0]) ? Integer.parseInt(memrequest[0]) : null);
					env.setMemRequestUnit(memrequest[1]);
					env.setCpuLimit(StringUtils.isNotBlank(cpulimit[0]) ? Integer.parseInt(cpulimit[0]) : null);
					env.setCpuLimitUnit(cpulimit[1]);
					env.setMemLimit(StringUtils.isNotBlank(memlimit[0]) ? Integer.parseInt(memlimit[0]) : null);
					env.setMemLimitUnit(memlimit[1]);
					env.setResourceQuota(true);
				}
				this.envService.createEnvWithoutK8s(env);
			}
		}
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);

		return result;
	}

	/**
	 * 更新cluster
	 *
	 * @param bzK8sClusterBO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "cluster", level = "W")
	public ExecuteResult<String> updateCluster(String clusterid, Cluster bzK8sClusterBO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		boolean notconnect = false;
		try {
			notconnect = this.clusterManager.connectTest(bzK8sClusterBO.getK8sKubeconfig());
		} catch (ClassCastException e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0004", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		} catch (Exception e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0005", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		if (notconnect) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0005", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		String clusterId = bzK8sClusterBO.getId();
		Cluster cluster = clusterDao.getClusterById(clusterId);
		cluster.setK8sName(bzK8sClusterBO.getK8sName());
		cluster.setK8sKubeconfig(bzK8sClusterBO.getK8sKubeconfig());
		clusterDao.update(cluster);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 查询cluster列表
	 *
	 * @param cluster
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<Cluster>> getCluster(Cluster cluster) {
		ExecuteResult<List<Cluster>> result = new ExecuteResult<>();
		List<Cluster> list = this.clusterDao.getCluster(cluster);
		result.setResult(list);
		return result;
	}

	/**
	 * 查询cluster列表
	 *
	 * @param cluster
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<Cluster> getOneCluster(Cluster cluster) {
		ExecuteResult<Cluster> result = new ExecuteResult<>();
		Cluster resultdo = this.clusterDao.getOneCluster(cluster);
		if (resultdo == null) {
			result.setCode(Code.NOTFOUND);
		} else {
			result.setResult(resultdo);
		}
		return result;
	}

	/**
	 * 查询namespace
	 *
	 * @param clusterId
	 * @author falcomlife
	 * @date 19-9-20
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<String>> listNamespace(String clusterId) throws IOException {
		ExecuteResult<List<String>> result = new ExecuteResult<>();
		List<String> list = new ArrayList<>();
		Cluster bzK8sClusterBO = new Cluster();
		bzK8sClusterBO.setId(clusterId);
		ExecuteResult<Cluster> clusterbo = this.getOneCluster(bzK8sClusterBO);
		ExecuteResult<List<V1Namespace>> resultTem = null;
		try {
			resultTem = this.clusterManager.listNamespace(clusterbo.getResult().getK8sKubeconfig());
		} catch (NullPointerException e) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0007", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		for (V1Namespace namespace : resultTem.getResult()) {
			list.add(namespace.getMetadata().getName());
		}
		result.setResult(list);
		return result;
	}

	/**
	 * 查询namespace下的资源
	 *
	 * @param clusterId
	 * @param namespace
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ResourceAO>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<ResourceAO> getResourceInNamespace(String serviceId, String clusterId, String namespace) throws ApiException,
			IOException {
		ExecuteResult<ResourceAO> result = new ExecuteResult<>();
		ResourceAO resourceAO = new ResourceAO();
		Cluster cluster = this.clusterDao.getClusterById(clusterId);

		List<V1ReplicationController> replicationControllers =
				this.clusterManager.getReplicationControllers(serviceId, cluster.getK8sKubeconfig(), namespace);
		List<V1Deployment> deployments = this.clusterManager.getDeployments(serviceId, cluster.getK8sKubeconfig(),
				namespace);
		List<V1StatefulSet> statefulSets = this.clusterManager.getStatefulsets(serviceId, cluster.getK8sKubeconfig(),
				namespace);
		List<V1Job> jobs = this.clusterManager.getJobs(serviceId, cluster.getK8sKubeconfig(), namespace);
		List<V1beta1CronJob> cronJobs = this.clusterManager.getCronJobs(serviceId, cluster.getK8sKubeconfig(),
				namespace);
		List<V1DaemonSet> daemonSets = this.clusterManager.getDaemonSets(serviceId, cluster.getK8sKubeconfig(),
				namespace);
		List<V1Pod> pods = this.clusterManager.getPods(serviceId, cluster.getK8sKubeconfig(), namespace);

		List<V1Service> services = this.clusterManager.getServices(serviceId, cluster.getK8sKubeconfig(), namespace);
		List<V1beta1Ingress> ingresses = this.clusterManager.getIngresses(serviceId, cluster.getK8sKubeconfig(),
				namespace);

		List<V1ConfigMap> configMaps = this.clusterManager.getConfigMaps(serviceId, cluster.getK8sKubeconfig(),
				namespace);
		List<V1Secret> secrets = this.clusterManager.getSecrets(serviceId, cluster.getK8sKubeconfig(), namespace);
		List<V1ServiceAccount> serviceAccounts = this.clusterManager.getServiceAccounts(serviceId,
				cluster.getK8sKubeconfig(),
				namespace);

		List<V1PersistentVolume> persistentVolumes =
				this.clusterManager.getPersistentVolumes(serviceId, cluster.getK8sKubeconfig(), namespace);
		List<V1PersistentVolumeClaim> persistentVolumeClaims =
				this.clusterManager.getPersistentVolumeClaims(serviceId, cluster.getK8sKubeconfig(), namespace);
		List<V1StorageClass> storageClasses = this.clusterManager.getStorageClasses(serviceId,
				cluster.getK8sKubeconfig(),
				namespace);

		List<Map<String, String>> controllerList = new ArrayList<>();
		for (V1ReplicationController controller : replicationControllers) {
			Map<String, String> controllerMap = new HashMap<>();
			controllerMap.put("name", controller.getMetadata().getName());
			controllerMap.put("namespace", controller.getMetadata().getNamespace());
			controllerList.add(controllerMap);
		}
		List<Map<String, String>> deploymentList = new ArrayList<>();
		for (V1Deployment deployment : deployments) {
			Map<String, String> deploymentMap = new HashMap<>();
			deploymentMap.put("name", deployment.getMetadata().getName());
			deploymentMap.put("namespace", deployment.getMetadata().getNamespace());
			deploymentList.add(deploymentMap);
		}
		List<Map<String, String>> statefulsetList = new ArrayList<>();
		for (V1StatefulSet statefulSet : statefulSets) {
			Map<String, String> statefulSetMap = new HashMap<>();
			statefulSetMap.put("name", statefulSet.getMetadata().getName());
			statefulSetMap.put("namespace", statefulSet.getMetadata().getNamespace());
			statefulsetList.add(statefulSetMap);
		}
		List<Map<String, String>> jobList = new ArrayList<>();
		for (V1Job job : jobs) {
			Map<String, String> jobMap = new HashMap<>();
			jobMap.put("name", job.getMetadata().getName());
			jobMap.put("namespace", job.getMetadata().getNamespace());
			jobList.add(jobMap);
		}
		List<Map<String, String>> cronList = new ArrayList<>();
		for (V1beta1CronJob cron : cronJobs) {
			Map<String, String> cronMap = new HashMap<>();
			cronMap.put("name", cron.getMetadata().getName());
			cronMap.put("namespace", cron.getMetadata().getNamespace());
			cronList.add(cronMap);
		}
		List<Map<String, String>> daemonSetList = new ArrayList<>();
		for (V1DaemonSet daemonSet : daemonSets) {
			Map<String, String> daemonSetMap = new HashMap<>();
			daemonSetMap.put("name", daemonSet.getMetadata().getName());
			daemonSetMap.put("namespace", daemonSet.getMetadata().getNamespace());
			daemonSetList.add(daemonSetMap);
		}
		List<Map<String, String>> podList = new ArrayList<>();
		for (V1Pod pod : pods) {
			Map<String, String> podMap = new HashMap<>();
			podMap.put("name", pod.getMetadata().getName());
			podMap.put("namespace", pod.getMetadata().getNamespace());
			podList.add(podMap);
		}
		List<Map<String, String>> serviceList = new ArrayList<>();
		for (V1Service service : services) {
			Map<String, String> serviceMap = new HashMap<>();
			serviceMap.put("name", service.getMetadata().getName());
			serviceMap.put("namespace", service.getMetadata().getNamespace());
			serviceList.add(serviceMap);
		}
		List<Map<String, String>> ingressList = new ArrayList<>();
		for (V1beta1Ingress ingress : ingresses) {
			Map<String, String> ingressMap = new HashMap<>();
			ingressMap.put("name", ingress.getMetadata().getName());
			ingressMap.put("namespace", ingress.getMetadata().getNamespace());
			ingressList.add(ingressMap);
		}
		List<Map<String, String>> configMapList = new ArrayList<>();
		for (V1ConfigMap configMap : configMaps) {
			Map<String, String> configMapMap = new HashMap<>();
			configMapMap.put("name", configMap.getMetadata().getName());
			configMapMap.put("namespace", configMap.getMetadata().getNamespace());
			configMapList.add(configMapMap);
		}
		List<Map<String, String>> secretList = new ArrayList<>();
		for (V1Secret secret : secrets) {
			Map<String, String> secretMap = new HashMap<>();
			secretMap.put("name", secret.getMetadata().getName());
			secretMap.put("namespace", secret.getMetadata().getNamespace());
			secretList.add(secretMap);
		}
		List<Map<String, String>> serviceAccountList = new ArrayList<>();
		for (V1ServiceAccount serviceAccount : serviceAccounts) {
			Map<String, String> serviceAccountMap = new HashMap<>();
			serviceAccountMap.put("name", serviceAccount.getMetadata().getName());
			serviceAccountMap.put("namespace", serviceAccount.getMetadata().getNamespace());
			serviceAccountList.add(serviceAccountMap);
		}
		List<Map<String, String>> persistentVolumeList = new ArrayList<>();
		for (V1PersistentVolume persistentVolume : persistentVolumes) {
			Map<String, String> persistentVolumeMap = new HashMap<>();
			persistentVolumeMap.put("name", persistentVolume.getMetadata().getName());
			persistentVolumeMap.put("namespace", persistentVolume.getMetadata().getNamespace());
			persistentVolumeList.add(persistentVolumeMap);
		}
		List<Map<String, String>> persistentVolumeClaimList = new ArrayList<>();
		for (V1PersistentVolumeClaim persistentVolumeClaim : persistentVolumeClaims) {
			Map<String, String> persistentVolumeclaimMap = new HashMap<>();
			persistentVolumeclaimMap.put("name", persistentVolumeClaim.getMetadata().getName());
			persistentVolumeclaimMap.put("namespace", persistentVolumeClaim.getMetadata().getNamespace());
			persistentVolumeClaimList.add(persistentVolumeclaimMap);
		}
		List<Map<String, String>> storageClassList = new ArrayList<>();
		for (V1StorageClass storageClass : storageClasses) {
			Map<String, String> storageClassMap = new HashMap<>();
			storageClassMap.put("name", storageClass.getMetadata().getName());
			storageClassMap.put("namespace", storageClass.getMetadata().getNamespace());
			storageClassList.add(storageClassMap);
		}
		resourceAO.setReplicationControllers(controllerList);
		resourceAO.setDeployments(deploymentList);
		resourceAO.setStatefulSets(statefulsetList);
		resourceAO.setJobs(jobList);
		resourceAO.setCronJobs(cronList);
		resourceAO.setDaemonSets(daemonSetList);
		resourceAO.setPods(podList);
		resourceAO.setServices(serviceList);
		resourceAO.setIngresses(ingressList);
		resourceAO.setConfigMaps(configMapList);
		resourceAO.setSecrets(secretList);
		resourceAO.setServiceAccounts(serviceAccountList);
		resourceAO.setPersistentVolumes(persistentVolumeList);
		resourceAO.setPersistentVolumeClaims(persistentVolumeClaimList);
		resourceAO.setStorageClasses(storageClassList);
		result.setResult(resourceAO);
		return result;
	}

	/**
	 * 查询集群下人员信息(不分页)
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.UserClusterRelationAO>>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "cluster", level = "R")
	public ExecuteResult<List<UserClusterRelationAO>> queryNoPagerClusterUserInfo(String clusterId) {
		ExecuteResult<List<UserClusterRelationAO>> result = new ExecuteResult<>();
		List<SqlRow> userList = userClusterRelationDao.queryNopagerUsersByClusterId(clusterId);
		List<UserClusterRelationAO> resultList = new ArrayList<UserClusterRelationAO>();
		for (SqlRow sqlRow : userList) {
			UserClusterRelationAO userClusterRelationAO = new UserClusterRelationAO();
			userClusterRelationAO.setId(sqlRow.getString("id"));
			userClusterRelationAO.setUserId(sqlRow.getString("user_id"));
			userClusterRelationAO.setUserName(sqlRow.getString("username"));
			userClusterRelationAO.setAccessLevel(sqlRow.getString("access_level"));
			userClusterRelationAO.setCreateAt(sqlRow.getTimestamp("create_at"));
			List<SqlRow> envUserList = userEnvRelationDao.queryNopagerUsersByClusterId(clusterId,
					sqlRow.getString("user_id"));
			List<UserEnvRelationAO> resultUserList = new ArrayList<UserEnvRelationAO>();
			for (SqlRow user : envUserList) {
				UserEnvRelationAO envUserAO = new UserEnvRelationAO();
				envUserAO.setId(user.getString("id"));
				envUserAO.setAccessLevel(user.getString("access_level"));
				envUserAO.setEnvId(user.getString("envid"));
				envUserAO.setClusterId(user.getString("clusterid"));
				envUserAO.setNamespace(user.getString("namespace"));
				resultUserList.add(envUserAO);
			}
			userClusterRelationAO.setEnvuser(resultUserList);
			resultList.add(userClusterRelationAO);
		}
		result.setResult(resultList);
		return result;
	}

	/**
	 * 查询要加入的人员信息
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<AddServiceGroupUserAO>> queryAddClusterUserInfo(String clusterId, String userName,
																			  String email) {
		ExecuteResult<List<AddServiceGroupUserAO>> result = new ExecuteResult<>();
		List<SqlRow> users = userDao.getUserByUserNameOrEmail(userName, email);
		List<AddServiceGroupUserAO> userAOList = new ArrayList<>();
		for (SqlRow user : users) {
			UserClusterRelation userClusterRelation = userClusterRelationDao.queryByClusterIdAndUserId(clusterId,
					user.getString("id")
			);
			AddServiceGroupUserAO userAO = new AddServiceGroupUserAO();
			userAO.setUserId(user.getString("id"));
			userAO.setUserName(user.getString("username"));
			if (userClusterRelation == null) {
				userAO.setAddflag(true);
			} else {
				userAO.setAddflag(false);
			}
			userAOList.add(userAO);
		}
		result.setResult(userAOList);
		return result;
	}

	/**
	 * 添加集群人员
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "cluster", level = "W")
	public ExecuteResult<String> addClusterRelation(String clusterId, UserClusterRelationAO userClusterRelationAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		UserClusterRelation userClusterRelationold =
				userClusterRelationDao.queryByClusterIdAndUserId(userClusterRelationAO.getClusterId(),
						userClusterRelationAO.getUserId());
		if (userClusterRelationold == null) {
			UserClusterRelation userClusterRelation = new UserClusterRelation();
			BeanUtils.copyProperties(userClusterRelationAO, userClusterRelation);
			userClusterRelation.setId(UUID.randomUUID().toString());
			userClusterRelation.setCreateAt(new Date());
			userClusterRelationDao.insert(userClusterRelation);
			String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
			result.setResult(message);
		} else {
			String message = messageUtils.matchMessage("M0008", new Object[]{}, Boolean.TRUE);
			result.setResult(message);
		}
		return result;
	}

	/**
	 * 查询集群邀请码
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> inviteUser(String clusterId) {
		ExecuteResult<String> result = new ExecuteResult<String>();
		Cluster cluster = clusterDao.getClusterById(clusterId);
		result.setResult(cluster.getInviteCode());
		return result;
	}

	/**
	 * 添加集群人员(通过邀请码)
	 *
	 * @param inviteCode
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> addClusterRelationByInviteCode(String inviteCode) {
		ExecuteResult<String> result = new ExecuteResult<>();
		Cluster cluster = userClusterRelationDao.queryClusterByInviteCode(inviteCode);
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		UserClusterRelation userClusterRelationold = userClusterRelationDao.queryByClusterIdAndUserId(cluster.getId(),
				user.getId());
		if (userClusterRelationold != null) {
			String message = messageUtils.matchMessage("M0008", new Object[]{}, Boolean.TRUE);
			result.setResult(message);
		} else {
			UserClusterRelation userClusterRelation = new UserClusterRelation();
			userClusterRelation.setId(UUID.randomUUID().toString());
			userClusterRelation.setCreateAt(new Date());
			userClusterRelation.setUserId(user.getId());
			userClusterRelation.setAccessLevel("R");
			userClusterRelation.setClusterId(cluster.getId());
			userClusterRelationDao.insert(userClusterRelation);
			String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
			result.setResult(message);
		}
		return result;
	}


	/**
	 * 添加集群人员(多个)
	 *
	 * @param userClusterRelationAOS
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "cluster", level = "W")
	public ExecuteResult<String> addClusterRelationList(String clusterId,
														List<UserClusterRelationAO> userClusterRelationAOS) {
		ExecuteResult<String> result = new ExecuteResult<>();
		for (UserClusterRelationAO userClusterRelationAO : userClusterRelationAOS) {
			UserClusterRelation userClusterRelation = new UserClusterRelation();
			BeanUtils.copyProperties(userClusterRelationAO, userClusterRelation);
			userClusterRelation.setId(UUID.randomUUID().toString());
			userClusterRelation.setCreateAt(new Date());
			userClusterRelationDao.insert(userClusterRelation);
		}
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 更新人员集群权限信息
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "cluster", level = "W")
	public ExecuteResult<String> updateClusterRelatio(String clusterId, UserClusterRelationAO userClusterRelationAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		String clusterRelationId = userClusterRelationAO.getId();
		if (clusterRelationId != null) {
			UserClusterRelation userClusterRelation =
					userClusterRelationDao.queryByClusterRelationId(clusterRelationId);
			userClusterRelation.setAccessLevel(userClusterRelationAO.getAccessLevel());
			userClusterRelationDao.update(userClusterRelation);
			result.setResult(userClusterRelation.getId());
		} else {
			UserClusterRelation userClusterRelation = new UserClusterRelation();
			userClusterRelation.setId(UUID.randomUUID().toString());
			userClusterRelation.setAccessLevel(userClusterRelationAO.getAccessLevel());
			userClusterRelation.setCreateAt(new Date());
			userClusterRelation.setUserId(userClusterRelationAO.getUserId());
			userClusterRelationDao.insert(userClusterRelation);
			result.setResult(userClusterRelation.getId());
		}
		return result;
	}


	/**
	 * 更新人员命名空间权限信息
	 *
	 * @param userEnvRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "cluster", level = "W")
	public ExecuteResult<String> updateEnvRelatio(String clusterId, UserEnvRelationAO userEnvRelationAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		String relationId = userEnvRelationAO.getId();
		if (relationId != null) {
			UserEnvRelation userEnvRelation = userEnvRelationDao.queryByEnvRelationId(relationId);
			userEnvRelation.setAccessLevel(userEnvRelationAO.getAccessLevel());
			userEnvRelationDao.update(userEnvRelation);
			result.setResult(userEnvRelation.getId());
		} else {
			UserEnvRelation userEnvRelation = new UserEnvRelation();
			userEnvRelation.setId(UUID.randomUUID().toString());
			userEnvRelation.setUserId(userEnvRelationAO.getUserId());
			userEnvRelation.setAccessLevel(userEnvRelationAO.getAccessLevel());
			userEnvRelation.setEnvId(userEnvRelationAO.getEnvId());
			userEnvRelation.setClusterId(userEnvRelationAO.getClusterId());
			userEnvRelation.setCreateAt(new Date());
			userEnvRelationDao.insert(userEnvRelation);
			result.setResult(userEnvRelation.getId());
		}
		return result;
	}


	/**
	 * 删除集群人员，及其在集群下所有的命名空间权限
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "cluster", level = "W")
	public ExecuteResult<String> deleteClusterRelation(String clusterId, UserClusterRelationAO userClusterRelationAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		UserClusterRelation userClusterRelation =
				userClusterRelationDao.queryByClusterRelationId(userClusterRelationAO.getId());
		//查询该集群下的命名空间
		List<Env> envList =
				userClusterRelationDao.queryEnvByClusterId(userClusterRelation.getClusterId());
		for (Env env : envList) {
			UserEnvRelation userEnvRelation =
					userEnvRelationDao.queryByEnvIdAndUserId(env.getId(), userClusterRelation.getUserId());
			if (userEnvRelation != null) {
				userEnvRelationDao.delete(userEnvRelation);
			}
		}
		userClusterRelationDao.delete(userClusterRelation);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 查询集群列表(集群和集群下的环境)根据当前登录人
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ClusterAO>>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<Cluster>> queryClusterByUser() {
		ExecuteResult<List<Cluster>> result = new ExecuteResult<List<Cluster>>();
		List<Cluster> list = new ArrayList<>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		String userId = user.getId();
		List<UserClusterRelation> relist = userClusterRelationDao.queryClusterRelationByUserId(userId);
		for (UserClusterRelation userClusterRelation : relist) {
			Cluster cluster = clusterDao.getClusterById(userClusterRelation.getClusterId());
			if (cluster != null) {
				list.add(cluster);
			}
		}
		result.setResult(list);
		return result;
	}

	/**
	 * 查询集群列表(集群和集群下的环境)分页
	 *
	 * @param currentPage, pageSize
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.ClusterAO>>
	 * @date 2020/2/10
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<Pager<ClusterAO>> getClusterAndEnvPager(int currentPage, int pageSize) {
		ExecuteResult<Pager<ClusterAO>> result = new ExecuteResult<Pager<ClusterAO>>();
		List<Cluster> clusterGroups = clusterDao.getClusterPager(currentPage, pageSize);
		int count = clusterDao.getclusterCount();
		Pager<ClusterAO> pager = new Pager<>();
		pager.setPageNum(currentPage);
		pager.setPageSize(pageSize);
		List<ClusterAO> clusterAOList = new ArrayList<ClusterAO>();
		for (Cluster cluster : clusterGroups) {
			ClusterAO clusterAO = new ClusterAO();
			BeanUtils.copyProperties(cluster, clusterAO);
			List<Env> envs = envDao.getEnvListByClusterId(cluster.getId());
			List<EnvAO> envAOS = new ListUtil<Env, EnvAO>().copyList(envs,
					EnvAO.class);
			clusterAO.setEnvs(envAOS);
			clusterAOList.add(clusterAO);
		}
		pager.setRecords(clusterAOList);
		pager.setTotalRecord(count);
		result.setResult(pager);
		return result;
	}

	/**
	 * 查询集群列表(集群和集群下的环境)分页
	 *
	 * @param currentPage, pageSize
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.ClusterAO>>
	 * @date 2020/2/10
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "adminquery")
	public ExecuteResult<Pager<SqlRow>> getClusterAndEnvPagerSqlrow(int currentPage, int pageSize, String orderparam) {
		ExecuteResult<Pager<SqlRow>> result = new ExecuteResult<Pager<SqlRow>>();
		List<SqlRow> clusters = clusterDao.getClusterPagerSqlrow(currentPage, pageSize, orderparam);
		int count = clusterDao.getclusterCountSqlrow();
		Pager<SqlRow> pager = new Pager<>();
		pager.setPageNum(currentPage);
		pager.setPageSize(pageSize);
		pager.setRecords(clusters);
		pager.setTotalRecord(count);
		result.setResult(pager);
		return result;
	}

	/**
	 * 获取用户-集群关系
	 *
	 * @param userId
	 * @param clusterId
	 * @return java.lang.String
	 * @author MddandPyy
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public String getUserClusterLevel(String userId, String clusterId) {
		String level = null;
		UserClusterRelation userClusterRelation = userClusterRelationDao.queryByClusterIdAndUserId(clusterId, userId);
		if (userClusterRelation != null) {
			level = userClusterRelation.getAccessLevel();
		}
		return level;
	}

	/**
	 * 获取用户-env关系
	 *
	 * @param userId
	 * @param envId
	 * @return java.lang.String
	 * @author MddandPyy
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public String getUserEnvLevel(String userId, String envId) {
		String level = null;
		UserEnvRelation userEnvRelation = userEnvRelationDao.queryByEnvIdAndUserId(envId, userId);
		if (userEnvRelation != null) {
			level = userEnvRelation.getAccessLevel();
		}
		return level;
	}

	/**
	 * 根据集群名查询集群信息（有权限校验）
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ClusterAO>
	 * @date 2020/3/16
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "cluster", level = "R")
	public ExecuteResult<ClusterAO> getOneClusterById(String clusterId) {
		ExecuteResult<ClusterAO> result = new ExecuteResult<ClusterAO>();
		Cluster cluster = clusterDao.getClusterById(clusterId);
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		UserClusterRelation userClusterRelation = userClusterRelationDao.queryByClusterIdAndUserId(clusterId,
				user.getId());
		ClusterAO clusterAO = new ClusterAO();
		BeanUtils.copyProperties(cluster, clusterAO);
		if (userClusterRelation != null) {
			clusterAO.setAccessLevel(userClusterRelation.getAccessLevel());
		}
		result.setResult(clusterAO);
		return result;
	}

	/**
	 * 删除集群,及其在集群下所有的命名空间,以及人员权限
	 *
	 * @param clusterId,clusterAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	@Override
	@Transactional
	@Authority(type = "cluster", level = "W")
	public ExecuteResult<String> deleteCluster(String clusterId, ClusterAO clusterAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		Cluster cluster = clusterDao.getClusterById(clusterId);
		if (cluster != null) {
			List<Env> envs = envDao.getEnvListByClusterId(clusterId);
			for (Env env : envs) {
				List<UserEnvRelation> userEnvRelations = userEnvRelationDao.queryByEnvId(env.getId());
				for (UserEnvRelation userEnvRelation : userEnvRelations) {
					userEnvRelationDao.delete(userEnvRelation);
				}
				env.setIsdel(true);
				envDao.updateEnv(env);
			}
			cluster.setIsdel(true);
			clusterDao.update(cluster);
		}
		List<UserClusterRelation> userClusterRelations =
				userClusterRelationDao.queryClusterRelationByClusterId(clusterId);
		for (UserClusterRelation userClusterRelation : userClusterRelations) {
			userClusterRelationDao.delete(userClusterRelation);
		}
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 当前登录人离开分组，无需校验权限
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@Override
	@Transactional
	public ExecuteResult<String> deleteClusterRelationBylogin(UserClusterRelationAO userClusterRelationAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		String clusterId = userClusterRelationAO.getClusterId();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		UserClusterRelation userClusterRelation =
				userClusterRelationDao.queryByClusterIdAndUserId(clusterId, user.getId());
		//查询该集群下的命名空间
		List<Env> envList =
				userClusterRelationDao.queryEnvByClusterId(userClusterRelation.getClusterId());
		for (Env env : envList) {
			UserEnvRelation userEnvRelation =
					userEnvRelationDao.queryByEnvIdAndUserId(env.getId(), userClusterRelation.getUserId());
			if (userEnvRelation != null) {
				userEnvRelationDao.delete(userEnvRelation);
			}
		}
		userClusterRelationDao.delete(userClusterRelation);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 查询cluster下的env上挂的service，用于删除集群前的校验
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceAO>
	 * @date 2020/3/18
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<ServiceAO>> getEnvService(String clusterId) {
		ExecuteResult<List<ServiceAO>> result = new ExecuteResult<List<ServiceAO>>();
		List<ServiceAO> serviceAOS = new ArrayList<>();
		List<Env> envs = envDao.getEnvListByClusterId(clusterId);
		for (Env env : envs) {
			List<com.ladeit.pojo.doo.Service> services = serviceDao.getServiceByEnvId(env.getId());
			for (com.ladeit.pojo.doo.Service service : services) {
				ServiceAO serviceAO = new ServiceAO();
				BeanUtils.copyProperties(service, serviceAO);
				ServiceGroup serviceGroup = serviceGroupDao.queryServiceById(service.getServiceGroupId());
				if (serviceGroup != null) {
					serviceAO.setServiceGroupName(serviceGroup.getName());
				}
				serviceAOS.add(serviceAO);
			}
		}
		result.setResult(serviceAOS);
		return result;
	}

	/**
	 * 更新集群下的命名空间
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-25
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> refreshNamespace(String clusterId) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		Cluster cluster = this.clusterDao.getClusterById(clusterId);
		List<Env> envs = this.envDao.getEnvListByClusterId(clusterId);
		ExecuteResult<List<V1Namespace>> namespaces = this.clusterManager.listNamespace(cluster.getK8sKubeconfig());
		if (envs == null || envs.isEmpty()) {
			for (V1Namespace namespace : namespaces.getResult()) {
				Env env = new Env();
				env.setId(namespace.getMetadata().getUid());
				env.setClusterId(clusterId);
				env.setNamespace(namespace.getMetadata().getName());
				env.setEnvName(namespace.getMetadata().getName());
				List<V1ResourceQuota> rqs = this.clusterManager.getResourceQuota(namespace.getMetadata().getName(),
						cluster.getK8sKubeconfig());
				for (V1ResourceQuota v1ResourceQuota : rqs) {
					String[] cpurequest = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("requests" +
							".cpu"), "cpu");
					String[] memrequest = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("requests" +
							".memory"), "mem");
					String[] cpulimit = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("limits.cpu"),
							"cpu");
					String[] memlimit =
							UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("limits.memory"), "mem");
					env.setCpuRequest(StringUtils.isNotBlank(cpurequest[0]) ? Integer.parseInt(cpurequest[0]) : null);
					env.setCpuRequestUnit(cpurequest[1]);
					env.setMemRequest(StringUtils.isNotBlank(memrequest[0]) ? Integer.parseInt(memrequest[0]) : null);
					env.setMemRequestUnit(memrequest[1]);
					env.setCpuLimit(StringUtils.isNotBlank(cpulimit[0]) ? Integer.parseInt(cpulimit[0]) : null);
					env.setCpuLimitUnit(cpulimit[1]);
					env.setMemLimit(StringUtils.isNotBlank(memlimit[0]) ? Integer.parseInt(memlimit[0]) : null);
					env.setMemLimitUnit(memlimit[1]);
					env.setResourceQuota(true);
				}
				this.envService.createEnv(env);
			}
		} else if (namespaces.getResult() == null || namespaces.getResult().isEmpty()) {
			for (Env env : envs) {
				this.envService.deleteEnv(env.getId(), null);
			}
		} else {
			List<String> envsOld =
					new ArrayList<>(envs.stream().map(e -> e.getNamespace()).collect(Collectors.toList()));
			List<String> namespacesOld =
					new ArrayList<>(namespaces.getResult().stream().map(m -> m.getMetadata().getName()).collect(Collectors.toList()));
			List<String> envsNew = new ArrayList<>(envsOld);
			List<String> namespacesNew = new ArrayList<>(namespacesOld);

			Map<String, Env> envMaps = envs.stream().collect(Collectors.toMap(Env::getNamespace, Function.identity()));
			Map<String, V1Namespace> namespaceMap = new HashMap<>();
			for (V1Namespace namespace : namespaces.getResult()) {
				namespaceMap.put(namespace.getMetadata().getName(), namespace);
			}
			// 取envs有namespaces没有的差集
			envsOld.removeAll(namespacesNew);
			// 取namespaces有envs没有的差集
			namespacesOld.removeAll(envsNew);
			// 取交集
			envsNew.retainAll(namespacesNew);
			// 针对k8s已经中删除的namespace，env做逻辑删除
			for (String name : envsOld) {
				this.envService.deleteEnv(envMaps.get(name).getId(), null);
			}
			// 针对k8s中新加的namespace，env做新增
			for (String name : namespacesOld) {
				V1Namespace namespace = namespaceMap.get(name);
				Env env = new Env();
				env.setId(namespace.getMetadata().getUid());
				env.setClusterId(clusterId);
				env.setNamespace(namespace.getMetadata().getName());
				env.setEnvName(namespace.getMetadata().getName());
				List<V1ResourceQuota> rqs = this.clusterManager.getResourceQuota(namespace.getMetadata().getName(),
						cluster.getK8sKubeconfig());
				for (V1ResourceQuota v1ResourceQuota : rqs) {
					String[] cpurequest = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("requests" +
							".cpu"), "cpu");
					String[] memrequest = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("requests" +
							".memory"), "mem");
					String[] cpulimit = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("limits.cpu"),
							"cpu");
					String[] memlimit =
							UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("limits.memory"), "mem");
					env.setCpuRequest(StringUtils.isNotBlank(cpurequest[0]) ? Integer.parseInt(cpurequest[0]) : null);
					env.setCpuRequestUnit(cpurequest[1]);
					env.setMemRequest(StringUtils.isNotBlank(memrequest[0]) ? Integer.parseInt(memrequest[0]) : null);
					env.setMemRequestUnit(memrequest[1]);
					env.setCpuLimit(StringUtils.isNotBlank(cpulimit[0]) ? Integer.parseInt(cpulimit[0]) : null);
					env.setCpuLimitUnit(cpulimit[1]);
					env.setMemLimit(StringUtils.isNotBlank(memlimit[0]) ? Integer.parseInt(memlimit[0]) : null);
					env.setMemLimitUnit(memlimit[1]);
					env.setResourceQuota(true);
				}
				this.envService.createEnvWithoutK8s(env);
			}
			// 针对k8s中仍然存在的，对env信息进行更新
			for (String name : envsNew) {
				V1Namespace namespace = namespaceMap.get(name);
				List<V1ResourceQuota> rqs = this.clusterManager.getResourceQuota(namespace.getMetadata().getName(),
						cluster.getK8sKubeconfig());
				Env env = new Env();
				env.setId(namespace.getMetadata().getUid());
				env.setClusterId(clusterId);
				env.setNamespace(namespace.getMetadata().getName());
				env.setEnvName(namespace.getMetadata().getName());
				for (V1ResourceQuota v1ResourceQuota : rqs) {
					String[] cpurequest = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("requests" +
							".cpu"), "cpu");
					String[] memrequest = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("requests" +
							".memory"), "mem");
					String[] cpulimit = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("limits.cpu"),
							"cpu");
					String[] memlimit =
							UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("limits.memory"), "mem");
					env.setCpuRequest(StringUtils.isNotBlank(cpurequest[0]) ? Integer.parseInt(cpurequest[0]) : null);
					env.setCpuRequestUnit(cpurequest[1]);
					env.setMemRequest(StringUtils.isNotBlank(memrequest[0]) ? Integer.parseInt(memrequest[0]) : null);
					env.setMemRequestUnit(memrequest[1]);
					env.setCpuLimit(StringUtils.isNotBlank(cpulimit[0]) ? Integer.parseInt(cpulimit[0]) : null);
					env.setCpuLimitUnit(cpulimit[1]);
					env.setMemLimit(StringUtils.isNotBlank(memlimit[0]) ? Integer.parseInt(memlimit[0]) : null);
					env.setMemLimitUnit(memlimit[1]);
					env.setResourceQuota(true);
				}
				this.envService.updateEnvQuota(env);
			}
		}
		return result;
	}

	/**
	 * webkubectl
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-27
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> webkubectl(String clusterId) throws IOException {
		ExecuteResult<String> result = new ExecuteResult<>();
		Cluster cluster = this.clusterDao.getClusterById(clusterId);
		String webKubectlHost = System.getenv().get("LADEIT_WEBKUBECTL_HOST");
		String configBase64 = Base64.getEncoder().encodeToString(cluster.getK8sKubeconfig().getBytes());
		String json = "{\"name\":\"" + cluster.getK8sName() + "\",\"kubeConfig\":\"" + configBase64 + "\"}";
		RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
		Request request =
				new Request.Builder().url(webKubectlHost+"/api/kube-config").post(requestBody).build();
		Response response = this.okHttpClient.newCall(request).execute();
		String token = JSONObject.parseObject(new String(response.body().bytes())).getString("token");
		result.setResult(webKubectlHost + "/terminal/?token=" + token);
		return result;
	}

	private Object[] resolveArr(Object[] source) throws InvocationTargetException, IllegalAccessException {
		Object[] target = new Object[source.length];
		for (int i = 0; i < source.length; i++) {
			if (source[i] == null) {
				break;
			}
			Object newObj = this.convertResource(source[i]);
			target[i] = newObj;
		}
		return target;
	}

	private Object convertResource(Object source) throws InvocationTargetException,
			IllegalAccessException {
		Method[] methods = source.getClass().getMethods();
		Map<String, Object> map = new HashMap<>();
		;
		for (int i = 0; i < methods.length; i++) {
			boolean isGet = methods[i].getName().startsWith("get");
			if (isGet) {
				String name = methods[i].getName().substring(3, methods[i].getName().length());
				if ("Status".equals(name)) {
					continue;
				}
				if (!methods[i].getReturnType().getName().contains("io.kubernetes.client")) {
					Object object = methods[i].invoke(source);
					map.put(name, object);
					continue;
				}
				if (methods[i].getReturnType().getName().contains("IntOrString")) {
					System.out.println("IntOrString");
					System.out.println(methods[i].getName());
					IntOrString objects = (IntOrString) methods[i].invoke(source);
					if (objects.isInteger()) {
						map.put(name, objects.getIntValue());
					} else {
						map.put(name, objects.getStrValue());
					}
					continue;
				}
				Object objects = methods[i].invoke(source);
				if (objects != null && !(objects instanceof ArrayList)) {
					// 如果反射对象是普通的map
					Object target = this.convertResource(objects);
					map.put(name, target);
				} else if (objects != null && objects instanceof ArrayList) {
					// 如果反射对象是普通的map类型的数组
					List<Object> objs = (List<Object>) objects;
					List<Object> list = new ArrayList<>();
					for (int k = 0; k < objs.size(); k++) {
						Object target = this.convertResource(objs.get(k));
						list.add(target);
					}
					map.put(name, list);
				}
			}
		}
		return map;
	}
}

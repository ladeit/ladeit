package com.ladeit.biz.services.impl;

import com.ladeit.biz.annotation.Authority;
import com.ladeit.biz.dao.EnvDao;
import com.ladeit.biz.dao.ServiceDao;
import com.ladeit.biz.dao.ServiceGroupDao;
import com.ladeit.biz.dao.UserEnvRelationDao;
import com.ladeit.biz.manager.K8sClusterManager;
import com.ladeit.biz.runner.events.EventHandler;
import com.ladeit.biz.services.ClusterService;
import com.ladeit.biz.services.EnvService;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.EnvAO;
import com.ladeit.pojo.ao.ServiceAO;
import com.ladeit.pojo.doo.*;
import com.ladeit.util.ListUtil;
import com.ladeit.util.k8s.UnitUtil;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1ResourceQuota;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: ladeit
 * @description: EnvServiceImpl
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
@Service
@Slf4j
public class EnvServiceImpl implements EnvService {

	@Autowired
	private EnvDao envDao;
	@Autowired
	private ClusterService k8sClusterService;
	@Autowired
	private K8sClusterManager clusterManager;
	@Autowired
	private UserEnvRelationDao userEnvRelationDao;
	@Autowired
	private ServiceDao serviceDao;
	@Autowired
	private ServiceGroupDao serviceGroupDao;
	@Autowired
	private EventHandler eventHandler;
	@Autowired
	private MessageUtils messageUtils;

	/**
	 * 根据id得到env
	 *
	 * @param id
	 * @return com.ladeit.pojo.doo.Env
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@Override
	public Env getEnvById(String id) {
		return this.envDao.getEnvById(id);
	}

	/**
	 * 创建env
	 *
	 * @param env
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@Override
	@Transactional
	public ExecuteResult<String> createEnv(Env env) throws IOException, ApiException {
		ExecuteResult<String> result = this.createEnvWithoutK8s(env);
		V1Namespace namespace = new V1Namespace();
		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setName(env.getNamespace());
		namespace.setMetadata(meta);
		Cluster cluster = this.k8sClusterService.getClusterById(env.getClusterId());
		this.clusterManager.createNamespace(namespace, cluster.getK8sKubeconfig());
		this.eventHandler.put(env.getId(), null);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	* 创建一个env不创建k8s资源
	* @author falcomlife
	* @date 20-5-26
	* @version 1.0.0
	* @return com.ladeit.common.ExecuteResult<java.lang.String>
	* @param env
	*/
	@Override
	@Transactional
	public ExecuteResult<String> createEnvWithoutK8s(Env env) throws ApiException, IOException {
		ExecuteResult<String> result = new ExecuteResult<>();
		Env k8sEnvDo = this.envDao.getEnvByClusterAndNamespace(env.getClusterId(),
				env.getNamespace());
		if (k8sEnvDo != null) {
			result.setCode(Code.ALREADY_USED);
			String message = messageUtils.matchMessage("M0009", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		env.setCreateAt(new Date());
		env.setCreateBy(user.getUsername());
		env.setCreateById(user.getId());
		env.setIsdel(false);
		this.envDao.createEnv(env);
		UserEnvRelation userEnvRelation = new UserEnvRelation();
		userEnvRelation.setId(UUID.randomUUID().toString());
		userEnvRelation.setEnvId(env.getId());
		userEnvRelation.setAccessLevel("R,W,X");
		userEnvRelation.setUserId(user.getId());
		userEnvRelation.setClusterId(env.getClusterId());
		userEnvRelation.setCreateAt(new Date());
		userEnvRelationDao.insert(userEnvRelation);
		this.eventHandler.put(env.getId(), null);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 更新env信息
	 *
	 * @param envid
	 * @param envBO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	@Authority(type = "env", level = "W")
	public ExecuteResult<String> updateEnv(String envid, Env envBO) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		String envId = envBO.getId();
		Env env = envDao.getEnvById(envId);
		env.setEnvName(envBO.getEnvName());
		env.setNamespace(envBO.getNamespace());
		env.setAudit(envBO.getAudit());
		env.setResourceQuota(envBO.getResourceQuota());
		env.setCpuLimit(envBO.getCpuLimit());
		env.setCpuLimitUnit(envBO.getCpuLimitUnit());
		env.setMemLimit(envBO.getMemLimit());
		env.setMemLimitUnit(envBO.getMemLimitUnit());
		env.setCpuRequest(envBO.getCpuRequest());
		env.setCpuRequestUnit(envBO.getCpuRequestUnit());
		env.setMemRequest(envBO.getMemRequest());
		env.setMemRequestUnit(envBO.getMemRequestUnit());
		env.setNetworkLimit(envBO.getNetworkLimit());
		env.setModifyAt(new Date());
		env.setModifyBy(user.getUsername());
		this.envDao.updateEnv(env);
		Cluster cluster = this.k8sClusterService.getClusterById(env.getClusterId());
		this.applyResourceQuota(env, cluster.getK8sKubeconfig());
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 创建资源配额
	 *
	 * @param env
	 * @param config
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	private ExecuteResult<String> applyResourceQuota(Env env, String config) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		String mrUnit = env.getMemRequestUnit();
		String mlUnit = env.getMemLimitUnit();
		String clUnit = env.getCpuLimitUnit();
		String crUnit = env.getCpuRequestUnit();
		Integer mr = env.getMemRequest();
		Integer ml = env.getMemLimit();
		Integer cr = env.getCpuRequest();
		Integer cl = env.getCpuLimit();
		if (env.getResourceQuota() != null && env.getResourceQuota()) {
			// 开启资源配额
			List<V1ResourceQuota> resourceQuotas = this.clusterManager.getResourceQuota(env.getNamespace(), config);
			if (resourceQuotas != null && resourceQuotas.size() != 0) {
				// 如果有，根据情况进行判断
				for (V1ResourceQuota rq : resourceQuotas) {
					// TODO 这里暂时没有加限制，原来的想法是根据ns已经使用的资源情况进行校验，如果新的资源配置比已经使用的低了就不行，但是k8s这块源生没有限制，所以也没有加。
					this.clusterManager.createResourceQuota(env.getNamespace(), UnitUtil.unitConverter(clUnit, cl, 1),
							UnitUtil.unitConverter(mlUnit, ml, 2),
							UnitUtil.unitConverter(crUnit, cr, 1), UnitUtil.unitConverter(mrUnit, mr, 2), config);
				}
			} else {
				// 如果没有，直接创建
				this.clusterManager.createResourceQuota(env.getNamespace(), UnitUtil.unitConverter(clUnit, cl, 1),
						UnitUtil.unitConverter(mlUnit, ml, 2),
						UnitUtil.unitConverter(crUnit, cr, 1), UnitUtil.unitConverter(mrUnit, mr, 2), config);
			}
		} else {
			// 没开启资源配额，就查看是否已经创建了配额限制，如果没做，就不创建，如果做了，就删除配额限制
			List<V1ResourceQuota> rqs = this.clusterManager.getResourceQuota(env.getNamespace(), config);
			if (!rqs.isEmpty()) {
				// 存在resourcequota，进行删除
				for (V1ResourceQuota rq : rqs) {
					this.clusterManager.deleteResourceQuota(rq.getMetadata().getName(), env.getNamespace(), config);
				}
			}
		}
		return result;
	}

	/**
	 * 删除env
	 *
	 * @param bzK8sEnvAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "env", level = "W")
	@Transactional
	public ExecuteResult<String> deleteEnv(String envId, EnvAO bzK8sEnvAO) throws IOException {
		ExecuteResult<String> result = new ExecuteResult<>();
		Env env = envDao.getEnvById(envId);
		if (env != null) {
			env.setIsdel(true);
			envDao.updateEnv(env);
		}
		this.eventHandler.remove(envId);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 查询env
	 *
	 * @param pager
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.doo.Env>>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<Pager<Env>> getEnvPage(Pager<Env> pager) {
		ExecuteResult<Pager<Env>> result = new ExecuteResult<>();
		Pager<Env> pagerbo = new Pager<>();
		List<Env> list = this.envDao.getEnv(pager.getPageNum(), pager.getPageSize());
		int count = this.envDao.getEnvCount(pager.getPageNum(), pager.getPageSize());
		List<Env> listbo = new ListUtil<Env, Env>().copyList(list, Env.class);
		for (Env bzK8sEnvBO : listbo) {
			Cluster bzK8sClusterBO = new Cluster();
			bzK8sClusterBO.setId(bzK8sEnvBO.getClusterId());
			ExecuteResult<Cluster> k8sClusterBo = this.k8sClusterService.getOneCluster(bzK8sClusterBO);
			bzK8sEnvBO.setEnvName(k8sClusterBo.getResult().getK8sName());
		}
		pagerbo.setRecords(listbo);
		pagerbo.setPageNum(pager.getPageNum());
		pagerbo.setPageSize(pager.getPageSize());
		pagerbo.setTotalRecord(count);
		result.setResult(pagerbo);
		return result;
	}

	/**
	 * 查询envlist
	 *
	 * @param env
	 * @author falcomlife
	 * @date 19-9-27
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<Env>> getEnvList(Env env) {
		ExecuteResult<List<Env>> result = new ExecuteResult<>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		// 先查询所有的公共env
		List<Env> envlist = this.envDao.getEnvList(env);
		List<Env> listVisibility = new ArrayList<>();
		for (Env e : envlist) {
			if ("admin".equals(user.getUsername())) {
				listVisibility.add(e);
			} else {
				UserEnvRelation userEnvRelation = userEnvRelationDao.queryByEnvIdAndUserId(e.getId(), user.getId());
				if (userEnvRelation != null && userEnvRelation.getAccessLevel().contains("R")) {
					listVisibility.add(e);
				}
			}
		}
		List<Env> listCombine = new ArrayList<>();
		listCombine.addAll(listVisibility);
		List<Env> listCombineBo = new ListUtil<Env, Env>().copyList(listCombine,
				Env.class);
		List<Env> listcombine =
				listCombineBo.stream().distinct().sorted(Comparator.comparing(Env::getCreateAt)).collect(Collectors.toList());
		result.setResult(listcombine);
		return result;
	}

	/**
	 * 获取所有的env
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.doo.Env>>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<Env>> getAllEnv() {
		ExecuteResult<List<Env>> result = new ExecuteResult<>();
		List<Env> envs = this.envDao.getAllEnv();
		result.setResult(envs);
		return result;
	}

	/**
	 * 查看namespace是否支持istio
	 *
	 * @param clusterId
	 * @param envId
	 * @return com.ladeit.common.ExecuteResult<java.lang.Integer>
	 * @author falcomlife
	 * @date 20-3-12
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> namespaceType(String clusterId, String envId) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		Cluster cluster = this.k8sClusterService.getClusterById(clusterId);
		Env env = this.envDao.getEnvById(envId);
		V1Namespace namespaceObj = null;
		try {
			namespaceObj = this.clusterManager.getNamespace(cluster.getK8sKubeconfig(), env.getNamespace());
		} catch (NoSuchElementException e) {
			// 没找到namespace
			log.error(e.getMessage(), e);
			result.setCode(Code.NOTFOUND);
			return result;
		}
		boolean flag =
				namespaceObj.getMetadata().getLabels() != null && namespaceObj.getMetadata().getLabels().containsKey(
						"istio-injection") && namespaceObj.getMetadata().getLabels().get("istio-injection").equals(
						"enabled");
		if (flag) {
			result.setResult("1");
		} else {
			result.setResult("0");
		}
		return result;
	}

	/**
	 * 查询服务信息（有权限校验）
	 *
	 * @param envId,clusterId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ClusterAO>
	 * @date 2020/3/16
	 * @ahthor MddandPyy
	 */
	@Override
	@Authority(type = "env", level = "R")
	public ExecuteResult<EnvAO> getEnvByEnvAndClusterId(String envId, String clusterId) {
		ExecuteResult<EnvAO> result = new ExecuteResult<>();
		Env env = envDao.getEnvByClusterAndEnvId(clusterId, envId);
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		UserEnvRelation userEnvRelation = userEnvRelationDao.queryByEnvIdAndUserId(env.getId(), user.getId());
		EnvAO envAO = new EnvAO();
		BeanUtils.copyProperties(env, envAO);
		if (userEnvRelation != null) {
			envAO.setAccessLevel(userEnvRelation.getAccessLevel());
		}
		result.setResult(envAO);
		return result;
	}

	/**
	 * 查询env上挂的service，用于删除集群前的校验
	 *
	 * @param envId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceAO>
	 * @date 2020/3/18
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<List<ServiceAO>> getEnvService(String envId) {
		ExecuteResult<List<ServiceAO>> result = new ExecuteResult<List<ServiceAO>>();
		List<ServiceAO> serviceAOS = new ArrayList<>();
		Env env = envDao.getEnvById(envId);
		if (env != null) {
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
	 * 更新env，同步刷新的时候调用，主要更新配额
	 *
	 * @param env
	 * @return void
	 * @author falcomlife
	 * @date 20-5-25
	 * @version 1.0.0
	 */
	@Override
	public void updateEnvQuota(Env env) {
		this.envDao.updateEnvQuota(env);
	}
}

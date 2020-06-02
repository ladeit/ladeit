package com.ladeit.biz.services.impl;

import com.ladeit.biz.annotation.Authority;
import com.ladeit.biz.dao.EnvDao;
import com.ladeit.biz.dao.ServiceDao;
import com.ladeit.biz.dao.ServiceGroupDao;
import com.ladeit.biz.dao.UserEnvRelationDao;
import com.ladeit.biz.manager.K8sClusterManager;
import com.ladeit.biz.manager.K8sWorkLoadsManager;
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
import com.ladeit.pojo.dto.metric.pod.Occupy;
import com.ladeit.pojo.dto.metric.pod.PodMetric;
import com.ladeit.util.ListUtil;
import com.ladeit.util.k8s.MetricApi;
import com.ladeit.util.k8s.UnitUtil;
import io.fabric8.kubernetes.api.model.Container;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
	@Autowired
	private MetricApi metricApi;
	@Autowired
	private K8sWorkLoadsManager k8sWorkLoadsManager;
	@Autowired
	private K8sClusterManager k8sClusterManager;

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
		V1Namespace namespace = new V1Namespace();
		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setName(env.getNamespace());
		namespace.setMetadata(meta);
		Cluster cluster = this.k8sClusterService.getClusterById(env.getClusterId());
		V1Namespace v1Namespace = this.clusterManager.createNamespace(namespace, cluster.getK8sKubeconfig());
		env.setId(v1Namespace.getMetadata().getUid());
		ExecuteResult<String> result = this.createEnvWithoutK8s(env);
		this.eventHandler.put(env.getId(), null);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	/**
	 * 创建一个env不创建k8s资源
	 *
	 * @param env
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-26
	 * @version 1.0.0
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
	public ExecuteResult<String> deleteEnv(String envId, EnvAO bzK8sEnvAO) throws IOException, ApiException {
		ExecuteResult<String> result = new ExecuteResult<>();
		Env env = envDao.getEnvById(envId);
		Cluster cluster = this.k8sClusterService.getClusterById(env.getClusterId());
		if (env != null) {
			env.setIsdel(true);
			envDao.updateEnv(env);
			this.k8sClusterManager.deleteNamespace(cluster.getK8sKubeconfig(), env.getNamespace());
		}
		this.eventHandler.remove(envId);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);
		return result;
	}

	@Override
	public ExecuteResult<String> deleteEnvIgnoreK8s(String envId, EnvAO bzK8sEnvAO) throws IOException {
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
	public ExecuteResult<List<Env>> getEnvList(Env env, String config) throws IOException, ApiException {
		ExecuteResult<List<Env>> result = new ExecuteResult<>();
		BigDecimal bigZero = new BigDecimal(0);
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
		// 对整合好的env数据按照时间进行排序
		List<Env> listcombine =
				listCombineBo.stream().distinct().sorted(Comparator.comparing(Env::getCreateAt)).collect(Collectors.toList());
		List<V1ResourceQuota> resourceQuotas = this.k8sClusterManager.getAllResourceQuota(config);
		for (Env envRes : listcombine) {
			// 获取每个namespace中的pod
			ExecuteResult<List<V1Pod>> podRes = this.k8sWorkLoadsManager.getPodsInNamespace(envRes.getNamespace(),
					config);
			// 初始化占用资源对象
			List<Occupy> occupiesCpuReq = new ArrayList<>();
			List<Occupy> occupiesMemReq = new ArrayList<>();
			List<Occupy> occupiesCpuLimit = new ArrayList<>();
			List<Occupy> occupiesMemLimit = new ArrayList<>();
			envRes.setOccupyCpuReq(occupiesCpuReq);
			envRes.setOccupyMemReq(occupiesMemReq);
			envRes.setOccupyCpuLimit(occupiesCpuLimit);
			envRes.setOccupyMemLimit(occupiesMemLimit);
			// 初始化占用资源数量的值
//			BigDecimal cpuReqSum = new BigDecimal(0);
//			BigDecimal memReqSum = new BigDecimal(0);
//			BigDecimal cpuLimitSum = new BigDecimal(0);
//			BigDecimal memLimitSum = new BigDecimal(0);
			// 初始化命名空间所拥有的资源对象
			BigDecimal namespaceCpuRequest = new BigDecimal(0);
			BigDecimal namespaceMemRequest = new BigDecimal(0);
			BigDecimal namespaceCpuLimit = new BigDecimal(0);
			BigDecimal namespaceMemLimit = new BigDecimal(0);
			Map<String, Long> mapResult = new HashMap<>();
			if (podRes.getResult() != null && !podRes.getResult().isEmpty()) {
				mapResult =
						podRes.getResult().stream().map(pod -> pod.getStatus()).collect(Collectors.groupingBy(V1PodStatus::getPhase, Collectors.counting()));
				Long all = mapResult.values().stream().reduce((i, j) -> i + j).get();
				mapResult.put("SUM", all);
				for (V1Pod pod : podRes.getResult()) {
					// 获取每个pod占用的资源，封装成对象
					Occupy occupyCpuReq = new Occupy();
					Occupy occupyMemReq = new Occupy();
					Occupy occupyCpuLimit = new Occupy();
					Occupy occupyMemLimit = new Occupy();
					BigDecimal cpuRequest = new BigDecimal(0);
					BigDecimal memRequest = new BigDecimal(0);
					BigDecimal cpuLimit = new BigDecimal(0);
					BigDecimal memLimit = new BigDecimal(0);
					for (V1Container v1Container : pod.getSpec().getContainers()) {
						// 获取没给container占用的资源，累加在一起就是pod占用的资源
						Map<String, Quantity> reqMap = v1Container.getResources().getRequests();
						Map<String, Quantity> limitMap = v1Container.getResources().getLimits();
						if (((reqMap != null && (reqMap.get("cpu") == null && reqMap.get("memory") == null)) && (limitMap != null && (limitMap.get("cpu") == null && limitMap.get("memory") == null))) || (reqMap == null || limitMap == null)) {
							continue;
						}
						BigDecimal cpur = UnitUtil.quantityToNum(reqMap.get("cpu"), "cpu");
						BigDecimal memr = UnitUtil.quantityToNum(reqMap.get("memory"), "mem");
						BigDecimal cpul = UnitUtil.quantityToNum(limitMap.get("cpu"), "cpu");
						BigDecimal meml = UnitUtil.quantityToNum(limitMap.get("memory"), "mem");
						// 累加每个pod中每个container的资源占用值
						cpuRequest = cpuRequest.add(cpur == null ? bigZero : cpur);
						memRequest = memRequest.add(memr == null ? bigZero : memr);
						cpuLimit = cpuLimit.add(cpul == null ? bigZero : cpul);
						memLimit = memLimit.add(meml == null ? bigZero : meml);
					}
					// 累加所有pod的资源占用值
//					cpuReqSum = cpuReqSum.add(cpuRequest);
//					memReqSum = memReqSum.add(memRequest);
//					cpuLimitSum = cpuLimitSum.add(cpuLimit);
//					memLimitSum = memLimitSum.add(memLimit);
					// 定义一个资源配额标记
					boolean resourcequota = false;
					// 查找到每个namespace的资源定义文件，获取namespace的资源值
					if (resourceQuotas != null && !resourceQuotas.isEmpty()) {
						for (V1ResourceQuota v1ResourceQuota : resourceQuotas) {
							if (v1ResourceQuota.getMetadata().getNamespace().equals(envRes.getNamespace())) {
								// 如果命名空间里有资源配额对象，打开标记开关
								resourcequota = true;
								namespaceCpuRequest = UnitUtil.quantityToNum(v1ResourceQuota.getSpec().getHard().get(
										"requests.cpu"), "cpu");
								namespaceMemRequest = UnitUtil.quantityToNum(v1ResourceQuota.getSpec().getHard().get(
										"requests.memory"), "mem");
								namespaceCpuLimit = UnitUtil.quantityToNum(v1ResourceQuota.getSpec().getHard().get(
										"limits.cpu"), "cpu");
								namespaceMemLimit = UnitUtil.quantityToNum(v1ResourceQuota.getSpec().getHard().get(
										"limits.memory"), "mem");
								envRes.setCpuRequest(namespaceCpuRequest.intValue());
								envRes.setCpuRequestUnit("m");
								envRes.setMemRequest(namespaceMemRequest.intValue());
								envRes.setMemRequestUnit("m");
								envRes.setCpuLimit(namespaceCpuLimit.intValue());
								envRes.setCpuLimitUnit("m");
								envRes.setMemLimit(namespaceMemLimit.intValue());
								envRes.setMemLimitUnit("m");
							}
						}
					}
					if (resourcequota) {
						// 资源占用对象的最终封装
						occupyCpuReq.setName(pod.getMetadata().getName());
						occupyMemReq.setName(pod.getMetadata().getName());
						occupyCpuLimit.setName(pod.getMetadata().getName());
						occupyMemLimit.setName(pod.getMetadata().getName());
						occupyCpuReq.setNum(cpuRequest.longValue());
						occupyMemReq.setNum(memRequest.longValue());
						occupyCpuLimit.setNum(cpuLimit.longValue());
						occupyMemLimit.setNum(memLimit.longValue());
						occupyCpuReq.setPercentage(namespaceCpuRequest.equals(bigZero) ? 0 :
								cpuRequest.divide(namespaceCpuRequest, 3, RoundingMode.HALF_UP).doubleValue());
						occupyMemReq.setPercentage(namespaceMemRequest.equals(bigZero) ? 0 :
								memRequest.divide(namespaceMemRequest, 3, RoundingMode.HALF_UP).doubleValue());
						occupyCpuLimit.setPercentage(namespaceCpuLimit.equals(bigZero) ? 0 :
								cpuLimit.divide(namespaceCpuLimit, 3, RoundingMode.HALF_UP).doubleValue());
						occupyMemLimit.setPercentage(namespaceMemLimit.equals(bigZero) ? 0 :
								memLimit.divide(namespaceMemLimit, 3, RoundingMode.HALF_UP).doubleValue());
						occupyCpuReq.setEnvId(envRes.getId());
						occupyMemReq.setEnvId(envRes.getId());
						occupyCpuLimit.setEnvId(envRes.getId());
						occupyMemLimit.setEnvId(envRes.getId());
						// 放入资源占用列表中
						occupiesCpuReq.add(occupyCpuReq);
						occupiesMemReq.add(occupyMemReq);
						occupiesCpuLimit.add(occupyCpuLimit);
						occupiesMemLimit.add(occupyMemLimit);
					}
				}
			} else {
				mapResult.put("SUM", 0L);
			}
			envRes.setPodCount(mapResult);
		}
		result.setResult(listcombine);
		return result;
	}

	/**
	 * 给env添加资源属性
	 *
	 * @param list
	 * @param config
	 * @return void
	 * @author falcomlife
	 * @date 20-5-30
	 * @version 1.0.0
	 */
	private List<Env> getResourceQuotaInNamespace(List<Env> list, String config) {
		for (Env e : list) {
			List<V1ResourceQuota> rqs = null;
			try {
				rqs = this.clusterManager.getResourceQuota(e.getNamespace(),
						config);
			} catch (ApiException ex) {
				log.info(ex.getMessage(), ex);
			}
			if (rqs != null && !rqs.isEmpty()) {
				for (V1ResourceQuota v1ResourceQuota : rqs) {
					String[] cpurequest = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("requests" +
							".cpu"), "cpu");
					String[] memrequest = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("requests" +
							".memory"), "mem");
					String[] cpulimit = UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("limits.cpu"),
							"cpu");
					String[] memlimit =
							UnitUtil.unitConverter(v1ResourceQuota.getSpec().getHard().get("limits.memory"), "mem");
					e.setCpuRequest(StringUtils.isNotBlank(cpurequest[0]) ? Integer.parseInt(cpurequest[0]) : null);
					e.setCpuRequestUnit(cpurequest[1]);
					e.setMemRequest(StringUtils.isNotBlank(memrequest[0]) ? Integer.parseInt(memrequest[0]) : null);
					e.setMemRequestUnit(memrequest[1]);
					e.setCpuLimit(StringUtils.isNotBlank(cpulimit[0]) ? Integer.parseInt(cpulimit[0]) : null);
					e.setCpuLimitUnit(cpulimit[1]);
					e.setMemLimit(StringUtils.isNotBlank(memlimit[0]) ? Integer.parseInt(memlimit[0]) : null);
					e.setMemLimitUnit(memlimit[1]);
				}
				e.setResourceQuota(true);
			} else {
				e.setResourceQuota(false);
			}
		}
		return list;
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
		Cluster cluster = this.k8sClusterService.getClusterById(clusterId);
		UserEnvRelation userEnvRelation = userEnvRelationDao.queryByEnvIdAndUserId(env.getId(), user.getId());
		ExecuteResult<List<V1Namespace>> namespaces = this.clusterManager.listNamespace(cluster.getK8sKubeconfig());
		List<V1ResourceQuota> rqs = null;
		try {
			rqs = this.clusterManager.getResourceQuota(env.getNamespace(),
					cluster.getK8sKubeconfig());
		} catch (ApiException e) {
			log.info(e.getMessage(), e);
		}
		if (rqs != null && !rqs.isEmpty()) {
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
		} else {
			env.setResourceQuota(false);
		}
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

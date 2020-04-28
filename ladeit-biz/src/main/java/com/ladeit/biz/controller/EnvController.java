package com.ladeit.biz.controller;

import com.ladeit.biz.annotation.Authority;
import com.ladeit.biz.dao.ClusterDao;
import com.ladeit.biz.dao.EnvDao;
import com.ladeit.biz.services.EnvService;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.ClusterAO;
import com.ladeit.pojo.ao.EnvAO;
import com.ladeit.pojo.ao.ServiceAO;
import com.ladeit.pojo.doo.Cluster;
import com.ladeit.pojo.doo.Env;
import com.ladeit.util.ListUtil;
import io.kubernetes.client.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @program: ladeit
 * @description: K8sEnvController
 * @author: falcomlife
 * @create: 2019/09/19
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/api/${api.version}/env")
@Api(description = "环境操作", tags = "/api/v1/", hidden = true)
public class EnvController {

	@Autowired
	private EnvService k8sEnvService;

	@Autowired
	private ClusterDao clusterDao;

	@Autowired
	private EnvDao envDao;

	@Autowired
	private MessageUtils messageUtils;

	/**
	 * 创建env
	 *
	 * @param bzK8sEnvAO
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@ApiOperation(value = "创建环境")
	@PostMapping("")
	public ExecuteResult<String> createEnv(@RequestBody EnvAO bzK8sEnvAO) throws IOException, ApiException {
		Env bzK8sEnvBO = new Env();
		BeanUtils.copyProperties(bzK8sEnvAO, bzK8sEnvBO);
		return this.k8sEnvService.createEnv(bzK8sEnvBO);
	}

	/**
	 * 更新env
	 * @param bzK8sEnvAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@ApiOperation(value = "更新环境")
	@PutMapping("")
	public ExecuteResult<String> updateEnv(@RequestBody EnvAO bzK8sEnvAO) throws IOException, ApiException {
		Env bzK8sEnvBO = new Env();
		BeanUtils.copyProperties(bzK8sEnvAO, bzK8sEnvBO);
		return this.k8sEnvService.updateEnv(bzK8sEnvBO.getId(),bzK8sEnvBO);
	}

	/**
	 * 删除env
	 * @param bzK8sEnvAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	@DeleteMapping("")
	public ExecuteResult<String> deleteEnv(@RequestBody EnvAO bzK8sEnvAO) throws IOException {
		return this.k8sEnvService.deleteEnv(bzK8sEnvAO.getId(),bzK8sEnvAO);
	}

	/**
	 * 得到env
	 *
	 * @param pageNum
	 * @param pageSize
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	//@GetMapping("")
	@Deprecated
	public ExecuteResult<Pager<EnvAO>> getEnvPage(@RequestParam("pageNum") int pageNum,
												  @RequestParam("pageSize") int pageSize) {
		ExecuteResult<Pager<EnvAO>> result = new ExecuteResult<>();
		Pager<EnvAO> pagerAo = new Pager<>();
		Pager<Env> pager = new Pager<>();
		pager.setPageNum(pageNum);
		pager.setPageSize(pageSize);
		ExecuteResult<Pager<Env>> resultBo = this.k8sEnvService.getEnvPage(pager);
		pagerAo.setPageSize(resultBo.getResult().getPageSize());
		pagerAo.setPageNum(resultBo.getResult().getPageNum());
		pagerAo.setTotalRecord(resultBo.getResult().getTotalRecord());
		List<EnvAO> list = new ListUtil<Env, EnvAO>().copyList(resultBo.getResult().getRecords(), EnvAO.class);
		pagerAo.setRecords(list);
		result.setResult(pagerAo);
		return result;
	}

	/**
	 * 查询envlist
	 *
	 * @author falcomlife
	 * @date 19-9-27
	 * @version 1.0.0
	 */
	@ApiOperation(value = "分页查询环境")
	@GetMapping("/{clusterId}")
	@Authority(type="cluster",level="R")
	public ExecuteResult<List<EnvAO>> getEnvList(@PathVariable("clusterId") String clusterId) {
		ExecuteResult<List<EnvAO>> result = new ExecuteResult<>();
		Env bzK8sEnvBO = new Env();
		bzK8sEnvBO.setClusterId(clusterId);
		ExecuteResult<List<Env>> list = this.k8sEnvService.getEnvList(bzK8sEnvBO);
		List<EnvAO> resultList = new ListUtil<Env, EnvAO>().copyList(list.getResult(),
				EnvAO.class);
		result.setResult(resultList);
		return result;
	}

	/**
	 * 根据集群名命名空间名查询命名空间信息（有权限校验）
	 * @param clusterName,envName
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ClusterAO>
	 * @date 2020/3/16
	 * @ahthor MddandPyy
	 */
	@GetMapping("/clusterAndEnvName")
	public ExecuteResult<EnvAO> getEnvByName(@RequestParam("clusterName") String clusterName,@RequestParam("envName") String envName){
		ExecuteResult<EnvAO> result = new ExecuteResult<>();
		List<Cluster> clusters = clusterDao.getClusterByName(clusterName);
		if(clusters.size()!=0){
			if(clusters.size()==1){
				Cluster cluster = clusters.get(0);
				Env env = envDao.getEnvByClusterAndNamespace(cluster.getId(),envName);
				if(env!=null){
					return k8sEnvService.getEnvByEnvAndClusterId(env.getId(),cluster.getId());
				}
			}else{
				result.setCode(Code.FAILED);
				// Cluster name already exists.
				//result.addErrorMessage("存在同名cluster");
				String message = messageUtils.matchMessage("M0031",new Object[]{},Boolean.TRUE);
				result.addErrorMessage(message);
				return result;
			}
		}
		return result;
	}

	/**
	 * 查询env上挂的service，用于删除集群前的校验
	 * @param envId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceAO>
	 * @date 2020/3/18
	 * @ahthor MddandPyy
	 */
	@GetMapping("/service")
	public ExecuteResult<List<ServiceAO>> getEnvService(@RequestParam("EnvId") String envId){
		return k8sEnvService.getEnvService(envId);
	}
}

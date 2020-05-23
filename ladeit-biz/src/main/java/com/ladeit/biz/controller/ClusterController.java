package com.ladeit.biz.controller;

import com.ladeit.biz.dao.ClusterDao;
import com.ladeit.biz.services.ClusterService;
import com.ladeit.biz.services.EnvService;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.doo.Cluster;
import com.ladeit.pojo.doo.Env;
import com.ladeit.pojo.doo.User;
import com.ladeit.pojo.doo.UserClusterRelation;
import com.ladeit.util.ListUtil;
import io.ebean.SqlRow;
import io.kubernetes.client.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: ladeit
 * @description: K8sClusterController
 * @author: falcomlife
 * @create: 2019/09/19
 * @version: 1.0.0
 */
@RestController
@RequestMapping(value = "/api/${api.version}/cluster")
@Api(description = "Cluster操作", tags = "/api/v1/", hidden = true)
public class ClusterController {

	@Autowired
	private ClusterService k8sClusterService;

	@Autowired
	private EnvService k8sEnvService;

	@Autowired
	private ClusterDao clusterDao;

	@Autowired
	private MessageUtils messageUtils;

	/**
	 * 创建cluster
	 *
	 * @param bzK8sClusterAO
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@ApiOperation(value = "接入集群")
	@PostMapping("")
	public ExecuteResult<String> createCluster(@RequestBody ClusterAO bzK8sClusterAO) throws IOException,
			ApiException, InterruptedException {
		Cluster bzK8sClusterBO = new Cluster();
		BeanUtils.copyProperties(bzK8sClusterAO, bzK8sClusterBO);
		return this.k8sClusterService.createCluster(bzK8sClusterBO);
	}

	/**
	 * 更新cluster
	 *
	 * @param bzK8sClusterAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@ApiOperation(value = "更新集群")
	@PutMapping("")
	public ExecuteResult<String> updateCluster(@RequestBody ClusterAO bzK8sClusterAO) {
		Cluster bzK8sClusterBO = new Cluster();
		BeanUtils.copyProperties(bzK8sClusterAO, bzK8sClusterBO);
		return this.k8sClusterService.updateCluster(bzK8sClusterAO.getId(), bzK8sClusterBO);
	}


	/**
	 * 查询集群列表
	 *
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	@ApiOperation(value = "查询集群")
	@GetMapping("")
	public ExecuteResult<List<ClusterAO>> getCluster() {
		ExecuteResult<List<ClusterAO>> result = new ExecuteResult<>();
		ExecuteResult<List<Cluster>> resultbo = this.k8sClusterService.queryClusterByUser();
		List<ClusterAO> list = new ListUtil<Cluster, ClusterAO>().copyList(resultbo.getResult(),
				ClusterAO.class);
		result.setResult(list);
		return result;
	}

	/**
	 * 查询集群列表(集群和集群下的环境)
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ClusterAO>>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@ApiOperation(value = "查询集群和服务")
	@GetMapping("/getClusterAndEnv")
	public ExecuteResult<List<ClusterAO>> getClusterAndEnv() {
		ExecuteResult<List<ClusterAO>> result = new ExecuteResult<>();
		ExecuteResult<List<Cluster>> resultbo = this.k8sClusterService.queryClusterByUser();
		List<ClusterAO> list = new ArrayList<>();
		List<Cluster> clusterList = resultbo.getResult();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		for (Cluster cluster : clusterList) {
			String clusterLevel = k8sClusterService.getUserClusterLevel(user.getId(), cluster.getId());
			ClusterAO clusterAO = new ClusterAO();
			BeanUtils.copyProperties(cluster, clusterAO);
			clusterAO.setAccessLevel(clusterLevel);
			//添加环境信息
			Env bzK8sEnvBO = new Env();
			bzK8sEnvBO.setClusterId(cluster.getId());
			ExecuteResult<List<Env>> envListResult = k8sEnvService.getEnvList(bzK8sEnvBO);
			List<EnvAO> envAOS = new ArrayList<>();
			for (Env env : envListResult.getResult()) {
				String envLevel = k8sClusterService.getUserEnvLevel(user.getId(), env.getId());
				EnvAO envAO = new EnvAO();
				BeanUtils.copyProperties(env, envAO);
				envAO.setAccessLevel(envLevel);
				envAOS.add(envAO);
			}
			clusterAO.setEnvs(envAOS);
			list.add(clusterAO);
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
	@GetMapping("/getClusterAndEnvPager")
	public ExecuteResult<Pager<ClusterAO>> getClusterAndEnvPager(@RequestParam("currentPage") int currentPage,
																 @RequestParam("pageSize") int pageSize) {
		return k8sClusterService.getClusterAndEnvPager(currentPage, pageSize);
	}

	/**
	 * 查询集群列表(集群和集群下的环境)分页,后期优化，返回sqlrow
	 *
	 * @param currentPage, pageSize
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.ClusterAO>>
	 * @date 2020/2/10
	 * @ahthor MddandPyy
	 */
	@GetMapping("/getClusterAndEnvPagerSqlrow")
	public ExecuteResult<Pager<SqlRow>> getClusterAndEnvPagerSqlrow(@RequestParam("currentPage") int currentPage,
																	@RequestParam("pageSize") int pageSize,
																	@RequestParam(value = "OrderParam", required =
																			false) String orderparam) {
		return k8sClusterService.getClusterAndEnvPagerSqlrow(currentPage, pageSize, orderparam);
	}


	/**
	 * 查询namespace
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<java.lang.String>>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@ApiOperation(value = "查询集群")
	@GetMapping("/namespace/{clusterId}")
	public ExecuteResult<List<String>> getCluster(@PathVariable("clusterId") String clusterId) throws IOException {
		return k8sClusterService.listNamespace(clusterId);
	}

	/**
	 * 查询resources
	 *
	 * @author falcomlife
	 * @date 19-9-27
	 * @version 1.0.0
	 */
	@ApiOperation(value = "分页查询环境")
	@GetMapping("/{clusterId}/{namespace}")
	public ExecuteResult<ResourceAO> getResourceInNamespace(@PathVariable("clusterId") String clusterId,
															@PathVariable("namespace") String namespace) throws ApiException, IOException, InvocationTargetException, IllegalAccessException {
		ExecuteResult<ResourceAO> result = this.k8sClusterService.getResourceInNamespace(null, clusterId, namespace);
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
	@GetMapping("/getNoPagerUsers")
	public ExecuteResult<List<UserClusterRelationAO>> queryNoPagerClusterUserInfo(@RequestParam("ClusterId") String clusterId) {
		return k8sClusterService.queryNoPagerClusterUserInfo(clusterId);
	}

	/**
	 * 查询要加入的人员信息
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	@GetMapping("/getAddUsers")
	public ExecuteResult<List<AddServiceGroupUserAO>> queryAddClusterUserInfo(@RequestParam("ClusterId") String clusterId, @RequestParam(value = "UserName", required = false) String userName, @RequestParam(value = "Email", required = false) String email) {
		return k8sClusterService.queryAddClusterUserInfo(clusterId, userName, email);
	}

	/**
	 * 添加集群人员
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@PostMapping("/addClusterRelation")
	public ExecuteResult<String> addClusterRelation(@RequestBody UserClusterRelationAO userClusterRelationAO) {
		return k8sClusterService.addClusterRelation(userClusterRelationAO.getClusterId(), userClusterRelationAO);
	}

	/**
	 * 查询集群邀请码
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@GetMapping("/getInviteCode")
	public ExecuteResult<String> inviteUser(@RequestParam("ClusterId") String clusterId) {
		return k8sClusterService.inviteUser(clusterId);
	}

	/**
	 * 添加集群人员(通过邀请码)
	 *
	 * @param inviteCode
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@PostMapping("/addClusterRelation/{inviteCode}")
	public ExecuteResult<String> addClusterRelationByInviteCode(@PathVariable("inviteCode") String inviteCode) {
		return k8sClusterService.addClusterRelationByInviteCode(inviteCode);
	}

	/**
	 * 添加集群人员(多个)
	 *
	 * @param userClusterRelationAOS
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@PostMapping("/addClusterRelationList")
	public ExecuteResult<String> addClusterRelationList(@RequestBody List<UserClusterRelationAO> userClusterRelationAOS) {
		UserClusterRelationAO userClusterRelationAO = userClusterRelationAOS.get(0);
		return k8sClusterService.addClusterRelationList(userClusterRelationAO.getClusterId(), userClusterRelationAOS);
	}

	/**
	 * 更新人员集群权限信息
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@PutMapping("/updateClusterRelation")
	public ExecuteResult<String> updateClusterRelatio(@RequestBody UserClusterRelationAO userClusterRelationAO) {
		return k8sClusterService.updateClusterRelatio(userClusterRelationAO.getClusterId(), userClusterRelationAO);
	}

	/**
	 * 更新人员命名空间权限信息
	 *
	 * @param userEnvRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@PutMapping("/updateEnvRelation")
	public ExecuteResult<String> updateEnvRelatio(@RequestBody UserEnvRelationAO userEnvRelationAO) {
		return k8sClusterService.updateEnvRelatio(userEnvRelationAO.getClusterId(), userEnvRelationAO);
	}


	/**
	 * 删除集群人员，及其在集群下所有的命名空间权限
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@DeleteMapping("/deleteClusterRelation")
	public ExecuteResult<String> deleteClusterRelation(@RequestBody UserClusterRelationAO userClusterRelationAO) {
		return k8sClusterService.deleteClusterRelation(userClusterRelationAO.getClusterId(), userClusterRelationAO);
	}

	/**
	 * 根据集群名查询集群信息（有权限校验）
	 *
	 * @param clusterName
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ClusterAO>
	 * @date 2020/3/16
	 * @ahthor MddandPyy
	 */
	@GetMapping("/clusterName")
	public ExecuteResult<ClusterAO> getClusterByName(@RequestParam("clusterName") String clusterName) {
		ExecuteResult<ClusterAO> result = new ExecuteResult<>();
		List<Cluster> clusters = clusterDao.getClusterByName(clusterName);
		if (clusters.size() != 0) {
			if (clusters.size() == 1) {
				Cluster cluster = clusters.get(0);
				return k8sClusterService.getOneClusterById(cluster.getId());
			} else {
				result.setCode(Code.FAILED);
				// Cluster name already exists.
				//result.addErrorMessage("存在同名cluster");
				String message = messageUtils.matchMessage("M0031", new Object[]{},Boolean.TRUE);
				result.addErrorMessage(message);

				return result;
			}
		}
		return result;
	}


	/**
	 * 删除集群,及其在集群下所有的命名空间,以及人员权限
	 *
	 * @param clusterAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	@DeleteMapping()
	public ExecuteResult<String> deleteCluster(@RequestBody ClusterAO clusterAO) {
		return k8sClusterService.deleteCluster(clusterAO.getId(), clusterAO);
	}

	/**
	 * 当前登录人离开分组，无需校验权限
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	@DeleteMapping("/ClusterRelation/user")
	public ExecuteResult<String> deleteClusterRelationBylogin(@RequestBody UserClusterRelationAO userClusterRelationAO) {
		return k8sClusterService.deleteClusterRelationBylogin(userClusterRelationAO);
	}

	/**
	 * 查询cluster下的env上挂的service，用于删除集群前的校验
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceAO>
	 * @date 2020/3/18
	 * @ahthor MddandPyy
	 */
	@GetMapping("/env/service")
	public ExecuteResult<List<ServiceAO>> getEnvService(@RequestParam("ClusterId") String clusterId) {
		return k8sClusterService.getEnvService(clusterId);
	}

}

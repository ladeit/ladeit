package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.EnvAO;
import com.ladeit.pojo.ao.ServiceAO;
import com.ladeit.pojo.doo.Env;
import io.kubernetes.client.ApiException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

/**
 * @program: ladeit
 * @description: EnvService
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
public interface EnvService {

	/**
	 * 根据id得到env
	 *
	 * @param id
	 * @return com.ladeit.pojo.doo.Env
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	Env getEnvById(String id);

	/**
	 * 创建env
	 *
	 * @param env
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	ExecuteResult<String> createEnv(Env env) throws IOException, ApiException;

	/**
	 * 创建一个env不创建k8s资源
	 *
	 * @param env
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-6-2
	 * @version 1.0.0
	 */
	ExecuteResult<String> createEnvWithoutK8s(Env env) throws ApiException, IOException;

	/**
	 * 更新env
	 *
	 * @param env
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> updateEnv(String envId, Env env) throws IOException, ApiException;

	/**
	 * 删除env
	 *
	 * @param bzK8sEnvAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> deleteEnv(String envId, EnvAO bzK8sEnvAO) throws IOException, ApiException;

	/**
	 * 删除env
	 *
	 * @param bzK8sEnvAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> deleteEnvIgnoreK8s(String envId, EnvAO bzK8sEnvAO) throws IOException, ApiException;

	/**
	 * 查询env
	 *
	 * @param pager
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	ExecuteResult<Pager<Env>> getEnvPage(Pager<Env> pager);

	/**
	 * 查询envlist
	 *
	 * @param bzK8sEnvBO
	 * @author falcomlife
	 * @date 19-9-27
	 * @version 1.0.0
	 */
	ExecuteResult<List<Env>> getEnvList(Env bzK8sEnvBO, String config) throws IOException, ApiException;

	/**
	 * 获取所有的env
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.doo.Env>>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	ExecuteResult<List<Env>> getAllEnv();

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
	ExecuteResult<String> namespaceType(String clusterId, String envId) throws IOException, ApiException;

	/**
	 * 查询服务信息（有权限校验）
	 *
	 * @param envId,clusterId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ClusterAO>
	 * @date 2020/3/16
	 * @ahthor MddandPyy
	 */
	ExecuteResult<EnvAO> getEnvByEnvAndClusterId(String envId, String clusterId) throws ApiException;

	/**
	 * 查询env上挂的service，用于删除集群前的校验
	 *
	 * @param envId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ServiceAO>>
	 * @author MddandPyy
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	ExecuteResult<List<ServiceAO>> getEnvService(String envId);

	/**
	 * 更新env，同步刷新的时候调用，主要更新配额
	 *
	 * @param env
	 * @return void
	 * @author falcomlife
	 * @date 20-5-25
	 * @version 1.0.0
	 */
	void updateEnvQuota(Env env);
}
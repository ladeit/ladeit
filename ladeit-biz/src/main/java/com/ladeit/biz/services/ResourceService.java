package com.ladeit.biz.services;

import com.alibaba.fastjson.JSONObject;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.*;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: ResourceService
 * @author: falcomlife
 * @create: 2019/11/18
 * @version: 1.0.0
 */
public interface ResourceService {
	/**
	 * 查询yaml
	 *
	 * @param clusterId
	 * @param type
	 * @param namespace
	 * @param name
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-18
	 * @version 1.0.0
	 */
	ExecuteResult<String> getYaml(String clusterId, String type, String namespace, String name) throws InvocationTargetException, IllegalAccessException;

	/**
	 * 查询service组件
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.WorkLoadAO>>
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	ExecuteResult<List<WorkLoadAO>> getWorkLoads(String serviceId) throws IOException;

	/**
	 * 查询service组件
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ServiceIngressAO>>
	 * @author falcomlife
	 * @date 19-12-4
	 * @version 1.0.0
	 */
	ExecuteResult<List<ServiceIngressAO>> getServices(String serviceId) throws IOException;

	/**
	 * 查询分类组件
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.TypesResourceAO>>
	 * @author falcomlife
	 * @date 19-12-7
	 * @version 1.0.0
	 */
	ExecuteResult<List<TypesResourceAO>> getTypesResources(String serviceId) throws InvocationTargetException,
			IllegalAccessException, NoSuchMethodException, IOException, ApiException;

	/**
	 * 查询分类组件
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.TypesResourceAO>>
	 * @author falcomlife
	 * @date 19-12-7
	 * @version 1.0.0
	 */
	ExecuteResult<List<QueryResourceAO>> getTypesResourcesName(String serviceId) throws InvocationTargetException,
			IllegalAccessException, NoSuchMethodException, IOException, ApiException;


	/**
	 * 资源列表页面页面查询yaml
	 *
	 * @param serviceId
	 * @param type
	 * @param name
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-17
	 * @version 1.0.0
	 */
	ExecuteResult<String> getYamlInService(String serviceId, String type, String name) throws InvocationTargetException, IllegalAccessException;

	/**
	 * release资源列表页面新建资源
	 *
	 * @param yaml
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-17
	 * @version 1.0.0
	 */
	ExecuteResult<String> createByYaml(String yaml, String serviceId) throws IOException, ApiException;

	/**
	 * release资源列表页面修改资源
	 *
	 * @param yaml
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-17
	 * @version 1.0.0
	 */
	ExecuteResult<String> replaceByYaml(String yaml, String serviceId) throws IOException, ApiException;

	/**
	 * 服务详情伸缩pod数量
	 *
	 * @param count
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-20
	 * @version 1.0.0
	 */
	ExecuteResult<String> scaleWorkload(int count, String serviceId) throws IOException, ApiException;

	/**
	 * 查询service包含的各个状态的pod的数量
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.Map<java.lang.String,java.lang.Integer>>
	 * @author falcomlife
	 * @date 19-12-20
	 * @version 1.0.0
	 */
	ExecuteResult<Map<String, Long>> getPodsStatus(String serviceId) throws IOException;

	/**
	 * 查询service的拓扑图
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.TopologyAO>
	 * @author falcomlife
	 * @date 19-12-21
	 * @version 1.0.0
	 */
	ExecuteResult<TopologyAO> getTopology(String serviceId) throws IOException;

	/**
	 * 更新拓补图
	 *
	 * @param topologyAO
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-21
	 * @version 1.0.0
	 */
	ExecuteResult<String> updateTopology(TopologyAO topologyAO, String serviceId) throws IOException;

	/**
	* 查询分组拓扑图
	* @author falcomlife
	* @date 20-4-10
	* @version 1.0.0
	* @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.GroupTopologyAO>
	* @param groupId
	*/
	ExecuteResult<GroupTopologyAO> getGroupTopology(String groupId);

	/**
	 * 查看service的配置信息（每次更新时候用）
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ConfigurationAO>
	 * @author falcomlife
	 * @date 20-4-6
	 * @version 1.0.0
	 */
	ExecuteResult<ConfigurationAO> getConfiguration(String serviceId) throws IOException, ApiException;

	/**
	 * 根据类型和uid查询对象
	 *
	 * @param config
	 * @param uid
	 * @param type
	 * @return com.ladeit.common.ExecuteResult<java.lang.Object>
	 * @author falcomlife
	 * @date 20-4-8
	 * @version 1.0.0
	 */
	ExecuteResult<JSONObject> getResourceByUid(String config, String uid, String type) throws IOException;

	/**
	 * 根据releaseId查询资源
	 *
	 * @param config
	 * @param releaseId
	 * @param type
	 * @return com.ladeit.common.ExecuteResult<com.alibaba.fastjson.JSONObject>
	 * @author falcomlife
	 * @date 20-4-14
	 * @version 1.0.0
	 */
	ExecuteResult<JSONObject> getResourceByReleaseIdSelector(String config, String releaseId, String type) throws IOException;

	/**
	 * 生成k8sapiclient
	 *
	 * @param id
	 * @return io.kubernetes.client.ApiClient
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	ApiClient clientGenerateByClusterId(String id) throws IOException;

	/**
	 * 生成k8sapiclient
	 *
	 * @param id
	 * @return io.kubernetes.client.ApiClient
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	ApiClient clientGenerateByEnvId(String id) throws IOException;

	/**
	 * 生成k8sapiclient
	 *
	 * @param id
	 * @return io.kubernetes.client.ApiClient
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	ApiClient clientGenerateByServiceId(String id) throws IOException;

	/**
	 * 得到k8s config
	 *
	 * @param id
	 * @return io.kubernetes.client.ApiClient
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	String getConfigByServiceId(String id);
}

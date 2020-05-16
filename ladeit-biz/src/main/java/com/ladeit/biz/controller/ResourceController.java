package com.ladeit.biz.controller;

import com.ladeit.biz.services.ResourceService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.*;
import freemarker.template.utility.Execute;
import io.kubernetes.client.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: ResourceController
 * @author: falcomlife
 * @create: 2019/11/18
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/api/v1/resource")
public class ResourceController {

	@Autowired
	private ResourceService resourceService;

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
	@GetMapping("/{clusterId}/{type}/{namespace}/{name}")
	public ExecuteResult<String> getYaml(@PathVariable("clusterId") String clusterId,
										 @PathVariable("type") String type,
										 @PathVariable("namespace") String namespace,
										 @PathVariable("name") String name) throws InvocationTargetException,
			IllegalAccessException {
		return this.resourceService.getYaml(clusterId, type, namespace, name);
	}

	/**
	 * 资源列表页面页面查询yaml
	 *
	 * @param serviceId
	 * @param type
	 * @param name
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-18
	 * @version 1.0.0
	 */
	@GetMapping("/{serviceId}/{type}/{name}")
	public ExecuteResult<String> getYamlInService(@PathVariable("serviceId") String serviceId,
												  @PathVariable("type") String type,
												  @PathVariable("name") String name) throws InvocationTargetException,
			IllegalAccessException {
		return this.resourceService.getYamlInService(serviceId, type, name);
	}

	/**
	 * 查询service组件
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.WorkLoadAO>>
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	@Deprecated
	@GetMapping("/workloads/{serviceId}")
	public ExecuteResult<List<WorkLoadAO>> getWorkloads(@PathVariable("serviceId") String serviceId) throws IOException {
		return this.resourceService.getWorkLoads(serviceId);
	}

	/**
	 * 查询service组件
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ServiceIngressAO>>
	 * @author falcomlife
	 * @date 19-12-4
	 * @version 1.0.0
	 */
	@Deprecated
	@GetMapping("/services/{serviceId}")
	public ExecuteResult<List<ServiceIngressAO>> getServices(@PathVariable("serviceId") String serviceId) throws IOException {
		return this.resourceService.getServices(serviceId);
	}

	/**
	 * 查询分类组件
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.TypesResourceAO>>
	 * @author falcomlife
	 * @date 19-12-7
	 * @version 1.0.0
	 */
	@GetMapping("/{serviceId}")
	public ExecuteResult<List<TypesResourceAO>> getTypesResources(@PathVariable("serviceId") String serviceId) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, IOException, ApiException {
		return this.resourceService.getTypesResources(serviceId);
	}

	/**
	 * 查询分类组件
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.TypesResourceAO>>
	 * @author falcomlife
	 * @date 19-12-7
	 * @version 1.0.0
	 */
	@GetMapping("/name/{serviceId}")
	public ExecuteResult<List<QueryResourceAO>> getTypesResourcesName(@PathVariable("serviceId") String serviceId) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, IOException, ApiException {
		return this.resourceService.getTypesResourcesName(serviceId);
	}

	/**
	 * release资源列表页面修改资源
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-17
	 * @version 1.0.0
	 */
	@PutMapping("/{serviceId}")
	public ExecuteResult<String> applyYaml(@RequestBody String yaml, @PathVariable String serviceId) throws IOException
			, ApiException {
		return this.resourceService.replaceByYaml(yaml, serviceId);
	}

	/**
	 * release资源列表页面创建资源
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-17
	 * @version 1.0.0
	 */
	@PostMapping("/{serviceId}")
	public ExecuteResult<String> applyNewYaml(@RequestBody String yaml, @PathVariable String serviceId) throws IOException
			, ApiException {
		return this.resourceService.createByYaml(yaml, serviceId);
	}

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
	@PutMapping("/{serviceId}/pod")
	public ExecuteResult<String> scaleWorkload(@RequestBody int count, @PathVariable String serviceId) throws IOException, ApiException {
		return this.resourceService.scaleWorkload(count, serviceId);
	}

	/**
	 * 查询service包含的各个状态的pod的数量
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.Map<java.lang.String,java.lang.Integer>>
	 * @author falcomlife
	 * @date 19-12-20
	 * @version 1.0.0
	 */
	@GetMapping("/{serviceId}/pod")
	public ExecuteResult<Map<String, Long>> getPodsStatus(@PathVariable String serviceId) throws IOException {
		return this.resourceService.getPodsStatus(serviceId);
	}

	/**
	 * 查询service的拓扑图
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.TopologyAO>
	 * @author falcomlife
	 * @date 19-12-21
	 * @version 1.0.0
	 */
	@GetMapping("/{serviceId}/topology")
	public ExecuteResult<TopologyAO> getTopology(@PathVariable String serviceId) throws IOException {
		return this.resourceService.getTopology(serviceId);
	}


	/**
	 * 查询分组拓扑图
	 *
	 * @param groupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.TopologyAO>
	 * @author falcomlife
	 * @date 19-12-21
	 * @version 1.0.0
	 */
	@GetMapping("/group/{groupId}/topology")
	public ExecuteResult<GroupTopologyAO> getGroupTopology(@PathVariable String groupId) throws IOException {
		return this.resourceService.getGroupTopology(groupId);
	}

	/**
	 * 查询service的拓扑图
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.TopologyAO>
	 * @author falcomlife
	 * @date 19-12-21
	 * @version 1.0.0
	 */
	@PutMapping("/{serviceId}/topology")
	public ExecuteResult<String> updateTopology(@RequestBody TopologyAO topologyAO, @PathVariable String serviceId) throws IOException {
		return this.resourceService.updateTopology(topologyAO, serviceId);
	}

	/**
	* 查看service的配置信息（每次更新时候用）
	* @author falcomlife
	* @date 20-4-6
	* @version 1.0.0
	* @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ConfigurationAO>
	* @param
	*/
	@GetMapping("/{serviceId}/configuration")
	public ExecuteResult<ConfigurationAO> getConfiguration(@PathVariable String serviceId) throws IOException,
			ApiException {
		return this.resourceService.getConfiguration(serviceId);
	}

	/**
	* 获取服务下面所有的yaml
	* @author falcomlife
	* @date 20-5-16
	* @version 1.0.0
	* @return com.ladeit.common.ExecuteResult<java.lang.String>
	* @param serviceId
	*/
	@GetMapping("/{serviceId}/yaml")
	public ExecuteResult<String> getAllYamlInService(@PathVariable String serviceId) throws IOException {
		return this.resourceService.getAllYamlInService(serviceId);
	}
}

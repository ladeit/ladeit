package com.ladeit.biz.manager;

import com.ladeit.pojo.ao.YamlContentAO;
import io.kubernetes.client.ApiCallback;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1Service;

import java.io.IOException;
import java.util.List;

public interface K8sContainerManager {
	/**
	 * 运行yaml
	 *
	 * @param yamlContent
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	String replaceByYaml(YamlContentAO yamlContent, String k8sKubeconfig, String name, ApiCallback apiCallback) throws IOException, ApiException;

	/**
	 * 通过YAML文件创建
	 *
	 * @param yamlContent
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	@SuppressWarnings("unlikely-arg-type")
	String createByYaml(YamlContentAO yamlContent, String k8sKubeconfig, String name) throws IOException,
			ApiException;

	/**
	 * 运行deployment
	 *
	 * @param deployment
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	String applyYaml(String config, V1Deployment deployment) throws ApiException;

	/**
	 * 运行service
	 *
	 * @param config
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-11-7
	 * @version 1.0.0
	 */
	String applyYaml(String config, V1Service service) throws ApiException;

	/**
	 * 直接运行yaml文件
	 *
	 * @param yaml
	 * @param config
	 * @return void
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	void applyYaml(String yaml, String config) throws IOException;

	/**
	 * 通过uid获取资源
	 *
	 * @param uid
	 * @return
	 * @author falcomlife
	 * @date 19-12-1
	 * @version 1.0.0
	 */
	V1Pod getPodByUid(String uid, String config) throws ApiException;

	/**
	 * 查询podlist
	 *
	 * @param namespace
	 * @return
	 */
	List<V1Pod> getPodList(String namespace, String config) throws ApiException;
}

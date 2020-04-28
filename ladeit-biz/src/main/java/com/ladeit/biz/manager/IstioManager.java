package com.ladeit.biz.manager;

import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;

import java.io.IOException;

/**
 * @program: ladeit
 * @description: IstioManager
 * @author: falcomlife
 * @create: 2019/11/19
 * @version: 1.0.0
 */
public interface IstioManager {

	/**
	 * 创建VirtualService
	 *
	 * @param content
	 * @return void
	 * @author falcomlife
	 * @date 19-11-19
	 * @version 1.0.0
	 */
	VirtualService createVirtualServices(String content, VirtualService virtualService) throws IOException;

	/**
	 * 创建Gateway
	 *
	 * @param gateway
	 * @return void
	 * @author falcomlife
	 * @date 19-11-19
	 * @version 1.0.0
	 */
	void createGateway(String content, Gateway gateway) throws IOException;

	/**
	 * 创建DestinationRule
	 *
	 * @param destinationRule
	 * @return void
	 * @author falcomlife
	 * @date 19-11-19
	 * @version 1.0.0
	 */
	void createDestinationrules(String content, DestinationRule destinationRule) throws IOException;

	/**
	 * 查找destinationrule
	 *
	 * @param k8sKubeconfig
	 * @param name
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 19-11-20
	 * @version 1.0.0
	 */
	DestinationRule getDestinationrules(String k8sKubeconfig, String name, String namespace) throws IOException;

	/**
	 * 查找virtualService
	 *
	 * @param k8sKubeconfig
	 * @param name
	 * @param namespace
	 * @return me.snowdrop.istio.api.networking.v1alpha3.VirtualService
	 * @author falcomlife
	 * @date 19-11-20
	 * @version 1.0.0
	 */
	VirtualService getVirtualservice(String k8sKubeconfig, String name, String namespace) throws IOException;

	/**
	 * 删除virtualService
	 *
	 * @param k8sKubeconfig
	 * @param name
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 20-1-13
	 * @version 1.0.0
	 */
	void deleteVirtualservice(String k8sKubeconfig, String name, String namespace) throws IOException;

	/**
	 * 删除destinationrule
	 *
	 * @param k8sKubeconfig
	 * @param name
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 20-1-13
	 * @version 1.0.0
	 */
	void deleteDestinationrules(String k8sKubeconfig, String name, String namespace) throws IOException;

	/**
	 * 删除Gateway
	 *
	 * @param k8sKubeconfig
	 * @param name
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 20-1-13
	 * @version 1.0.0
	 */
	void deleteGateway(String k8sKubeconfig, String name, String namespace) throws IOException;
}

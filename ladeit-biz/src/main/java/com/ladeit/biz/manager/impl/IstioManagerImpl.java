package com.ladeit.biz.manager.impl;

import com.ladeit.biz.manager.IstioManager;
import io.fabric8.kubernetes.client.Config;
import lombok.extern.slf4j.Slf4j;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import me.snowdrop.istio.client.DefaultIstioClient;
import me.snowdrop.istio.client.IstioClient;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @program: ladeit
 * @description: IstioManagerImpl
 * @author: falcomlife
 * @create: 2019/11/19
 * @version: 1.0.0
 */
@Component
@Slf4j
public class IstioManagerImpl implements IstioManager {

	/**
	 * 创建VirtualService
	 *
	 * @param content
	 * @return void
	 * @author falcomlife
	 * @date 19-11-19
	 * @version 1.0.0
	 */
	@Override
	public VirtualService createVirtualServices(String content, VirtualService virtualService) throws IOException {
		IstioClient istioClient = this.getIstioClient(content);
		return istioClient.virtualService().inNamespace(virtualService.getMetadata().getNamespace()).createOrReplace(virtualService);
	}

	/**
	 * 创建Gateway
	 *
	 * @param gateway
	 * @return void
	 * @author falcomlife
	 * @date 19-11-19
	 * @version 1.0.0
	 */
	@Override
	public void createGateway(String content, Gateway gateway) throws IOException {
		IstioClient istioClient = this.getIstioClient(content);
		istioClient.gateway().inNamespace(gateway.getMetadata().getNamespace()).createOrReplace(gateway);
	}

	/**
	 * 创建DestinationRule
	 *
	 * @param destinationRule
	 * @return void
	 * @author falcomlife
	 * @date 19-11-19
	 * @version 1.0.0
	 */
	@Override
	public void createDestinationrules(String content, DestinationRule destinationRule) throws IOException {
		IstioClient istioClient = this.getIstioClient(content);
		istioClient.destinationRule().inNamespace(destinationRule.getMetadata().getNamespace()).createOrReplace(destinationRule);
	}

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
	@Override
	public DestinationRule getDestinationrules(String k8sKubeconfig, String name, String namespace) throws IOException {
		IstioClient istioClient = this.getIstioClient(k8sKubeconfig);
		return istioClient.destinationRule().inNamespace(namespace).withName(name).get();
	}

	/**
	 * 查找 virtualService
	 *
	 * @param k8sKubeconfig
	 * @param name
	 * @param namespace
	 * @return me.snowdrop.istio.api.networking.v1alpha3.VirtualService
	 * @author falcomlife
	 * @date 19-11-20
	 * @version 1.0.0
	 */
	@Override
	public VirtualService getVirtualservice(String k8sKubeconfig, String name, String namespace) throws IOException {
		IstioClient istioClient = this.getIstioClient(k8sKubeconfig);
		return istioClient.virtualService().inNamespace(namespace).withName(name).get();
	}

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
	@Override
	public void deleteVirtualservice(String k8sKubeconfig, String name, String namespace) throws IOException {
		IstioClient istioClient = this.getIstioClient(k8sKubeconfig);
		VirtualService virtualService = istioClient.virtualService().inNamespace(namespace).withName(name).get();
		if (virtualService != null) {
			istioClient.virtualService().inNamespace(namespace).withName(name).delete();
		}
	}

	/**
	 * 删除destinationRule
	 *
	 * @param k8sKubeconfig
	 * @param name
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 20-1-13
	 * @version 1.0.0
	 */
	@Override
	public void deleteDestinationrules(String k8sKubeconfig, String name, String namespace) throws IOException {
		IstioClient istioClient = this.getIstioClient(k8sKubeconfig);
		DestinationRule destinationRule = istioClient.destinationRule().inNamespace(namespace).withName(name).get();
		if (destinationRule != null) {
			istioClient.destinationRule().inNamespace(namespace).withName(name).delete();
		}
	}

	/**
	 * 删除gateway
	 *
	 * @param k8sKubeconfig
	 * @param name
	 * @param namespace
	 * @return void
	 * @author falcomlife
	 * @date 20-1-13
	 * @version 1.0.0
	 */
	@Override
	public void deleteGateway(String k8sKubeconfig, String name, String namespace) throws IOException {
		IstioClient istioClient = this.getIstioClient(k8sKubeconfig);
		Gateway gateway = istioClient.gateway().inNamespace(namespace).withName(name).get();
		if (gateway != null) {
			istioClient.gateway().inNamespace(namespace).withName(name).delete();
		}
	}

	/**
	 * 生成istioclient
	 *
	 * @param content
	 * @return me.snowdrop.istio.client.IstioClient
	 * @author falcomlife
	 * @date 19-11-19
	 * @version 1.0.0
	 */
	private IstioClient getIstioClient(String content) throws IOException {
		Config config = Config.fromKubeconfig(content);
		IstioClient istioClient = new DefaultIstioClient(config);
		return istioClient;
	}
}

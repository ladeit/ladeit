package com.ladeit.pojo.ao;

import com.ladeit.pojo.ao.serviceIngress.ServicePort;
import lombok.Data;

import java.util.List;

/**
 * @program: ladeit
 * @description: ServiceIngressAO
 * @author: falcomlife
 * @create: 2019/12/01
 * @version: 1.0.0
 */
@Data
public class ServiceIngressAO {

	/**
	 * 名称
	 */
	private String name;
	/**
	 * 负载均衡器
	 */
	private String loadbalance;
	/**
	 * 集群ip
	 */
	private String clusterIp;
	/**
	 * 节点端口
	 */
	private List<ServicePort> servicePort;
	/**
	 * 入站
	 */
	private List<String> ingress;
}

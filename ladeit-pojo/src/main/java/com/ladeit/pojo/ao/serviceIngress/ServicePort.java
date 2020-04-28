package com.ladeit.pojo.ao.serviceIngress;

import lombok.Data;

/**
 * @program: ladeit
 * @description: ServicePort
 * @author: falcomlife
 * @create: 2019/12/05
 * @version: 1.0.0
 */
@Data
public class ServicePort {
	private String name;
	private String port;
	private String protocal;
	private String targetPort;
	private String nodePort;
}

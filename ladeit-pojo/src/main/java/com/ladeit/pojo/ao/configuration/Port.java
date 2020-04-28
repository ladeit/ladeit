package com.ladeit.pojo.ao.configuration;

import lombok.Data;

/**
 * @program: ladeit
 * @description: Port
 * @author: falcomlife
 * @create: 2020/01/07
 * @version: 1.0.0
 */
@Data
public class Port {
	private String name;
	private int containerPort;
	private int servicePort;
}

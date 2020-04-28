package com.ladeit.biz.moniter;

import lombok.Data;

/**
 * @program: ladeit
 * @description: K8sEventEvent
 * @author: falcomlife
 * @create: 2020/01/15
 * @version: 1.0.0
 */
@Data
public class K8sEventEvent {
	private String type;
	private Integer status;
	private String message;
}

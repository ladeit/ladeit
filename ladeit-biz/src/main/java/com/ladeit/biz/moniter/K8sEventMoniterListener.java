package com.ladeit.biz.moniter;

/**
 * @program: ladeit
 * @description: K8sEventMoniterListener
 * @author: falcomlife
 * @create: 2020/01/15
 * @version: 1.0.0
 */
public interface K8sEventMoniterListener {

	void fallback(K8sEventEvent k8sEventEvent);

}

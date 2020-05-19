package com.ladeit.biz.moniter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ladeit.biz.manager.K8sClusterManager;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1beta1Event;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @program: ladeit
 * @description: K8sEventSource
 * @author: falcomlife
 * @create: 2020/01/15
 * @version: 1.0.0
 */
@Slf4j
public class K8sEventSource {

	private K8sClusterManager k8sClusterManager;
	private String config;
	private String namespace;
	private String type;
	private String name;

	List<K8sEventMoniterListener> listenerList = new ArrayList<>();

	public K8sEventSource(K8sClusterManager k8sClusterManager, String config, String namespace, String type, String name){
		this.k8sClusterManager = k8sClusterManager;
		this.config = config;
		this.namespace = namespace;
		this.type = type;
		this.name = name;
	}

	public void addMoniter(K8sEventMoniterListener listener) {
		this.listenerList.add(listener);
	}

	public void removeMoniter(K8sEventMoniterListener listener) {
		this.listenerList.remove(listener);
	}

	public void startAction() {

		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
				.setNameFormat("event-pool-%d").build();
		ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1,
				0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
		singleThreadPool.execute(() -> {
			while(true) {
				try {
					Thread.sleep(1000);
					List<V1beta1Event> list = k8sClusterManager.getResourceEvent(this.config, this.namespace,
							null);
					list.stream().filter(event -> event.getRegarding().getName().startsWith(this.name) && this.type.equals(event.getRegarding().getKind())).forEach(event -> {
						if ("SuccessfulCreate".equals(event.getReason())) {
							this.notifyMoniter(event.getNote());
							return;
						}
					});
				} catch (ApiException e) {
					log.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	private void notifyMoniter(String message) {
		this.listenerList.stream().forEach(listener -> {
			K8sEventEvent event = new K8sEventEvent();
			event.setType(this.type);
			event.setStatus(1);
			event.setMessage(message);
			listener.fallback(event);
		});
	}
}

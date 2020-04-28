package com.ladeit.biz.runner.pods;

import io.kubernetes.client.ApiException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: ladeit
 * @description: EventHandlerImpl
 * @author: falcomlife
 * @create: 2020/04/02
 * @version: 1.0.0
 */
@Component
public class PodHandlerImpl implements PodHandler {

	private Map<String, PodSubcriber> subcriberPool = new HashMap<>();

	@Override
	public Map<String, PodSubcriber> getSubcriberPool() {
		return subcriberPool;
	}

	@Override
	public void put(String envId, String rev) throws IOException {
		PodSubcriber es = new PodSubcriber(envId, this, rev);
		es.init();
		this.subcriberPool.put(envId, es);
		es.watch();
	}

	@Override
	public void remove(String envId) throws IOException {
		PodSubcriber es = this.subcriberPool.get(envId);
		es.setStop(true);
		es.getWatch().close();
		this.subcriberPool.remove(envId);
	}

	@Override
	public void error(String envId, String rev) throws IOException {
		this.remove(envId);
		this.put(envId, rev);
	}
}

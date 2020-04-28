package com.ladeit.biz.runner.events;

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
public class EventHandlerImpl implements EventHandler {

	private Map<String, EventsSubcriber> subcriberPool = new HashMap<>();

	@Override
	public Map<String, EventsSubcriber> getSubcriberPool() {
		return subcriberPool;
	}

	@Override
	public void put(String envId, String rev) throws IOException, ApiException {
		EventsSubcriber es = new EventsSubcriber(envId, this, rev);
		es.init();
		this.subcriberPool.put(envId, es);
		es.watch();
	}

	@Override
	public void remove(String envId) throws IOException {
		EventsSubcriber es = this.subcriberPool.get(envId);
		es.setStop(true);
		es.getWatch().close();
		this.subcriberPool.remove(envId);
	}

	@Override
	public void error(String envId, String rev) throws IOException, ApiException {
		this.remove(envId);
		this.put(envId, rev);
	}
}

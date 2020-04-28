package com.ladeit.biz.runner.events;

import io.kubernetes.client.ApiException;

import java.io.IOException;
import java.util.Map;

/**
 * @program: ladeit
 * @description: EventHandler
 * @author: falcomlife
 * @create: 2020/04/02
 * @version: 1.0.0
 */
public interface EventHandler {

	Map<String, EventsSubcriber> getSubcriberPool();

	void put(String envId, String rev) throws IOException, ApiException;

	void remove(String envId) throws IOException;

	void error(String envId, String rev) throws IOException, ApiException;
}

package com.ladeit.biz.runner.pods;

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
public interface PodHandler {

	Map<String, PodSubcriber> getSubcriberPool();

	void put(String envId, String rev) throws IOException, ApiException;

	void remove(String envId) throws IOException;

	void error(String envId, String rev) throws IOException, ApiException;
}

package com.ladeit.util.k8s;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.util.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @program: ladeit
 * @description: Config2ApiClientUtil
 * @author: falcomlife
 * @create: 2020/04/08
 * @version: 1.0.0
 */
public class K8sClientUtil {

	/**
	 * 通过config字符串获取apiclient
	 *
	 * @param config
	 * @return io.kubernetes.client.ApiClient
	 * @author falcomlife
	 * @date 20-4-8
	 * @version 1.0.0
	 */
	public static ApiClient get(String config) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(config.getBytes());
		ApiClient apiClient = Config.fromConfig(inputStream);
		Configuration.setDefaultApiClient(apiClient);
		return apiClient;
	}

	/**
	 * 根据config返回api
	 *
	 * @param config
	 * @param T
	 * @return java.lang.Object
	 * @author falcomlife
	 * @date 20-4-11
	 * @version 1.0.0
	 */
	public static <T> Object get(String config, Class T) {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(config.getBytes());
		ApiClient apiClient = null;
		try {
			apiClient = Config.fromConfig(inputStream);
			Configuration.setDefaultApiClient(apiClient);
			return T.newInstance();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage() ,e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage() ,e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e.getMessage() ,e);
		}
	}
}

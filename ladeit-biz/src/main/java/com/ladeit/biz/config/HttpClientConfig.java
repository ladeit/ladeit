package com.ladeit.biz.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {
	/**
	 * 
	 * @return 创建HttpClient对象
	 */
	@Bean
	public CloseableHttpClient getOkHttpClient() {
		return HttpClients.createDefault();
	}
}

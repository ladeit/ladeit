package com.ladeit.biz.config;

import okhttp3.OkHttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

	private Integer connectTimeout = 10;
	private Integer writeTimeout = 10;
	private Integer readTimeout = 20;

	/**
	 * 创建单利httpclient对象
	 *
	 * @return
	 * @author falcomlife
	 * @date 20-5-28
	 * @version 1.0.0
	 * @deprecated 为了做webkubectl功能，这里将此方式弃用。
	 */
	@Deprecated
	@Bean
	public CloseableHttpClient getOkHttpClient() {
		return HttpClients.createDefault();
	}

	@Bean(name = "globalOkHttpClient")
	public OkHttpClient okHttpClient() {
		return new OkHttpClient.Builder().connectTimeout(this.connectTimeout, TimeUnit.SECONDS).writeTimeout(this.writeTimeout, TimeUnit.SECONDS).readTimeout(this.readTimeout, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();
	}
}

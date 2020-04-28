package com.ladeit.biz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.websocket.server.ServerEndpointConfig.Configurator;

/**
 * @program: ladeit
 * @description: GetHttpSessionConfig
 * @author: falcomlife
 * @create: 2019/08/03
 * @version: 1.0.0
 */
@Configuration
public class WebSocketConfig extends Configurator {

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}
}

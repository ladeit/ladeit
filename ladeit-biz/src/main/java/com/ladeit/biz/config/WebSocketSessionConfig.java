package com.ladeit.biz.config;

import com.ladeit.common.Token.UserInfo;
import com.ladeit.common.Token.UserInfoUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

/**
 * @program: ladeit
 * @description: GetHttpSessionConfig
 * @author: falcomlife
 * @create: 2019/08/03
 * @version: 1.0.0
 */
@Configuration
public class WebSocketSessionConfig extends Configurator {

	/**
	 * 重写握手
	 *
	 * @param sec
	 * @param request
	 * @param response
	 * @FunctionName modifyHandshake
	 * @author falcomlife
	 * @date 19-8-3
	 * @version 1.0.0
	 * @Return void
	 */
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		UserInfo userInfo = UserInfoUtil.getInfo();
		sec.getUserProperties().put("token", userInfo.getOauthToken());
		sec.getUserProperties().put("tokenType", userInfo.getTokenType());
		UserInfoUtil.remove();
	}

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		//这个对象说一下，貌似只有服务器是tomcat的时候才需要配置,具体我没有研究
		return new ServerEndpointExporter();
	}
}

package com.ladeit.biz.websocket.events;

import com.alibaba.fastjson.JSONObject;
import com.ladeit.biz.config.SpringBean;
import com.ladeit.biz.config.WebSocketConfig;
import com.ladeit.biz.services.ReleaseService;
import com.ladeit.biz.services.ServiceService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.util.ListUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @program: BuzzyCore
 * @description: JobLogWebSocket
 * @author: sora
 * @create: 2019/07/31
 * @version: 1.0.0
 */
@ServerEndpoint(value = "/api/v1/events/{userId}", configurator = WebSocketConfig.class)
@Component
@Scope("prototype")
@Slf4j
public class EventsWebSocket {

	private Session session;
	private String[] ids;

	/**
	 * 开启链接
	 *
	 * @param session
	 * @FunctionName onOpen
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("userId") String userId) {
		this.session = session;
		ServiceService serviceService = SpringBean.getObject(ServiceService.class);
		RedisReceiver redisReceiver = SpringBean.getObject(RedisReceiver.class);
		ExecuteResult<List<String>> result = serviceService.getServiceBelongUser(userId);
		if (result.getCode() != Code.SUCCESS) {
			return;
		}
		redisReceiver.getThreadServiceIds().put(this, result.getResult());
		log.info("成功建立socket链接");
	}

	/**
	 * 关闭链接
	 *
	 * @param
	 * @FunctionName onClose
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	@OnClose
	public void onClose() {
		try {
			this.session.close();
			RedisReceiver redisReceiver = SpringBean.getObject(RedisReceiver.class);
			redisReceiver.getThreadServiceIds().remove(this);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		log.info("socket链接关闭");
	}

	/**
	 * 接收到client端信息
	 *
	 * @param message
	 * @param session
	 * @FunctionName onMessagell
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		log.info("接收到client端信息" + message);
	}

	/**
	 * 异常
	 *
	 * @param session
	 * @param error
	 * @FunctionName onError
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		log.info("发生异常" + error);
	}

	/**
	 * 向前台传送数据
	 *
	 * @param eventSub
	 * @FunctionName sendMessage
	 * @author sora
	 * @date 19-7-31
	 * @version 1.0.0
	 * @Return void
	 */
	public void sendMessage(EventSub eventSub) {
		this.session.getAsyncRemote().sendText(JSONObject.toJSON(eventSub).toString());
	}
}

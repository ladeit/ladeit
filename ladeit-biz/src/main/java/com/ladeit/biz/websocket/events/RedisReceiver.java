package com.ladeit.biz.websocket.events;

import com.alibaba.fastjson.JSONObject;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @program: ladeit-parent
 * @description: RedisReceiver
 * @author: falcomlife
 * @create: 2020/04/27
 * @version: 1.0.0
 */
@Configuration
public class RedisReceiver {

	public static Map<EventsWebSocket, List<String>> threadServiceIds;

	public void receiveMessage(String message) {
		EventSub eventSub = JSONObject.parseObject(message).toJavaObject(EventSub.class);
		threadServiceIds.forEach((ews,list)->{
			list.stream().filter(id -> id.equals(eventSub.getServiceId())).forEach(id -> {
				ews.sendMessage(eventSub);
			});
		});
	}
}

package com.ladeit.biz.websocket.events;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
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

	private Map<EventsWebSocket, List<String>> threadServiceIds = new HashMap<>();

	public Map<EventsWebSocket, List<String>> getThreadServiceIds() {
		return threadServiceIds;
	}

	public void receiveMessage(String message) {
		JSONObject originJson = (JSONObject) ((JSONArray) JSONObject.parse(message)).get(1);
		String startTime = originJson.getString("startTime");
		String endTime = originJson.getString("endTime");
		originJson.put("startTime", ((JSONArray) JSONObject.parse(startTime)).get(1).toString());
		originJson.put("endTime", ((JSONArray) JSONObject.parse(endTime)).get(1).toString());
		EventSub eventSub = originJson.toJavaObject(EventSub.class);
		this.threadServiceIds.forEach((ews, list) -> {
			list.stream().filter(id -> id.equals(eventSub.getServiceId())).forEach(id -> {
				ews.sendMessage(eventSub);
			});
		});
	}
}
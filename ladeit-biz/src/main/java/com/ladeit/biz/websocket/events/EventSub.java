package com.ladeit.biz.websocket.events;

import lombok.Data;

import java.util.Date;

/**
 * @program; ladeit-parent
 * @description; EventSub
 * @author; falcomlife
 * @create; 2020/04/27
 * @version; 1.0.0
 */

@Data
public class EventSub {

	private String serviceId;
	private int status;
	private String reason;
	private String type;
	private String note;
	private String kind;
	private String name;
	private String namespace;
	private Date startTime;
	private Date endTime;
	private Date time;
}

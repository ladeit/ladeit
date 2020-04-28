package com.ladeit.biz.websocket.events;

import com.fasterxml.jackson.annotation.JsonFormat;
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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone="GMT+8")
	private Date startTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone="GMT+8")
	private Date endTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone="GMT+8")
	private Date time;
}

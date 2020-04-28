package com.ladeit.pojo.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @program: ladeit
 * @description: Event
 * @author: falcomlife
 * @create: 2020/04/03
 * @version: 1.0.0
 */
@Data
public class Event {
	private String eventUid;
	private String resourceUid;
	private String reason;
	private String type;
	private String message;
	private String kind;
	private String name;
	private String namespace;
	private Date time;
	private Date startTime;
	private Date endTime;
	private String clusterId;
	private String envId;
	private String note;
}

package com.ladeit.pojo.dto.metric.pod;

import lombok.Data;

/**
 * @program: ladeit-parent
 * @description: Occupy
 * @author: falcomlife
 * @create: 2020/05/30
 * @version: 1.0.0
 */
@Data
public class Occupy {
	private String name;
	private Double percentage;
	private long num;
	private String envId;
}

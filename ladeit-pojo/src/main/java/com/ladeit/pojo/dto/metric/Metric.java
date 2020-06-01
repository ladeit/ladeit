package com.ladeit.pojo.dto.metric;

import lombok.Data;

/**
 * @program: ladeit-parent
 * @description: PodMetric
 * @author: falcomlife
 * @create: 2020/05/30
 * @version: 1.0.0
 */
@Data
public class Metric {
	private Metadata metadata;
	private String window;
	private String timestamp;
}

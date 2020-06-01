package com.ladeit.pojo.dto.metric.pod;

import com.ladeit.pojo.dto.metric.Metric;
import lombok.Data;

import java.util.List;

/**
 * @program: ladeit-parent
 * @description: PodMetric
 * @author: falcomlife
 * @create: 2020/05/30
 * @version: 1.0.0
 */
@Data
public class PodMetric extends Metric {
	private List<Container> containers;
}

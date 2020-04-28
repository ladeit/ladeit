package com.ladeit.pojo.ao.topology;

import lombok.Data;

/**
 * @program: ladeit
 * @description: Maps
 * @author: falcomlife
 * @create: 2019/12/19
 * @version: 1.0.0
 */
@Data
public class MatchRouteMap {
	private String matchId;
	private String routeId;
	private String subset;
	private int weight;
}

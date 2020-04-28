package com.ladeit.pojo.ao;

import com.ladeit.pojo.ao.topology.Match;
import com.ladeit.pojo.ao.topology.MatchRouteMap;
import com.ladeit.pojo.ao.topology.Route;
import lombok.Data;

import java.util.List;

/**
 * @program: ladeit
 * @description: TopologyAO
 * @author: falcomlife
 * @create: 2019/11/08
 * @version: 1.0.0
 */
@Data
public class TopologyAO {
	private List<String> host;
	private List<Match> match;
	private List<Route> route;
	private List<MatchRouteMap> map;
}

package com.ladeit.pojo.ao.topology;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @program: ladeit
 * @description: Match
 * @author: falcomlife
 * @create: 2019/12/19
 * @version: 1.0.0
 */
@Data
public class Match {
	private String id;
	private List<Rule> rule;
	private String name;
	private Redirect redirect;
	private Rewrite rewrite;
	private Retries retries;
	private String timeout;
	private Header headers;
	private CorsPolicy corsPolicy;
	private List<String> fault;
	private List<Map<String,Integer>> nameWeight;
}

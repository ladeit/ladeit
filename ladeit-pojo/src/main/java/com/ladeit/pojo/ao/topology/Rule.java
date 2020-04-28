package com.ladeit.pojo.ao.topology;

import lombok.Data;

import java.util.List;

/**
 * @program: ladeit
 * @description: Rule
 * @author: falcomlife
 * @create: 2019/12/19
 * @version: 1.0.0
 */
@Data
public class Rule {
	private String name;
	private List<StringMatch> stringMatch;
}

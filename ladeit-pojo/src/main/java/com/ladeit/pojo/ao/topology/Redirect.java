package com.ladeit.pojo.ao.topology;

import lombok.Data;

/**
 * @program: ladeit
 * @description: Redirect
 * @author: falcomlife
 * @create: 2020/01/08
 * @version: 1.0.0
 */
@Data
public class Redirect {
	private String uri;
	private String authority;
	private int redirectCode;
}

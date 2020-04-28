package com.ladeit.pojo.ao.topology;

import lombok.Data;

import java.util.List;

/**
 * @program: ladeit
 * @description: Header
 * @author: falcomlife
 * @create: 2020/01/08
 * @version: 1.0.0
 */
@Data
public class Header {
	private List<String> set;
	private List<String> add;
	private List<String> remove;
}

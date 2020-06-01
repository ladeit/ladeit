package com.ladeit.pojo.dto.metric;

import lombok.Data;

/**
 * @program: ladeit-parent
 * @description: Metadata
 * @author: falcomlife
 * @create: 2020/05/30
 * @version: 1.0.0
 */
@Data
public class Metadata {
	private String name;
	private String namespace;
	private String creationTimestamp;
	private String selfLink;
}

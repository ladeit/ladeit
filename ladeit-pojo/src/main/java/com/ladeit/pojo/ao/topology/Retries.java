package com.ladeit.pojo.ao.topology;

import lombok.Data;

/**
 * @program: ladeit
 * @description: Retry
 * @author: falcomlife
 * @create: 2020/01/08
 * @version: 1.0.0
 */
@Data
public class Retries {
	private int attempts;
	private long perTryTimeout;
	private String retryOn;
}

package com.ladeit.biz.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;

/**
 * @description: ThreadPool
 * @author: falcomlife
 * @create: 2019/10/16
 * @version: 1.0.0
 */
@Configuration
public class ThreadPool {

	@Bean(name = "threadPoolFactory")
	public ThreadFactory getThreadFactory() {
		return new ThreadFactoryBuilder().setNameFormat("thread-%d").build();
	}
}

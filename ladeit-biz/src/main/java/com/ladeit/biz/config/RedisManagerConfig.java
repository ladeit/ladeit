package com.ladeit.biz.config;

import lombok.Data;
import org.crazycake.shiro.RedisManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @program: ladeit
 * @description: RedisManagerConfig
 * @author: falcomlife
 * @create: 2019/12/12
 * @version: 1.0.0
 */
@Configuration
@Data
public class RedisManagerConfig {

	@Value("${spring.redis.host}")
	private String host;
	@Value("${spring.redis.port}")
	private int port;
	@Value("${spring.redis.password}")
	private String password;
	@Value("${spring.redis.exprie}")
	private String exprie;
	@Value("${spring.redis.timeout}")
	private String timeout;

	@Bean(name = "shiroRedisManager")
	public RedisManager getRedisManager(){
		RedisManager redisManager = new RedisManager();
		redisManager.setHost(host);
		redisManager.setPort(port);
		redisManager.setPassword(password);
		// 配置缓存过期时间秒
		redisManager.setExpire(Integer.valueOf(exprie));
		// 毫秒
		redisManager.setTimeout(Integer.valueOf(timeout));
		return redisManager;
	}
}

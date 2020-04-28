package com.ladeit.biz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: TerminalPool
 * @author: falcomlife
 * @create: 2019/10/16
 * @version: 1.0.0
 */
@Configuration
public class TerminalContentPool {

	@Bean(name = "terminalPool")
	public Map<String, List<Byte>> getPool() {
		Map<String, List<Byte>> pool = new HashMap<>();
		return pool;
	}
}

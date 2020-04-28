package com.ladeit.biz.runner.pods;

import com.ladeit.biz.services.EnvService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.doo.Env;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: ladeit
 * @description: EventConfig
 * @author: falcomlife
 * @create: 2020/04/02
 * @version: 1.0.0
 */
@Slf4j
@Component
public class PodStarter implements CommandLineRunner {

	@Autowired
	private EnvService envService;
	@Autowired
	private PodHandler podHandler;
	@Override
	public void run(String... args) throws Exception {
		ExecuteResult<List<Env>> envRes = this.envService.getAllEnv();
		List<Env> envs = envRes.getResult();
		if (envs != null && !envs.isEmpty()) {
			for (Env env : envs) {
				this.podHandler.put(env.getId(),null);
			}
		}
	}
}

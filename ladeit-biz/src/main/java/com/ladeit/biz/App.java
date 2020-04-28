package com.ladeit.biz;

import com.ladeit.startup.mysql.MariaDBConfiguration;
import com.ladeit.startup.mysql.MysqlConfig;
import com.ladeit.startup.mysql.RedisConfig;
import com.ladeit.startup.mysql.RedisConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Map;
import java.util.Properties;

/**
 * 生产者启动类
 *
 * @version 1.0.0, 19/02/25
 * @author falcomlife
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.ladeit"})
@ServletComponentScan(basePackages = { "com.ladeit.biz.filter","com.ladeit.biz.shiro"})
@EnableAspectJAutoProxy(proxyTargetClass=true,exposeProxy=true)
public class App {

	public static String[] args;
	public static ConfigurableApplicationContext context;
	/**
	 * 生产者启动main方法
	 * @param args
	 */
	public static void main(String[] args) {
		App.args = args;
		//MariaDBConfiguration myConfiguration = new MariaDBConfiguration();
		//Boolean myflag = myConfiguration.startService();
		//RedisConfiguration redisConfiguration  = new RedisConfiguration();
		//Boolean reflag = redisConfiguration.startService();

		Map properties = System.getenv();
		if(properties.get("LADEIT_MYSQL_HOST")==null){
			MysqlConfig mysqlConfig = new MysqlConfig();
			mysqlConfig.startMysql();
		}

		if(properties.get("LADEIT_REDIS_HOST")==null){
			RedisConfig redisConfig = new RedisConfig();
			redisConfig.startRedis();
		}

		App.context = SpringApplication.run(App.class, args);

	}
}
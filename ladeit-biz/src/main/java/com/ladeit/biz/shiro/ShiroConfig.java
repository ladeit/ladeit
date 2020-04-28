package com.ladeit.biz.shiro;

import com.ladeit.biz.config.RedisManagerConfig;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Order(9)
public class ShiroConfig {

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

	/**
	 * SecurityManager,安全管理器,所有与安全相关的操作都会与之进行交互;
	 * 它管理着所有Subject,所有Subject都绑定到SecurityManager,与Subject的所有交互都会委托给SecurityManager
	 * DefaultWebSecurityManager :
	 * 会创建默认的DefaultSubjectDAO(它又会默认创建DefaultSessionStorageEvaluator)
	 * 会默认创建DefaultWebSubjectFactory 会默认创建ModularRealmAuthenticator
	 */
	@Bean
	public DefaultWebSecurityManager securityManager(ShiroRealmConfig shiroRealmConfig, @Value("${spring.redis.host}")
			String host, @Value("${spring.redis.port}") int port, @Value("${spring.redis.password}") String password,
													 @Value("${spring.redis.exprie}") String exprie, @Value("${spring" +
			".redis.timeout}") String timeout) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealms(shiroRealmConfig.allRealm());
		// 自定义session管理 使用redis
		securityManager.setSessionManager(this.sessionManager(host, port, password, exprie, timeout));
		// 自定义缓存实现 使用redis
		securityManager.setCacheManager(this.cacheManager(host, port, password, exprie, timeout));
		return securityManager;
	}

	/**
	 * 配置Shiro的访问策略
	 *
	 * @param securityManager
	 * @return org.apache.shiro.spring.web.ShiroFilterFactoryBean
	 * @author falcomlife
	 * @date 19-11-1
	 * @version 1.0.0
	 */
	@Bean
	public ShiroFilterFactoryBean factory(DefaultWebSecurityManager securityManager) {
		ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
		Map<String, Filter> filterMap = new HashMap<>();
		filterMap.put("jwt", new JwtFilter());
		filterMap.put("lan", new LanFilter());
		factoryBean.setFilters(filterMap);
		factoryBean.setSecurityManager(securityManager);
		Map<String, String> filterRuleMap = new HashMap<>();
		// 所有请求通过JWT Filter
		filterRuleMap.put("/index.html", "anon");
		filterRuleMap.put("/**", "jwt");
		filterRuleMap.put("/**", "lan");
		factoryBean.setFilterChainDefinitionMap(filterRuleMap);
		return factoryBean;
	}

	/**
	 * 添加注解支持
	 *
	 * @param
	 * @return org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator
	 * @author falcomlife
	 * @date 19-11-1
	 * @version 1.0.0
	 */
	@Bean
	@DependsOn("lifecycleBeanPostProcessor")
	@ConditionalOnMissingBean
	public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		// 强制使用cglib，防止重复代理和可能引起代理出错的问题
		defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
		return defaultAdvisorAutoProxyCreator;
	}

	/**
	 * 添加注解依赖
	 *
	 * @param
	 * @return org.apache.shiro.spring.LifecycleBeanPostProcessor
	 * @author falcomlife
	 * @date 19-11-1
	 * @version 1.0.0
	 */
	@Bean
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	/**
	 * 开启注解验证
	 *
	 * @param securityManager
	 * @return
	 */
	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
			DefaultWebSecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor =
				new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}


	/**
	 * 自定义sessionManager
	 *
	 * @param
	 * @return org.apache.shiro.session.mgt.SessionManager
	 * @author falcomlife
	 * @date 19-11-1
	 * @version 1.0.0
	 */
	public SessionManager sessionManager(String host, int port, String password, String exprie, String timeout) {
		// redis
		RedisSessionManager redisSessionManager = new RedisSessionManager();
		redisSessionManager.setSessionDAO(redisSessionDAO(host, port, password, exprie, timeout));
		redisSessionManager.setGlobalSessionTimeout(Long.parseLong(timeout));
		return redisSessionManager;
	}

	/**
	 * 配置shiro redisManager
	 * 使用的是shiro-redis开源插件
	 *
	 * @return
	 * @author falcomlife
	 * @date 19-11-1
	 * @version 1.0.0
	 */
	@Bean
	public RedisManager redisManager(@Value("${spring.redis.host}") String host,
									 @Value("${spring.redis.port}") int port,
									 @Value("${spring.redis.password}") String password, @Value("${spring.redis" +
			".exprie}") String exprie, @Value("${spring.redis.timeout}") String timeout) {
		RedisManager redisManager = new RedisManager();
		redisManager.setHost(host);
		redisManager.setPort(Integer.valueOf(port));
		redisManager.setPassword(password);
		// 配置缓存过期时间秒
		redisManager.setExpire(Integer.valueOf(exprie));
		// 毫秒
		redisManager.setTimeout(Integer.valueOf(timeout));
		return redisManager;
	}

	/**
	 * cacheManager 缓存 redis实现
	 * 使用的是shiro-redis开源插件
	 *
	 * @return org.crazycake.shiro.RedisCacheManager
	 * @author falcomlife
	 * @date 19-11-1
	 * @version 1.0.0
	 */
	@Bean
	public org.crazycake.shiro.RedisCacheManager cacheManager(String host, int port, String password, String exprie,
															  String timeout) {
		org.crazycake.shiro.RedisCacheManager redisCacheManager = new RedisCacheManager();
		redisCacheManager.setRedisManager(this.redisManager(host, port, password, exprie, timeout));
		return redisCacheManager;
	}

	/**
	 * cacheManager 缓存 redis实现
	 * 使用的是shiro-redis开源插件
	 *
	 * @return RedisSessionDAO
	 * @author falcomlife
	 * @date 19-11-1
	 * @version 1.0.0
	 */
	public RedisSessionDAO redisSessionDAO(String host, int port, String password, String exprie, String timeout) {
		RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
		redisSessionDAO.setRedisManager(this.redisManager(host, port, password, exprie, timeout));
		return redisSessionDAO;
	}

	@Bean(name = "shiroThreadLocal")
	public ThreadLocal<String> shiroThreadLocal() {
		ThreadLocal<String> threadLocal = new ThreadLocal<>();
		return threadLocal;
	}

}

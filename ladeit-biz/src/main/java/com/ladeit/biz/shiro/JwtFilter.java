package com.ladeit.biz.shiro;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.ladeit.biz.config.SpringBean;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.UserInfo;
import com.ladeit.pojo.doo.User;
import com.ladeit.util.git.TokenUtil;
import com.ladeit.util.redis.RedisUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.crazycake.shiro.RedisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


/**
 * Created by EalenXie on 2018/11/26 10:26. JWT核心过滤器配置
 * 所有的请求都会先经过Filter，所以我们继承官方的BasicHttpAuthenticationFilter，并且重写鉴权的方法。 执行流程
 * preHandle->isAccessAllowed->isLoginAttempt->executeLogin
 */
@Slf4j
@Component
@WebFilter(filterName = "JwtFilter", urlPatterns = "/**")
public class JwtFilter extends BasicHttpAuthenticationFilter {

	@Autowired
	private RedisUtil redisUtil;
	@Resource(name = "redisManager")
	private RedisManager redisManager;
	@Value("#{'${filter.url-socket}'.split(',')}")
	private List<String> urlSocket;

	/**
	 * 判断用户是否想要进行 需要验证的操作 检测header里面是否包含Authorization字段即可
	 */
	@Override
	protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
		String auth = getAuthzHeader(request);
		return auth != null && !auth.equals("");
	}

	/**
	 * 此方法调用登陆，验证逻辑
	 */
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		HttpServletRequest req = (HttpServletRequest) request;
		String url = req.getRequestURI();
		String method = req.getMethod();
		// option请求不需要验证
		if ("OPTIONS".equals(method)) {
			return Boolean.TRUE;
		}
		boolean isSocket = url.contains("/api/v1/terminal/socket");
		// 匹配url规则，放过不需要验证的请求
		if(!url.startsWith("/api/v1")){
			return Boolean.TRUE;
		}
		if ("/api/v1/user".equals(url) || "/api/v1/user/admin".equals(url) || "/api/v1/user/admin/password".equals(url) || url.equals("/api/v1/service/image") || url.contains("/api/v1/slack") || url.contains("/api/v1/event") || url.startsWith("/api/v1/log")) {
			return Boolean.TRUE;
		}
		// 如果是websocket，放过
		if (isSocket) {
			((HttpServletResponse) response).addHeader("sec-webSocket-protocol",
					req.getHeader("sec-webSocket-protocol"));
			return Boolean.TRUE;
		}
		if (!isLoginAttempt(request, response)) {
			return Boolean.FALSE;
		}
		String token = getAuthzHeader(request);
		Claims claims = null;
		try {
			claims = TokenUtil.parseJWT(token);
		} catch (RuntimeException e) {
			return Boolean.FALSE;
		}
		String userId = (String) claims.get("userId");
		this.redisUtil = (RedisUtil) SpringBean.getBean("redisUtil");
		this.redisManager = (RedisManager) SpringBean.getBean("redisManager");
		String sessionId = req.getHeader("sessionId");
		byte[] session = redisManager.get(("shiro_redis_session:" + sessionId).getBytes());
		try {
			if (session == null || session.length == 0) {
				// 先判断session信息有没有过期
				// Session expired, please login again.
				this.returnResponse(response, Code.SESSION_TIMEOUT, "身份信息失效，请重新登录。");
				return Boolean.FALSE;
			} else {
				String alreadyToken = (String) this.redisUtil.getPT(userId);
				if (StringUtils.isBlank(alreadyToken)) {
					// 判断登录信息是否过期，token是否过期
					// Session expired, please login again.
					this.returnResponse(response, Code.TOKEN_TIMEOUT, "登录信息失效，请重新登录。");
					return Boolean.FALSE;
				} else if (!alreadyToken.equals(token)) {
					// 验证token是否正确
					// Session expired, please login again.
					this.returnResponse(response, Code.TOKEN_TIMEOUT, "登录信息失效，请重新登录。");
					return Boolean.FALSE;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Boolean.TRUE;
	}

	private void returnResponse(ServletResponse response, int code, String message) throws IOException {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=utf-8");
		PrintWriter out = response.getWriter();
		ExecuteResult<String> result = new ExecuteResult<>();
		result.setCode(code);
		result.addErrorMessage(message);
		String res = JSONObject.toJSONString(result);
		out.append(res);
		out.flush();
		out.close();
	}
}
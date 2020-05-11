package com.ladeit.biz.shiro;

import com.ladeit.pojo.doo.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @program: ladeit
 * @description: LanInterceptor
 * @author: falcomlife
 * @create: 2020/04/07
 * @version: 1.0.0
 */
@Component
@WebFilter(filterName = "lanFilter", urlPatterns = "/**")
public class LanFilter extends BasicHttpAuthenticationFilter {
	// 目标方法执行之前
	@Override
	protected boolean isAccessAllowed(ServletRequest request,
									  ServletResponse response, Object mappedValue) {
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
		if ("/api/v1/user".equals(url) || url.equals("/api/v1/service/image") || url.contains("/api/v1/slack") || url.contains("/api/v1/event") || url.startsWith("/api/v1/log")) {
			return Boolean.TRUE;
		}
		// 如果是websocket，放过
		if (isSocket) {
			((HttpServletResponse) response).addHeader("sec-webSocket-protocol",
					req.getHeader("sec-webSocket-protocol"));
			return Boolean.TRUE;
		}
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		String lan = req.getHeader("lan");
		if (user.getLan() != null && !user.getLan().equals(lan)) {
			user.setLan(lan);
			PrincipalCollection principalCollection = SecurityUtils.getSubject().getPrincipals();
			String realmName = principalCollection.getRealmNames().iterator().next();
			PrincipalCollection newPrincipalCollection =
					new SimplePrincipalCollection(user, realmName);
			SecurityUtils.getSubject().runAs(newPrincipalCollection);
		}
		return true;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) {
		return Boolean.FALSE;
	}
}

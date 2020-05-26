package com.ladeit.biz.shiro;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ladeit.biz.services.UserService;
import com.ladeit.pojo.doo.User;
import com.ladeit.util.auth.PasswordUtil;
import com.ladeit.util.git.TokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ShiroRealmConfig {

	public static final String realmName = "mysql-redis";

	@Autowired
	private UserService userService;

	/**
	 * 配置所有自定义的realm,方便起见,应对可能有多个realm的情况
	 */
	public List<Realm> allRealm() {
		List<Realm> realmList = new LinkedList<>();
		AuthorizingRealm jwtRealm = jwtRealm();
		realmList.add(jwtRealm);
		return Collections.unmodifiableList(realmList);
	}

	/**
	 * 自定义 JWT的 Realm 重写 Realm 的 supports() 方法是通过 JWT 进行登录判断的关键
	 */
	private AuthorizingRealm jwtRealm() {
		AuthorizingRealm jwtRealm = new AuthorizingRealm() {
			/**
			 * 注意坑点 : 必须重写此方法，不然Shiro会报错 因为创建了 JWTToken 用于替换Shiro原生
			 * token,所以必须在此方法中显式的进行替换，否则在进行判断时会一直失败
			 */
			@Override
			public boolean supports(AuthenticationToken token) {
				return token instanceof UsernamePasswordToken;
			}

			@Override
			protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
				log.info("auth");
				Subject subject = SecurityUtils.getSubject();
				SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
				String userId = (String) subject.getSession().getAttribute("id");
				//Role role = roleService.getRoleByUserId(userId);
				Set<String> roles = new HashSet<>();
//				roles.add(role);
				roles.add("admin");
				authorizationInfo.setRoles(roles);
				return authorizationInfo;
			}

			/**
			 * 登录验证
			 * @author falcomlife
			 * @date 19-10-30
			 * @version 1.0.0
			 * @return org.apache.shiro.authc.AuthenticationInfo
			 * @param token
			 */
			@Override
			protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
				SimpleAccountRealm simpleAccount = new SimpleAccountRealm();
				log.info("login auth check");
				String username = (String) token.getPrincipal();
				String lan = username.split(",")[1];
				username = username.split(",")[0];
				String password = new String((char[]) token.getCredentials());
				User user = userService.getUserByUsername(username);
				if (user == null) {
					throw new UnknownAccountException("user not found");
				} else if (StringUtils.isBlank(user.getPassword())) {
					throw new UnknownAccountException("ladeit-bot can't login");
				}
				user.setLan(lan); // zh_CN en_US
				boolean flag = false;
				try {
					//slack先实现功能，登录校验方案待定
					if (user == null) {
						return null;
					} else if ((user.getUsername() + user.getId()).equals(password)) {
						flag = true;
					} else {
						flag = PasswordUtil.decode(password, user.getSalt(), user.getPassword());
					}
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					return null;
				}
				if (flag) {
					return new SimpleAuthenticationInfo(user, user.getPassword(), ShiroRealmConfig.realmName);
				}
				return null;
			}
		};
		jwtRealm.setCredentialsMatcher(credentialsMatcher());
		return jwtRealm;
	}

	/**
	 * 注意坑点 : 密码校验 , 这里因为是JWT形式,就无需密码校验和加密,直接让其返回为true(如果不设置的话,该值默认为false,即始终验证不通过)
	 */
	private CredentialsMatcher credentialsMatcher() {
		return (token, info) -> true;
	}

	/**
	 * 将accessleavl转换为role
	 *
	 * @param accessLevel
	 * @return
	 */
	private String levelToRoles(String accessLevel) {
		String role = null;
		if (accessLevel.equals("60")) {
			role = "ADMIN";
		}
		if (accessLevel.equals("50")) {
			role = "OWNER";
		}
		if (accessLevel.equals("40")) {
			role = "MAINTAINER";
		}
		if (accessLevel.equals("30")) {
			role = "DEVELOPER";
		}
		if (accessLevel.equals("20")) {
			role = "REPORTER";
		}
		if (accessLevel.equals("10")) {
			role = "GUEST";
		}
		return role;
	}
}
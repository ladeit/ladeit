package com.ladeit.biz.controller;

import com.ladeit.biz.services.UserService;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.UserInfo;
import com.ladeit.pojo.ao.OperationAO;
import com.ladeit.pojo.ao.UserAO;
import com.ladeit.pojo.ao.UserSlackRelationAO;
import com.ladeit.pojo.doo.User;
import com.ladeit.util.ExecuteResultUtil;
import com.ladeit.util.git.TokenUtil;
import com.ladeit.util.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.PUT;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: ladeit
 * @description: AuthController
 * @author: falcomlife
 * @create: 2019/10/29
 * @version: 1.0.0
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/user")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private MessageUtils messageUtils;

	/**
	 * 用户注册
	 *
	 * @param userAO
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.UserAO>
	 * @author falcomlife
	 * @date 19-10-30
	 * @version 1.0.0
	 */
	@PostMapping
	public ExecuteResult<UserAO> checkIn(@RequestBody UserAO userAO) throws NoSuchAlgorithmException {
		ExecuteResult<UserAO> result = new ExecuteResult<UserAO>();
		User user = new User();
		BeanUtils.copyProperties(userAO, user);
		ExecuteResult<User> resultdo = this.userService.checkIn(user);
		new ExecuteResultUtil<User>().copyInfoSourceToTarget(resultdo, result);
		result.setCode(resultdo.getCode());
		return result;
	}

	/**
	 * 登录方法
	 *
	 * @param username
	 * @param password
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.UserAO>
	 * @author falcomlife
	 * @date 19-10-31
	 * @version 1.0.0
	 */
	@GetMapping
	public ExecuteResult<UserInfo> login(@RequestParam String username, @RequestParam String password,
										 HttpServletResponse res, HttpServletRequest req) {
		ExecuteResult<UserInfo> result = new ExecuteResult<>();
		UserInfo userInfo = new UserInfo();
		UsernamePasswordToken shiroToken = new UsernamePasswordToken();
		String lan = req.getHeader("lan");
		username = username + "," + lan;
		shiroToken.setUsername(username);
		shiroToken.setPassword(password.toCharArray());
		Subject subject = SecurityUtils.getSubject();
		try {
			// 执行shiro登录验证
			subject.login(shiroToken);
			User user = (User) subject.getPrincipal();
			BeanUtils.copyProperties(user, userInfo);
			result.setResult(userInfo);
			// redis保存登录信息
			// 先查询redis里面有没有保存相应的token信息
			Object alreadyToken = this.redisUtil.getPT(user.getId());
			if (alreadyToken == null) {
				Map map = new HashMap<String, String>();
				map.put("userId", user.getId());
				String token = TokenUtil.createToken(map);
				userInfo.setToken(token);
				this.redisUtil.setPT(user.getId(), token);
			} else {
				try {
					TokenUtil.parseJWT(alreadyToken.toString());
					userInfo.setToken(alreadyToken.toString());
				} catch (Exception e) {
					Map map = new HashMap<String, String>();
					map.put("userId", user.getId());
					String token = TokenUtil.createToken(map);
					this.redisUtil.setPT(user.getId(), token);
					userInfo.setToken(token);
				}
			}
			userInfo.setSession(subject.getSession().getId().toString());

		} catch (UnknownAccountException e) {
			if ("en-US".equals(lan)) {
				result.addWarningMessage("Invalid username or password");
			} else {
				result.addWarningMessage("用户名或密码错误");
			}
			result.setCode(Code.NOUSER_FAILPASSWORD);
			return result;
		}

		return result;
	}

	/**
	 * 查询当前登录人活动列表
	 *
	 * @param currentPage, pageSize
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.OperationAO>>
	 * @date 2019/11/29
	 * @ahthor MddandPyy
	 */
	@GetMapping("/getOperations")
	public ExecuteResult<Pager<OperationAO>> queryOperationList(@RequestParam("currentPage") int currentPage,
																@RequestParam("pageSize") int pageSize, @RequestParam(
			"UserName") String userName) {
		return userService.queryOperationList(currentPage, pageSize, userName);
	}

	/**
	 * 更新密码
	 *
	 * @param userAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-16
	 * @version 1.0.0
	 */
	@PutMapping("/password")
	public ExecuteResult<String> updatePassword(@RequestBody UserAO userAO) throws NoSuchAlgorithmException {
		User user = new User();
		BeanUtils.copyProperties(userAO, user);
		return userService.updatePassword(user, userAO.getNewPassword());
	}

	/**
	 * 更新用户信息
	 *
	 * @param userAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	@PutMapping("/User")
	public ExecuteResult<UserAO> updateUser(@RequestBody UserAO userAO) {
		return userService.updateUser(userAO);
	}

	/**
	 * 获取用户信息
	 *
	 * @param userName
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.UserAO>
	 * @date 2020/3/12
	 * @ahthor MddandPyy
	 */
	@GetMapping("/User")
	public ExecuteResult<UserAO> getUser(@RequestParam("UserName") String userName) {
		return userService.getUser(userName);
	}

	/**
	 * 删除用户信息
	 *
	 * @param userAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	@DeleteMapping("/User")
	public ExecuteResult<String> deleteUser(@RequestBody UserAO userAO) {
		return userService.deleteUser(userAO);
	}


	/**
	 * 查询ladeit账户绑定的slack账户信息
	 *
	 * @param userId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.UserSlackRelationAO>
	 * @date 2020/1/14
	 * @ahthor MddandPyy
	 */
	@GetMapping("/getSlackUser")
	public ExecuteResult<UserSlackRelationAO> getSlackUser(@RequestParam("userId") String userId) {
		return userService.getSlackUser(userId);
	}

	/**
	 * 解绑slack用户和ladeit
	 *
	 * @param userSlackRelationId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/1/14
	 * @ahthor MddandPyy
	 */
	@DeleteMapping("/unbind/{userSlackRelationId}")
	public ExecuteResult<String> unbindSlackUser(@PathVariable("userSlackRelationId") String userSlackRelationId) {
		return userService.unbindSlackUser(userSlackRelationId);
	}

	/**
	* 判断admin是否已经修改过密码
	* @author falcomlife
	* @date 20-5-26
	* @version 1.0.0
	* @return com.ladeit.common.ExecuteResult<java.lang.String>
	* @param
	*/
	@GetMapping("/admin")
	public ExecuteResult<String> isFirst(){
		return this.userService.isFirst();
	}
}

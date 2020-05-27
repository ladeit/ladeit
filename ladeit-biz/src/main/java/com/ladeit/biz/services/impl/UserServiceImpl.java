package com.ladeit.biz.services.impl;

import com.ladeit.biz.dao.OperationDao;
import com.ladeit.biz.dao.UserDao;
import com.ladeit.biz.dao.UserSlackRelationDao;
import com.ladeit.biz.services.UserService;
import com.ladeit.biz.utils.MessageUtils;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.OperationAO;
import com.ladeit.pojo.ao.UserAO;
import com.ladeit.pojo.ao.UserSlackRelationAO;
import com.ladeit.pojo.doo.Operation;
import com.ladeit.pojo.doo.User;
import com.ladeit.pojo.doo.UserSlackRelation;
import com.ladeit.util.ListUtil;
import com.ladeit.util.auth.PasswordUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @program: ladeit
 * @description: UserServiceImpl
 * @author: falcomlife
 * @create: 2019/10/30
 * @version: 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;


	@Autowired
	private OperationDao operationDao;

	@Autowired
	private UserSlackRelationDao userSlackRelationDao;

	@Autowired
	private MessageUtils messageUtils;


	/**
	 * 注册
	 *
	 * @param user
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.User>
	 * @author falcomlife
	 * @date 19-10-30
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<User> checkIn(User user) throws NoSuchAlgorithmException {
		ExecuteResult<User> result = new ExecuteResult<>();
		//用户名唯一校验
		String username = user.getUsername();
		User u = this.userDao.getUserByUsername(username);
		if (u != null) {
			String message = messageUtils.matchMessage("M0029", new Object[]{}, Boolean.TRUE);
			result.addWarningMessage(message);
			result.setCode(Code.USER_EXIT);
		} else {
			user.setId(UUID.randomUUID().toString());
			user.setCreateAt(new Date());
			String[] password = PasswordUtil.encode(user.getPassword());
			user.setSalt(password[0]);
			user.setPassword(password[1]);
			user.setIsdel(false);
			result.setResult(user);
			this.userDao.insert(user);
		}
		return result;
	}

	/**
	 * 根绝用户名搜索用户
	 *
	 * @param username
	 * @return com.ladeit.pojo.doo.User
	 * @author falcomlife
	 * @date 19-10-31
	 * @version 1.0.0
	 */
	@Override
	public User getUserByUsername(String username) {
		return this.userDao.getUserByUsername(username);
	}


	/**
	 * 查询当前登录人活动列表
	 *
	 * @param currentPage, pageSize
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager                                                               <                                                               com.ladeit.pojo.ao.OperationAO>>
	 * @date 2019/11/29
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<Pager<OperationAO>> queryOperationList(int currentPage, int pageSize, String userName) {
		//User user = (User) SecurityUtils.getSubject().getPrincipal();
		User user = userDao.getUserByUsername(userName);
		ExecuteResult<Pager<OperationAO>> result = new ExecuteResult<>();
		Pager<OperationAO> pager = new Pager<>();
		pager.setPageNum(currentPage);
		pager.setPageSize(pageSize);
		List<Operation> operList = operationDao.queryOperListByUserId(currentPage, pageSize, user.getId());
		int userCount = operationDao.queryOperCoundByUserId(user.getId());
		List<OperationAO> resultList = new ListUtil<Operation, OperationAO>().copyList(operList,
				OperationAO.class);
		pager.setRecords(resultList);
		pager.setTotalRecord(userCount);
		result.setResult(pager);
		return result;
	}

	/**
	 * 更新用户信息
	 *
	 * @param userAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<UserAO> updateUser(UserAO userAO) {
		ExecuteResult<UserAO> result = new ExecuteResult<>();
		// 判断邮箱是否重复
		boolean isExistEmail = userDao.isExistEmail(userAO.getEmail(), userAO.getId());
		if (isExistEmail) {
			result.setCode(Code.FAILED);
			String message = messageUtils.matchMessage("M0030", new Object[]{}, Boolean.TRUE);
			result.addErrorMessage(message);
			return result;
		}
		User user = userDao.getUserById(userAO.getId());
		user.setEmail(userAO.getEmail());
		user.setNickName(userAO.getNickName());
		userDao.update(user);

		BeanUtils.copyProperties(user, userAO);
		userAO.setPassword("");
		userAO.setSalt("");

		result.setResult(userAO);
		return result;
	}

	/**
	 * 获取用户信息
	 *
	 * @param userName
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.UserAO>
	 * @date 2020/3/12
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<UserAO> getUser(String userName) {
		ExecuteResult<UserAO> result = new ExecuteResult<>();
		User user = userDao.getUserByUsername(userName);
		if (user != null) {
			UserAO userAO = new UserAO();
			BeanUtils.copyProperties(user, userAO);
			userAO.setPassword("");
			userAO.setSalt("");
			userAO.setIsdel(null);
			result.setResult(userAO);
		}
		return result;
	}


	/**
	 * 删除用户信息
	 *
	 * @param userAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> deleteUser(UserAO userAO) {
		ExecuteResult<String> result = new ExecuteResult<>();
		User user = userDao.getUserById(userAO.getId());
		user.setIsdel(true);
		userDao.update(user);
		String message = messageUtils.matchMessage("M0100", new Object[]{}, Boolean.TRUE);
		result.setResult(message);

		return result;
	}

	/**
	 * 查询ladeit账户绑定的slack账户信息
	 *
	 * @param userId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.UserSlackRelationAO>
	 * @date 2020/1/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<UserSlackRelationAO> getSlackUser(String userId) {
		ExecuteResult<UserSlackRelationAO> result = new ExecuteResult<UserSlackRelationAO>();
		UserSlackRelation userSlackRelation = userSlackRelationDao.queryUserSlackRelationByUserId(userId);
		if (userSlackRelation != null) {
			UserSlackRelationAO userSlackRelationAO = new UserSlackRelationAO();
			BeanUtils.copyProperties(userSlackRelation, userSlackRelationAO);
			result.setResult(userSlackRelationAO);
		} else {
			result.setResult(null);
		}

		return result;
	}

	/**
	 * 解绑slack用户和ladeit
	 *
	 * @param userSlackRelationId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/1/14
	 * @ahthor MddandPyy
	 */
	@Override
	public ExecuteResult<String> unbindSlackUser(String userSlackRelationId) {
		ExecuteResult<String> result = new ExecuteResult<String>();
		UserSlackRelation userSlackRelation = userSlackRelationDao.queryUserSlackRelationById(userSlackRelationId);
		userSlackRelationDao.delete(userSlackRelation);
		String message = messageUtils.matchMessage("M0025", new Object[]{}, Boolean.TRUE);
		result.setResult(message);

		return result;
	}

	/**
	 * 通过用户Id查询用户
	 *
	 * @param userid
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.User>
	 * @author falcomlife
	 * @date 20-4-17
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<User> getUserBySlackId(String userid) {
		ExecuteResult result = new ExecuteResult();
		result.setResult(this.userDao.getUserBySlackId(userid));
		return result;
	}

	/**
	 * 修改密码
	 *
	 * @param user
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-16
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> updatePassword(User user, String newPassword) throws NoSuchAlgorithmException {
		ExecuteResult<String> result = new ExecuteResult<>();
		User userInDatabase = this.getUserByUsername(user.getUsername());
		boolean flag = PasswordUtil.decode(user.getPassword(), userInDatabase.getSalt(), userInDatabase.getPassword());
		if (flag) {
			String[] password = PasswordUtil.encode(newPassword);
			user.setSalt(password[0]);
			user.setPassword(password[1]);
			this.userDao.update(user);
		} else {
			String message = messageUtils.matchMessage("M0032", new Object[]{}, Boolean.TRUE);
			result.setResult(message);
		}
		return result;
	}

	/**
	 * 判断admin是否修改过密码
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-26
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> isFirst() {
		ExecuteResult<String> result = new ExecuteResult<>();
		User user = this.userDao.getUserByUsername("admin");
		if (user.getPassword() != null) {
			result.setResult("havepassword");
		} else {
			result.setResult("nopassword");
		}
		return result;
	}

	/**
	 * 更新admin密码
	 *
	 * @param user
	 * @param newPassword
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-26
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<String> updateAdminPassword(User user, String newPassword) throws NoSuchAlgorithmException {
		ExecuteResult<String> result = new ExecuteResult<>();
		User userInDatabase = this.getUserByUsername("admin");
		String[] password = PasswordUtil.encode(newPassword);
		userInDatabase.setSalt(password[0]);
		userInDatabase.setPassword(password[1]);
		this.userDao.updateAdminPassword(userInDatabase);
		return result;
	}
}

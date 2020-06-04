package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.OperationAO;
import com.ladeit.pojo.ao.UserAO;
import com.ladeit.pojo.ao.UserSlackRelationAO;
import com.ladeit.pojo.doo.User;

import java.security.NoSuchAlgorithmException;

/**
 * @program: ladeit
 * @description: UserService
 * @author: falcomlife
 * @create: 2019/10/30
 * @version: 1.0.0
 */
public interface UserService {

	/**
	 * 注册
	 *
	 * @param user
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.User>
	 * @author falcomlife
	 * @date 19-10-30
	 * @version 1.0.0
	 */
	ExecuteResult<User> checkIn(User user) throws NoSuchAlgorithmException;

	/**
	 * 根据用户名搜索用户
	 *
	 * @param username
	 * @return com.ladeit.pojo.doo.User
	 * @author falcomlife
	 * @date 19-10-31
	 * @version 1.0.0
	 */
	User getUserByUsername(String username);

	/**
	 * 查询当前登录人活动列表
	 *
	 * @param currentPage, pageSize
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.OperationAO>>
	 * @date 2019/11/29
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<OperationAO>> queryOperationList(int currentPage, int pageSize, String userName);

	/**
	 * 更新用户信息
	 *
	 * @param userAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	ExecuteResult<UserAO> updateUser(UserAO userAO);

	/**
	 * 获取用户信息
	 *
	 * @param userName
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.UserAO>
	 * @date 2020/3/12
	 * @ahthor MddandPyy
	 */
	ExecuteResult<UserAO> getUser(String userName);


	/**
	 * 删除用户信息
	 *
	 * @param userAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> deleteUser(UserAO userAO);

	/**
	 * 查询ladeit账户绑定的slack账户信息
	 *
	 * @param userId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.UserSlackRelationAO>
	 * @date 2020/1/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<UserSlackRelationAO> getSlackUser(String userId);


	/**
	 * 解绑slack用户和ladeit
	 *
	 * @param userSlackRelationId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/1/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> unbindSlackUser(String userSlackRelationId);

	/**
	 * 通过用户Id查询用户
	 *
	 * @param userid
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.User>
	 * @author falcomlife
	 * @date 20-4-17
	 * @version 1.0.0
	 */
	ExecuteResult<User> getUserBySlackId(String userid);

	/**
	 * 修改密码
	 *
	 * @param user
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-16
	 * @version 1.0.0
	 */
	ExecuteResult<String> updatePassword(User user, String newPassword) throws NoSuchAlgorithmException;

	/**
	 * 判断admin是否修改过密码
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-26
	 * @version 1.0.0
	 */
	ExecuteResult<String> isFirst();

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
	ExecuteResult<String> updateAdminPassword(User user, String newPassword) throws NoSuchAlgorithmException;

	/**
	 * 判断密码是否相同
	 *
	 * @param userId
	 * @param password
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-6-4
	 * @version 1.0.0
	 */
	ExecuteResult<String> getPasswordIsSame(String userId, String password) throws NoSuchAlgorithmException;
}

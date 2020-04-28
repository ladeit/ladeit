package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.User;
import io.ebean.SqlRow;

import java.util.List;

/**
 * @program: ladeit
 * @description: UserDao
 * @author: falcomlife
 * @create: 2019/10/30
 * @version: 1.0.0
 */
public interface UserDao {

	/**
	 * 插入数据
	 *
	 * @param user
	 * @return void
	 * @author falcomlife
	 * @date 19-10-30
	 * @version 1.0.0
	 */
	void insert(User user);

	/**
	 * 更新用户
	 *
	 * @param user
	 * @return void
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	void update(User user);

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
	 * 根据id查询用户
	 *
	 * @param userId
	 * @return com.ladeit.pojo.doo.User
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	User getUserById(String userId);

	/**
	 * 根据邮箱(唯一)搜索用户
	 *
	 * @param email
	 * @return com.ladeit.pojo.doo.User
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	List<SqlRow> getUserByUserNameOrEmail(String username, String email);

	boolean isExistEmail(String email, String userId);

	/**
	 * 通过slackid查询用户
	 *
	 * @param userid
	 * @return java.lang.Object
	 * @author falcomlife
	 * @date 20-4-17
	 * @version 1.0.0
	 */
	Object getUserBySlackId(String userid);
}

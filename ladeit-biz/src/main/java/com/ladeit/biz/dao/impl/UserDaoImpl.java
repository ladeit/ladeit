package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.UserDao;
import com.ladeit.pojo.doo.User;
import com.ladeit.pojo.doo.UserSlackRelation;
import io.ebean.EbeanServer;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: ladeit
 * @description: UserDao
 * @author: falcomlife
 * @create: 2019/10/30
 * @version: 1.0.0
 */
@Repository
public class UserDaoImpl implements UserDao {


	@Autowired
	private EbeanServer server;

	/**
	 * 插入数据
	 *
	 * @param user
	 * @return void
	 * @author falcomlife
	 * @date 19-10-30
	 * @version 1.0.0
	 */
	@Override
	public void insert(User user) {
		this.server.insert(user);
	}

	@Override
	public void update(User user) {
		this.server.update(user);
	}

	/**
	 * 根据用户名搜索用户
	 *
	 * @param username
	 * @return com.ladeit.pojo.doo.User
	 * @author falcomlife
	 * @date 19-10-31
	 * @version 1.0.0
	 */
	@Override
	public User getUserByUsername(String username) {
		return this.server.createQuery(User.class).where().eq("username", username).eq("isdel", false).findOne();
	}

	@Override
	public User getUserById(String userId) {
		return this.server.createQuery(User.class).where().eq("id", userId).findOne();
	}

	@Override
	public List<SqlRow> getUserByUserNameOrEmail(String userName, String email) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("select * from user where isdel =0");
		if (!(userName == null || userName.trim().length() == 0)) {
			sbf.append(" and (username like :username or nick_name like :nickname ) ");
		}
		if (!(email == null || email.trim().length() == 0)) {
			sbf.append(" and email like :email ");
		}
		SqlQuery sqlQuery = server.createSqlQuery(sbf.toString());
		if (!(userName == null || userName.trim().length() == 0)) {
			sqlQuery.setParameter("username", "%" + userName + "%").setParameter("nickname", "%" + userName + "%");
		}
		if (!(email == null || email.trim().length() == 0)) {
			sqlQuery.setParameter("email", "%" + email + "%");
		}
		return sqlQuery.findList();
	}

	@Override
	public boolean isExistEmail(String email, String userId) {
		boolean exists = server.find(User.class).where().eq("email", email).and().ne("id", userId).exists();
		if (exists) {
			return true;
		}
		return false;
	}

	/**
	 * 通过slackId查询用户
	 *
	 * @param userid
	 * @return java.lang.Object
	 * @author falcomlife
	 * @date 20-4-17
	 * @version 1.0.0
	 */
	@Override
	public Object getUserBySlackId(String userid) {
		UserSlackRelation userSlackRelation = this.server.createQuery(UserSlackRelation.class).where().eq("slack_user_id",userid).findOne();
		return this.server.createQuery(User.class).where().idEq(userSlackRelation.getUserId()).findOne();
	}
}

package com.ladeit.biz.shiro;

import com.ladeit.biz.config.SpringBean;
import com.ladeit.biz.services.UserService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Token.UserInfo;
import com.ladeit.common.Token.UserInfoUtil;
import com.ladeit.pojo.doo.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;

/**
 * @program: ladeit
 * @description: SecurityUtils
 * @author: falcomlife
 * @create: 2020/04/17
 * @version: 1.0.0
 */
public class SecurityUtils extends org.apache.shiro.SecurityUtils {

	public static Subject getSubject() {
		Subject subject = org.apache.shiro.SecurityUtils.getSubject();
		Object o = subject.getPrincipal();
		if (o == null) {
			UserInfo u = UserInfoUtil.getInfo();
			if (u == null) {
				return null;
			} else {
				String userid = u.getSlackUserId();
				if (StringUtils.isNotBlank(userid)) {
					UserService userService = SpringBean.getObject(UserService.class);
					ExecuteResult<User> userRes = userService.getUserBySlackId(userid);
					if (userRes.getResult() != null) {
						User user = userRes.getResult();
						SlackSubject ss = new SlackSubject();
						ss.setPrincipal(user);
						return ss;
					} else {
						return null;
					}
				} else {
					return null;
				}
			}

		} else {
			return subject;
		}
	}
}

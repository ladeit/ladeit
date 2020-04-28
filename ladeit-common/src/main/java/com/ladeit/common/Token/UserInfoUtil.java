package com.ladeit.common.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: UserInfoPool
 * @author: falcomlife
 * @create: 2019/07/15
 * @version: 1.0.0
 */
public class UserInfoUtil {

	private static ThreadLocal<UserInfo> userInfoThreadLocal = new ThreadLocal<>();
	private static Map<String,UserInfo> map = new HashMap<>();
	public static void setInfo(UserInfo userInfo) {

		map.put(Thread.currentThread().getThreadGroup().getName()+","+Thread.currentThread().getName()+","+Thread.currentThread().getPriority()+","+Thread.currentThread().getId(),userInfo);
	}

	public static UserInfo getInfo() {
		return map.get(Thread.currentThread().getThreadGroup().getName()+","+Thread.currentThread().getName()+","+Thread.currentThread().getPriority()+","+Thread.currentThread().getId());
	}

	public static void remove(){
	    map.remove(Thread.currentThread().getThreadGroup().getName()+","+Thread.currentThread().getName()+","+Thread.currentThread().getPriority()+","+Thread.currentThread().getId());
    }
}

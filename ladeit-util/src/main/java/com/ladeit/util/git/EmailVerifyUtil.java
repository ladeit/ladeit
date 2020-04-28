package com.ladeit.util.git;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailVerifyUtil {
	/**
	 * 判断是否是邮箱
	 * 
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {
		if (null == email || "".equals(email)) {
			return false;
		}
		String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern p = Pattern.compile(regEx1);
		Matcher m = p.matcher(email);
		if (m.matches()) {
			return true;
		}
		return false;
	}
	public static void main(String[] args) {
		System.out.println(isEmail("lgybean@gmail.com"));
	}
}

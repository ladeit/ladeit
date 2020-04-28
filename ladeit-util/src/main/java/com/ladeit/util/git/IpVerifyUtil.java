package com.ladeit.util.git;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpVerifyUtil {
	/**
	 * 判断是否是ip
	 * @param ip
	 * @return
	 */
	public static boolean isIP(String url) {
		Pattern pattern = Pattern.compile("((1[0-9][0-9]\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)|([1-9][0-9]\\.)|([0-9]\\.)){3}((1[0-9][0-9])|(2[0-4][0-9])|(25[0-5])|([1-9][0-9])|([0-9]))");
		if(url.startsWith("http://")) {
			//切分处理获取ip
			String ip = url.split("/")[2].split(":")[0];
			Matcher matcher = pattern.matcher(ip);
			return matcher.find();
		}else {
			//切分处理获取ip
			String ip = url.split("/")[0].split(":")[0];
			Matcher matcher = pattern.matcher(ip);
			return matcher.find();
		}
	}
}

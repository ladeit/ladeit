package com.ladeit.biz.utils;

import com.ladeit.pojo.doo.User;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * @author MddAndPyy
 * @version V1.0
 * @Classname MessageUtils
 * @Date 2020/4/7 14:53
 */
@Service
public class MessageUtils {

	private static final String ZH_PROPERTIES = "message.properties";
	//private static final String EN_PROPERTIES = "message.properties";

	private static Properties p_zh = new Properties();
	private static Properties p_en = new Properties();

	static {
		try {
			p_zh.load(ClassLoader.getSystemResourceAsStream(ZH_PROPERTIES));
			//p_en.load(ClassLoader.getSystemResourceAsStream(EN_PROPERTIES));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String matchMessage(String MessageCode, Object[] params, boolean isUser) {
		String language = null;
		if (isUser) {
			User user = (User) SecurityUtils.getSubject().getPrincipal();
			language = user.getLan();
		} else {
			language = "en-US";
		}
		String message = null;
		String pattern = null;
		String property = (String) p_zh.get(MessageCode);
		if (property != null && property.length() != 0) {
			String[] ps = property.split("\\|en\\|");
			if ("zh-CN".equals(language)) {
				pattern = ps[0];
				message = MessageFormat.format(pattern, params);
			} else if ("en-US".equals(language)) {
				pattern = ps[1];
				message = MessageFormat.format(pattern, params);
			} else {
			}
		}
		return message;
	}

}

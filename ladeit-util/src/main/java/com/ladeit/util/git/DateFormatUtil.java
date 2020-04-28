package com.ladeit.util.git;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {
	private static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * 格式化为字符串
	 * @param date
	 * @return
	 */
	public static String formate(Date date) {
		return sf.format(date);
	}
}

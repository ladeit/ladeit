package com.ladeit.util.git;

public class LongTimeStampConvertInt {
		
	public static String convert(long timeStamp) {
		int length = String.valueOf(timeStamp).length();
		String message = null;
		//此范围按照秒计算
		if(length >= 4 && length < 5) {
			long time = (timeStamp / 1000);
			message = String.valueOf(time) +" secs";
		}
		//此范围按照分钟计算
		else if(length >= 5 && length < 7) {
			long time = (timeStamp / 1000 / 60);
			message = String.valueOf(time) +" mins";
		}
		//此范围按照小时计算
		else if(length >= 7 && length < 8) {
			long time = (timeStamp / 1000 / 60 / 60);
			message = String.valueOf(time) +" hours";
		}
		//此范围按照天计算
		else {
			long time =  (timeStamp / 1000 / 60 / 60 / 24);
			message = String.valueOf(time) +" days";
		}
		return message;
	}
}

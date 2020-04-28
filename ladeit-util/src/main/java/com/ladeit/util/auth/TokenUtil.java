package com.ladeit.util.auth;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @program: ladeit
 * @description: TokenUtil
 * @author: falcomlife
 * @create: 2020/04/06
 * @version: 1.0.0
 */
public class TokenUtil {


	/**
	 * 创建服务组token
	 *
	 * @param text
	 * @return java.lang.String
	 * @date 2019/11/7
	 * @ahthor MddandPyy
	 */
	public static String createToken(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		char[] encodes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(text.getBytes());
		byte[] data = messageDigest.digest();
		StringBuffer sb = new StringBuffer(data.length * 2);
		int pos = 0, val = 0;
		for (int i = 0; i < data.length; i++) {
			val = (val << 8) | (data[i] & 0xFF);
			pos += 8;
			while (pos > 5) {
				char c = encodes[val >> (pos -= 6)];
// sb.append(
//c == 'i' ? "ia" :
//  c == '+' ? "ib" :
// c == '/' ? "ic" : c
// );
				if ('i' == c) {
					sb.append("ia");
				} else if ('+' == c) {
					sb.append("ib");
				} else if ('/' == c) {
					sb.append("ic");
				} else {
					sb.append(c);
				}
				val &= ((1 << pos) - 1);
			}
		}
		if (pos > 0) {
			char c = encodes[val << (6 - pos)];
//sb.append(
////  c == 'i' ? "ia" :
//// c == '+' ? "ib" :
////c == '/' ? "ic" : c
////);
			if ('i' == c) {
				sb.append("ia");
			} else if ('+' == c) {
				sb.append("ib");
			} else if ('/' == c) {
				sb.append("ic");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

}

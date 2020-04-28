package com.ladeit.util.auth;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @program: ladeit
 * @description: PasswordResolving
 * @author: falcomlife
 * @create: 2019/10/30
 * @version: 1.0.0
 */
public class PasswordUtil {


	/**
	 * 加密程序
	 *
	 * @param plain
	 * @return java.lang.String[]
	 * @author falcomlife
	 * @date 19-10-30
	 * @version 1.0.0
	 */
	public static String[] encode(String plain) throws NoSuchAlgorithmException {
		String[] result = new String[2];
		//1.生成随机盐salt
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		String salt = byte2Hex(random.generateSeed(32));
		//String salt = "12345678901234567890123456789012";
		//2.明文和随机盐一起SHA256运算102400次
		result[0] = salt;
		result[1] = algorithm(plain, salt);
		;
		return result;
	}

	/**
	 * 解密程序
	 *
	 * @param plain
	 * @param salt
	 * @param ciperText
	 * @return boolean
	 * @author falcomlife
	 * @date 19-10-30
	 * @version 1.0.0
	 */
	public static boolean decode(String plain, String salt, String ciperText) throws NoSuchAlgorithmException {
		String ciperTextHolder = algorithm(plain, salt);
		return ciperTextHolder.equals(ciperText) ? true : false;
	}

	/**
	 * byte转16进制，不够填零补位
	 *
	 * @param bytes
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-10-30
	 * @version 1.0.0
	 */
	private static String byte2Hex(byte[] bytes) {
		StringBuffer stringBuffer = new StringBuffer();
		String temp = null;
		for (int i = 0; i < bytes.length; i++) {
			temp = Integer.toHexString(bytes[i] & 0xFF);
			if (temp.length() == 1) {
				//1得到一位的进行补0操作
				stringBuffer.append("0");
			}
			stringBuffer.append(temp);
		}
		return stringBuffer.toString();
	}

	/**
	 * 慢hash算法
	 *
	 * @param plain
	 * @param salt
	 * @return java.lang.String
	 * @author falcomlife
	 * @date 19-10-30
	 * @version 1.0.0
	 */
	public static String algorithm(String plain, String salt) throws NoSuchAlgorithmException {
		String ciperText = plain;
		for (int i = 0; i < 1024; i++) {
			ciperText = salt + ciperText;
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(ciperText.getBytes());
			ciperText = byte2Hex(messageDigest.digest());
		}
		return ciperText;
	}
}

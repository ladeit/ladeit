package com.ladeit.util.git;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {
	/**
	 * 将字符串生成hash串  
	 * @param key
	 * @return
	 */
	public static String hashKeyForDisk(String key) {
	        String cacheKey;
	        try {
	            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
	            mDigest.update(key.getBytes());
	            cacheKey = bytesToHexString(mDigest.digest());
	        } catch (NoSuchAlgorithmException e) {
	            cacheKey = String.valueOf(key.hashCode());
	        }
	        return cacheKey;
	    }
	  
	  private static String bytesToHexString(byte[] bytes) {
	        StringBuilder sb = new StringBuilder();
	        for (int i = 0; i < bytes.length; i++) {
	            String hex = Integer.toHexString(0xFF & bytes[i]);
	            if (hex.length() == 1) {
	                sb.append('0');
	            }
	            sb.append(hex);
	        }
	        return sb.toString();
	    }
}

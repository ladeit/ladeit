package com.ladeit.util.k8s;

import java.util.*;

/**
 * @program: ladeit
 * @description: LabelToString
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
public class LabelToString {

	public static String tran(Map<String, String> map) {
		StringBuffer stringBuffer = new StringBuffer();
		Iterator<String> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = map.get(key);
			stringBuffer.append(key + "=" + value + ",");
		}
		return stringBuffer.toString().substring(0,stringBuffer.length()-1);
	}
}

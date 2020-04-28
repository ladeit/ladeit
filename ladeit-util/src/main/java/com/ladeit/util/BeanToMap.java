package com.ladeit.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: BeanToMap
 * @author: falcomlife
 * @create: 2019/09/27
 * @version: 1.0.0
 */
public class BeanToMap {
	public static Map<String, Object> convert(Object obj) {
		Map<String, Object> reMap = new HashMap<String, Object>();
		if (obj == null)
			return null;
		Field[] fields = obj.getClass().getDeclaredFields();
		try {
			for (int i = 0; i < fields.length; i++) {
				try {
					Field f = obj.getClass().getDeclaredField(
							fields[i].getName());
					f.setAccessible(true);
					Object o = f.get(obj);
					reMap.put(fields[i].getName(), o);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return reMap;
	}
}

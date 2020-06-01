package com.ladeit.util.k8s;

import io.kubernetes.client.custom.Quantity;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: ladeit
 * @description: UnitUtil
 * @author: falcomlife
 * @create: 2020/03/31
 * @version: 1.0.0
 */
public class UnitUtil {
	/**
	 * 配额资源换算
	 *
	 * @param unit
	 * @param value
	 * @param type
	 * @return java.math.BigDecimal
	 * @author falcomlife
	 * @date 20-3-26
	 * @version 1.0.0
	 */
	public static BigDecimal unitConverter(String unit, Integer value, int type) {
		if (value == null || unit == null) {
			return null;
		}
		BigDecimal result = null;
		if (type == 1) {
			// 1代表cpu
			if ("m".equals(unit)) {
				BigDecimal origin = new BigDecimal(value);
				result = origin.divide(new BigDecimal(1000));
			} else if ("core".equals(unit)) {
				result = new BigDecimal(value);
			}
		} else if (type == 2) {
			// 2代表memory
			if ("m".equals(unit)) {
				BigDecimal origin = new BigDecimal(value);
				result =
						origin.multiply(new BigDecimal(1000)).multiply(new BigDecimal(1000));
			} else if ("Mi".equals(unit)) {
				BigDecimal origin = new BigDecimal(value);
				result = origin.multiply(new BigDecimal(1024)).multiply(new BigDecimal(1024));
			} else if ("Gi".equals(unit)) {
				BigDecimal origin = new BigDecimal(value);
				result =
						origin.multiply(new BigDecimal(1024)).multiply(new BigDecimal(1024)).multiply(new BigDecimal(1024));
			}
		}
		return result;
	}

	public static String[] unitConverter(Quantity quantity, String type) {
		String[] result = new String[2];
		String regEx = "[^0-9]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(quantity.toSuffixedString());
		result[0] = m.replaceAll("").trim();
		result[1] = quantity.toSuffixedString().replace(result[0], "");
		return result;
	}

	/**
	 * quantity转化，cpu统一成mcore，memory统一成m
	 *
	 * @param quantity
	 * @param type
	 * @return
	 * @author falcomlife
	 * @date 20-5-30
	 * @version 1.0.0
	 */
	public static BigDecimal quantityToNum(Quantity quantity, String type) {
		BigDecimal result = null;
		if (quantity == null) {
			return null;
		}
		if ("cpu".equals(type)) {
			if (quantity.getFormat() == Quantity.Format.DECIMAL_SI) {
				result = quantity.getNumber().multiply(new BigDecimal(1000));
			} else if (quantity.getFormat() == Quantity.Format.BINARY_SI) {
				result = quantity.getNumber();
			}
		}
		if ("mem".equals(type)) {
			if (quantity.getFormat() == Quantity.Format.DECIMAL_SI) {
				result = quantity.getNumber().multiply(new BigDecimal(1000));
			} else if (quantity.getFormat() == Quantity.Format.BINARY_SI) {
				result = quantity.getNumber().divide(new BigDecimal(1024)).divide(new BigDecimal(1024)).divide(new BigDecimal(1024)).multiply(new BigDecimal(1000));
			}
		}
		return result;
	}

	/**
	 * 剥离数值和单位
	 *
	 * @param resource
	 * @return java.lang.String[]
	 * @author falcomlife
	 * @date 20-4-7
	 * @version 1.0.0
	 */
	public static String[] stripNumberUnit(String resource) {
		String[] numunit = new String[2];
		if (resource.endsWith("m")) {
			numunit[0] = resource.replace("m", "");
			numunit[1] = "m";
		} else if (resource.endsWith("C")) {
			numunit[0] = resource.replace("C", "");
			numunit[1] = "C";
		} else if (resource.endsWith("Mi")) {
			numunit[0] = resource.replace("Mi", "");
			numunit[1] = "Mi";
		} else if (resource.endsWith("Gi")) {
			numunit[0] = resource.replace("Gi", "");
			numunit[1] = "Gi";
		} else {
			numunit[0] = resource;
			numunit[1] = null;
		}
		return numunit;
	}
}

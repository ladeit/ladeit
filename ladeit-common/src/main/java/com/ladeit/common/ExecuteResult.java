package com.ladeit.common;

import java.io.Serializable;
import java.util.*;

/**
 * 返回信息封装类
 *
 * @author falcomlife
 * @version 1.0.0, 19/02/25
 */
public class ExecuteResult<T> implements Serializable {

	private static final long serialVersionUID = 7365417829056921958L;
	/**
	 * 错误信息列表
	 */
	private List<String> errorMessages = new ArrayList<>();
	/**
	 * 异常属性
	 */
	private Map<String, String> fieldErrors = new LinkedHashMap<>();
	/**
	 * 警告信息列表
	 */
	private List<String> warningMessages = new ArrayList<>();
	/**
	 * 返回结果
	 */
	private T result;
	/**
	 * 状态码
	 */
	private int code;
	/**
	 * 成功信息
	 */
	private String successMessage;
	/**
	 *
	 */
	private String token;

	/**
	 * 添加错误信息
	 *
	 * @param errorMessage 错误信息
	 */
	public void addErrorMessage(String errorMessage) {
		this.errorMessages.add(errorMessage);
	}

	/**
	 * 添加错误键值对
	 *
	 * @param field        错误键名
	 * @param errorMessage 错误信息
	 */
	public void addFieldError(String field, String errorMessage) {
		this.fieldErrors.put(field, errorMessage);
	}

	/**
	 * 添加警告信息
	 *
	 * @param warningMessage 警告信息
	 */
	public void addWarningMessage(String warningMessage) {
		this.warningMessages.add(warningMessage);
	}

	/**
	 * 得到错误信息列表
	 *
	 * @return 返回错误信息列表List
	 */
	public List<String> getErrorMessages() {
		return this.errorMessages;
	}

	/**
	 * 替换错误信息列表数组
	 *
	 * @param errorMessages 错误信息列表List
	 */
	public void setErrorMessages(List<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	/**
	 * 得到错误map
	 *
	 * @return 错误信息map
	 */
	public Map<String, String> getFieldErrors() {
		return this.fieldErrors;
	}

	/**
	 * 替换错误map
	 *
	 * @param fieldErrors 错误信息键值对列表
	 */
	public void setFieldErrors(Map<String, String> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

	/**
	 * 替换错误map
	 *
	 * @param value 错误信息键值对列表
	 */
	public void addFieldErrors(String value) {
		this.fieldErrors.put(this.fieldErrors.size() + "", value);
	}

	/**
	 * 得到返回信息
	 *
	 * @return 返回信息
	 */
	public T getResult() {
		return this.result;
	}

	/**
	 * 设置返回信息
	 *
	 * @param result 返回信息
	 */
	public void setResult(T result) {
		this.result = result;
	}

	/**
	 * 如果没有错误信息或者错误键值返回true，否则返回false
	 *
	 * @return 返回是否错误
	 */
	public boolean isSuccess() {
		return (this.errorMessages.isEmpty());
	}

	/**
	 * 得到成功信息
	 *
	 * @return 成功信息
	 */
	public String getSuccessMessage() {
		return this.successMessage;
	}

	/**
	 * 替换错误信息
	 *
	 * @param successMessage 错误信息
	 */
	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}

	/**
	 * 得到警告信息
	 *
	 * @return 警告信息
	 */
	public List<String> getWarningMessages() {
		return this.warningMessages;
	}

	/**
	 * 替换警告信息
	 *
	 * @param warningMessages 警告信息
	 */
	public void setWarningMessages(List<String> warningMessages) {
		this.warningMessages = warningMessages;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}


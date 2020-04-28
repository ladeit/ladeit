package com.ladeit.util;


import com.ladeit.common.ExecuteResult;

import java.util.List;

/**
 * @description: ExecuteUtil
 * @author: falcomlife
 * @create: 2019/07/09
 * @version: 1.0.0
 */
public class ExecuteResultUtil<T> {

	/**
	 * 目标类型
	 */
	private Class targetClass;


	/**
	* 无参构建方法
	* @author falcomlife
	* @date 19-8-19
	* @version 1.0.0
	* @return
	* @param
	*/
	public ExecuteResultUtil() {
		super();
	}
	/**
	 * 初始化时指定目标类型
	 *
	 * @param targetClass
	 * @return
	 * @author falcomlife
	 * @date 19-8-19
	 * @version 1.0.0
	 */
	public ExecuteResultUtil(Class targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * @param source
	 * @FunctionName copyWarnError
	 * @author falcomlife
	 * @date 19-7-9
	 * @version 1.0.0
	 */
	public ExecuteResult<T> copyWarnError(ExecuteResult source) {
		ExecuteResult<T> result = new ExecuteResult<>();
		result.setWarningMessages(source.getWarningMessages());
		result.setErrorMessages(source.getErrorMessages());
		result.setFieldErrors(source.getFieldErrors());
		result.setSuccessMessage(source.getSuccessMessage());
		result.setCode(source.getCode());
		return result;
	}

	public ExecuteResult<T> copyInfoSourceToTarget(ExecuteResult source,ExecuteResult target){
		List<String> warns = source.getWarningMessages();
		List<String> errors = source.getErrorMessages();
		for (String warn : warns) {
			target.addWarningMessage(warn);
		}
		for (String error : errors) {
			target.addErrorMessage(error);
		}
		return target;
	}
}

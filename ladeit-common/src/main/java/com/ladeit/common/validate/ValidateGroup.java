package com.ladeit.common.validate;

import javax.validation.groups.Default;

public class ValidateGroup {
	/**
	*增加组
	 */
	public interface ADD extends Default{}
	/**
	*更新组
	 */
	public interface UPDATE extends Default{}
	/**
	*查询组
	 */
	public interface QUERY extends Default{}
	/**
	*删除组
	 */
	public interface DELTE extends Default{}
}

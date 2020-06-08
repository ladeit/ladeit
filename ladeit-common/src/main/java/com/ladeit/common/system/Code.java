package com.ladeit.common.system;

/**
 * warning异常发生时，执行过程可能会成功，或部分成功，但是存在部分数据或功能上的不完整，但是不影响操作和使用。
 * error异常发生时，执行过程出错失败，不会进行任何的持久化过程，已经持久化的数据理应回滚。
 *
 * @description: Code
 * @author: falcomlife
 * @create: 2019/07/18
 * @version: 1.0.0
 */
public class Code {

	/*
	 * 100-599号异常属于warning异常
	 */

	/**
	 * 操作成功
	 */
	public static final int SUCCESS = 100;
	/**
	 * 没找到资源
	 */
	public static final int NOTFOUND = 101;
	/**
	 * istio接口报错
	 */
	public static final int ISTIOWARN = 103;
	/**
	 * 登录用户名或密码错误
	 */
	public static final int NOUSER_FAILPASSWORD = 104;
	/**
	 * 用户名已存在
	 */
	public static final int USER_EXIT = 105;
	/**
	 * 警告
	 */
	public static final int WARNNING = 599;
	/*
	 * 600-1100号异常数据error异常
	 */

	/**
	 * 执行失败
	 */
	public static final int FAILED = 600;
	/**
	 * 403 Forbidden
	 */
	public static final int FORBIDDEN = 601;
	/**
	 * gitlab接口调用失败
	 */
	public static final int GITLAB_API_ERROR = 602;
	/**
	 * 执行持久化时出错
	 */
	public static final int SQL_ERROR = 603;
	/**
	 * 入参为空
	 */
	public static final int PARAM_BLANK = 604;
	/**
	 */
	public static final int PROJECTISVAILD = 605;
	/**
	 * 资源已经存在
	 */
	public static final int ALREADY_EXIST = 606;
	/**
	 * 资源已被占用
	 */
	public static final int ALREADY_USED = 607;
	/**
	 * 身份信息（session）失效
	 */
	public static final int SESSION_TIMEOUT = 608;
	/**
	 * token失效
	 */
	public static final int TOKEN_TIMEOUT = 609;
	/**
	 * 入参验证错误
	 */
	public static final int PARAM_ERROR = 610;

	/**
	 * 状态错误
	 */
	public static final int STATUS_ERROR = 611;

	/**
	 * 未找到服务
	 */

	public static final int NOT_FOUND_SERVICE = 612;

	/**
	 * k8s接口报错
	 */
	public static final int K8SWARN = 613;

	/**
	* 密码错误
	*/
	public static final int FAILPASSWORD = 614;

	/**
	 * 集群已经存在
	 */
	public static final int CLUSTER_EXIST = 615;
	/*
	 * 1100号以后的异常给权限使用，可能造成前台重新登录
	 */

	/**
	 * 权限不匹配
	 */
	public static final int AUTH_ERROR = 1100;


}

package com.ladeit.biz.aop;

import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Token.UserInfoUtil;
import com.ladeit.common.system.Code;
import com.ladeit.util.ExecuteResultUtil;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: ExecuteResultAop
 * @description: executeresultaop 是结合 ExecuteResult 一起使用的，用来封装返回实体类的aop工具类。
 * @author: falcomlife
 * @create: 2019/08/07
 * @version: 1.0.0
 * <p>
 * ExecuteResult 返回实体类中对于后台返回结果的处理存在两种情况，一种是后台调用时显式抛出的异常，
 * 另一种是经过业务判断，人为放入 ExecuteResult 工具类的code字段的返回错误码以及同时放入 errorMessages 或
 * warningMessages 中的错误或警告信息。
 * 对于第一种返回情况，我们在aop切面方法中统一catch异常，并根据返回异常的类型进行针对性的处理异常信息。
 * 对于第二种返回情况，我们将一个请求调用到的，每个 service 的返回信息，存储在一个 resultPool 中，在返回到 controller 时进行统一处理
 * <p>
 * 要使用此工具类，我们约定以下编码规范。
 * service 层返回信息一定使用 ExecuteResult 工具类进行包装。如果不使用此类型进行返回，也不会造成程序错误，但是您将不能使用每层的 service 中的 errorMessages 和
 * warningMessages 的自动封装功能。
 * 请结合 com.ladeit.common.system.Code 工具类一起使用，并根据自己情况规定 warning 和 error 的 code 范围。
 * 业务过程中出现的异常，我们直接向上抛出，异常会在这个aop中统一处理。
 */
@Aspect
@Order(value = 2)
@Component
@Slf4j
public class ExecuteResultAop {

	public static Map<Thread, List<Object>> resultPool = new HashMap<>();

	@Pointcut("execution( * com.ladeit..services.impl.*.*(..))")
	public void services() {
	}

	@Pointcut("execution( * com.ladeit..controller.*.*(..))")
	public void controller() {
	}

	@Before("controller()")
	public void beforeController(JoinPoint joinPoint) {
		//获取service返回值类型
		Signature signature = joinPoint.getSignature();
		Class returnType = ((MethodSignature) signature).getReturnType();
		//创建返回对象
		Thread t = Thread.currentThread();
		List<Object> resultList = new ArrayList<>();
		ExecuteResultAop.resultPool.put(t, resultList);
	}

	@Around("controller()")
	public Object aroundController(ProceedingJoinPoint pjp) {
		ExecuteResult result = new ExecuteResult();
		Thread t = Thread.currentThread();
		List<Object> resultList = null;
		try {
			Object obj = pjp.proceed();
			if (obj instanceof ExecuteResult) {
				// 针对ExecuteResult类型的返回返回进行转型
				result = (ExecuteResult) obj;
			} else {
				// 其他类型的返回不作处理
				return obj;
			}
		} catch (Throwable throwable) {
			log.error(throwable.getMessage(), throwable);
			// 针对controller自己抛出的异常，我们统一处理成失败,错误信息是系统异常
			if (throwable instanceof UnexpectedRollbackException) {
				// UnexpectedRollbackException 异常是给spring回滚用的，可能是 Spring 在 service层自己抛出的。
				// 一般抛出这类异常的时候，引起这个异常的异常已经在resultPool内了，所以这里不做处理了。
			} else {
				// 其他类型的异常照常处理
				resultList = ExecuteResultAop.resultPool.get(t);
				ExecuteResult res = new ExecuteResult();
				res.setCode(Code.FAILED);
				resultList.add(res);
			}
		} finally {
			resultList = ExecuteResultAop.resultPool.get(t);
			for (Object obj : resultList) {
				ExecuteResult resultInner = (ExecuteResult) obj;
				if (resultInner.equals(result)) {
					// 如果controller层直接返回service层的结果 写法形如return this.xxxService.xxxmethod()
					// ，可能造成resultList中的对象和result是同一个对象
					continue;
				}
				//new ExecuteResultUtil().copyInfoSourceToTarget(resultInner, result);
				if (resultInner.getCode() >= Code.AUTH_ERROR) {
					// 目前最大的错误码1100，不可能出现，不做处理
				} else if (resultInner.getCode() >= Code.FAILED && resultInner.getCode() < Code.AUTH_ERROR) {
					// 错误码600-1100属于错误等级，先都处理成失败
					result.setCode(Code.FAILED);
					if (resultInner.getErrorMessages().isEmpty() && resultList.size() == 1) {
						// 如果没写异常信息，且只有本条一条异常，说明改错误是在controller aop 中捕获异常后放入的，这里给补充一个错误信息
						result.addErrorMessage("Failed, ladeit system error.");
					} else {
						// 如果有异常信息，这里使用以前的异常信息
						for (Object error : resultInner.getErrorMessages()) {
							result.addErrorMessage(error.toString());
						}
					}
				} else if (resultInner.getCode() < Code.FAILED && resultInner.getCode() > Code.SUCCESS) {
					// 0-600属于警告等级，处理所有的warning信息
					result.setCode(resultInner.getCode());
					if (!resultInner.getWarningMessages().isEmpty()) {
						for (Object warning : resultInner.getWarningMessages()) {
							result.addWarningMessage(warning.toString());
						}
					}
				}
			}
			if (result.getCode() == 0) {
				// 默认code填成功
				result.setCode(Code.SUCCESS);
			}
			ExecuteResultAop.resultPool.remove(t);
			UserInfoUtil.remove();
		}
		return result;
	}

	@Around("services()")
	public Object aroundServices(ProceedingJoinPoint pjp) {
		ExecuteResult result = new ExecuteResult();
		boolean isExecuteResult = false;
		try {
			Object obj = pjp.proceed();
			isExecuteResult = obj instanceof ExecuteResult;
			if (isExecuteResult) {
				// 返回值类型是ExecuteResult的进行统一处理
				result = (ExecuteResult) obj;
				if (result.getCode() == 0) {
					// 没写返回码的，默认是成功，这里将code处理成成功
					result.setCode(Code.SUCCESS);
				} else if (result.getCode() >= Code.FAILED && result.getCode() < Code.AUTH_ERROR) {
					// 针对在业务内try掉的异常，返回码是错误类型的，我们在这里统一向上抛出异常，触发spring事务管理回滚事务
					throw new RuntimeException(result.getErrorMessages().get(0).toString());
				} else if (result.getCode() > Code.SUCCESS && result.getCode() < Code.FAILED) {
					// 针对在业务内try掉的异常，返回码是警告类型的，我们在这里统一向上抛出异常，触发spring事务管理回滚事务
					throw new RuntimeException(result.getWarningMessages().get(0).toString());
				}
			} else {
				// 返回值类型不是ExecuteResult的不作处理
				return obj;
			}
		} catch (Throwable throwable) {
			// 针对抛出异常的程序进行统一处理，这里包含两种情况，一种是程序内try掉，返回code的异常，这部分异常在上面的执行逻辑中已经进行了处理并重新抛出异常，这里可以捕获；另一种是程序直接向上抛出，未被捕获的异常。
			// isExecuteResult = true;
			log.error(throwable.getMessage(), throwable);
			if (throwable instanceof ApiException) {
				// k8s接口异常
				result.setCode(Code.K8SWARN);
				result.addWarningMessage(((ApiException) throwable).getResponseBody());
				throw new RuntimeException(result.getWarningMessages().get(0).toString());
			} else {
				// 其他类型异常
				if (result.getCode() == 0) {
					result.setCode(Code.FAILED);
					if (result.getErrorMessages().isEmpty()) {
						// 没有异常信息的统一维护异常信息，一般是 service 层抛出的，未经人工处理和未捕获的异常
						result.addErrorMessage("Failed, ladeit system error.");
					}
					throw new RuntimeException(result.getErrorMessages().get(0).toString());
				} else if (result.getCode() >= Code.FAILED && result.getCode() < Code.AUTH_ERROR) {
					throw new RuntimeException(result.getErrorMessages().get(0).toString());
				} else if (result.getCode() > Code.SUCCESS && result.getCode() < Code.FAILED) {
					throw new RuntimeException(result.getWarningMessages().get(0).toString());
				}

			}
		} finally {
			// 维护resultPool
			Thread t = Thread.currentThread();
			List<Object> resultList = ExecuteResultAop.resultPool.get(t);
			if (resultList != null) {
				resultList.add(result);
			}
		}
		return result;
	}

//	@AfterThrowing(pointcut = "services()", throwing = "e")
//	public Object handlerServiceThrow(JoinPoint joinPoint, Exception e) {
//		ExecuteResult result = new ExecuteResult();
//		if (e instanceof ApiException) {
//			result.setCode(Code.K8SWARN);
//			result.addWarningMessage(((ApiException) e).getResponseBody());
//		} else {
//			result.setCode(Code.FAILED);
//			result.addErrorMessage(e.getMessage());
//		}
//		return result;
//	}
//
//	@AfterThrowing(pointcut = "controller()", throwing = "e")
//	public Object handlerControllerThrow(JoinPoint joinPoint, Exception e) {
//		ExecuteResult result = new ExecuteResult();
//		if (e instanceof ApiException) {
//			result.setCode(Code.K8SWARN);
//			result.addWarningMessage(((ApiException) e).getResponseBody());
//		} else {
//			result.setCode(Code.FAILED);
//			result.addErrorMessage(e.getMessage());
//		}
//		return result;
//	}
}

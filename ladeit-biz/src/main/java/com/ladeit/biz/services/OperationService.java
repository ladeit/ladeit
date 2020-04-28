package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.doo.Operation;

/**
 * @program: ladeit
 * @description: OperationService
 * @author: falcomlife
 * @create: 2019/11/12
 * @version: 1.0.0
 */
public interface OperationService {

	/**
	 * 插入数据
	 *
	 * @param operation
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Operation>
	 * @author falcomlife
	 * @date 19-11-12
	 * @version 1.0.0
	 */
	ExecuteResult<Operation> insert(Operation operation);
}

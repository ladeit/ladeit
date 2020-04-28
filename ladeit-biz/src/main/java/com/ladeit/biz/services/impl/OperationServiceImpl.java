package com.ladeit.biz.services.impl;

import com.ladeit.biz.dao.OperationDao;
import com.ladeit.biz.services.OperationService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.doo.Operation;
import com.ladeit.pojo.doo.User;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @program: ladeit
 * @description: OperationService
 * @author: falcomlife
 * @create: 2019/11/12
 * @version: 1.0.0
 */
@Service
public class OperationServiceImpl implements OperationService {

	@Autowired
	private OperationDao operationDao;

	/**
	 * 插入数据
	 *
	 * @param operation
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Operation>
	 * @author falcomlife
	 * @date 19-11-12
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<Operation> insert(Operation operation) {
		ExecuteResult<Operation> result = new ExecuteResult<>();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		if (user == null) {
			operation.setCreateBy("bot");
		} else {
			operation.setCreateBy(user.getUsername());
			operation.setCreateById(user.getId());
		}
		operation.setCreateAt(new Date());
		this.operationDao.insert(operation);
		return result;
	}
}

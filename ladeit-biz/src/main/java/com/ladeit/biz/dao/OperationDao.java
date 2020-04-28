package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Operation;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname OperationDao
 * @Date 2019/11/11 13:50
 */
public interface OperationDao {

	List<Operation> getOperListByTargetAndId(String target, String targetId);

	/**
	 * 插入数据
	 *
	 * @param operation
	 * @return void
	 * @author falcomlife
	 * @date 19-11-12
	 * @version 1.0.0
	 */
	void insert(Operation operation);

	List<Operation> queryOperListByUserId(int currentPage, int pageSize,String userId);

	int queryOperCoundByUserId(String userId);
}

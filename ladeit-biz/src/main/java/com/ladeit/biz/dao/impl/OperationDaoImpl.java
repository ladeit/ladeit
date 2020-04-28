package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.OperationDao;
import com.ladeit.pojo.doo.Operation;
import io.ebean.EbeanServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname OperationDaoImpl
 * @Date 2019/11/11 13:52
 */
@Repository
public class OperationDaoImpl implements OperationDao {

	@Autowired
	private EbeanServer server;

	@Override
	public List<Operation> getOperListByTargetAndId(String target, String targetId) {
		return this.server.createQuery(Operation.class).where().eq("target", target).eq("targetId", targetId).orderBy(
		        "create_at desc").findList();
	}

	/**
	 * 插入数据
	 *
	 * @param operation
	 * @return void
	 * @author falcomlife
	 * @date 19-11-12
	 * @version 1.0.0
	 */
	@Override
	public void insert(Operation operation) {
		this.server.insert(operation);
	}

    @Override
    public List<Operation> queryOperListByUserId(int currentPage, int pageSize,String userId) {
        return this.server.createQuery(Operation.class).where().eq("createById",userId).orderBy("create_at desc").setFirstRow((currentPage - 1) * pageSize).setMaxRows(pageSize).findList();
    }

	@Override
	public int queryOperCoundByUserId(String userId) {
		return this.server.createQuery(Operation.class).where().eq("createById",userId).findCount();
	}
}

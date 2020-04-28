package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.ServiceGroupDao;
import com.ladeit.pojo.doo.Certificate;
import com.ladeit.pojo.doo.ServiceGroup;
import io.ebean.EbeanServer;
import io.ebean.ExpressionList;
import io.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ServiceGroupDaoImpl
 * @Date 2019/11/6 13:32
 */
@Repository
public class ServiceGroupDaoImpl implements ServiceGroupDao {

	@Autowired
	private EbeanServer server;

	@Override
	public void insert(ServiceGroup serviceGroup) {
		this.server.insert(serviceGroup);
	}

	@Override
	public List<ServiceGroup> queryServiceGroupList(String groupName, List<String> groupIdList) {
		if (groupIdList.size() == 0) {
			return new ArrayList<ServiceGroup>();
		} else {
			ExpressionList expressionList = this.server.createQuery(ServiceGroup.class).where();
			expressionList.in("id", groupIdList);
			if (!(groupName == null || groupName.trim().length() == 0)) {
				//expressionList.like("name","%"+groupName+"%");
				expressionList.eq("name", groupName);
			}
			expressionList.eq("isdel", false);
			return expressionList.findList();
		}
	}

	@Override
	public List<ServiceGroup> queryServiceGroupListByName(String groupName) {
		return this.server.createQuery(ServiceGroup.class).where().eq("name", groupName).eq("isdel",false).findList();
	}

	@Override
	public List<ServiceGroup> queryServiceGroupPagerList(int currentPage, int pageSize, String groupName) {
		ExpressionList expressionList = this.server.createQuery(ServiceGroup.class).where();
		if (!(groupName == null || groupName.trim().length() == 0)) {
			//expressionList.like("name","%"+groupName+"%");
			expressionList.eq("name", groupName);
		}
		expressionList.eq("isdel", false);
		expressionList.setFirstRow((currentPage - 1) * pageSize).setMaxRows(pageSize).orderBy("create_at desc").findList();
		return expressionList.findList();
	}

	@Override
	public List<SqlRow> queryServiceGroupSqlrowPagerList(int currentPage, int pageSize, String groupName,
                                                         String orderparam) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("select t1.* from (select t1.*,t2.name groupname,t2.create_at groupcreateat,t2.create_by " +
                "groupcreateby ,IFNULL(t3.c,0) imagenum from (select * from service where isdel = 0 ) t1 RIGHT JOIN " +
                "(select * from service_group where isdel = 0) t2 on t1.service_group_id = t2.id LEFT JOIN (select " +
                "count(1) c,service_id from image where isdel = 0 group by service_id) t3 on t1.id = t3.service_id " +
                "where 1=1 ");
		if (!(groupName == null || groupName.trim().length() == 0)) {
			sbf.append(" and t2.name like '%'" + groupName + "'%' ");
		}
		int start = (currentPage - 1) * pageSize;
		int end = pageSize;
		if (orderparam == null || orderparam.trim().length() == 0) {
			sbf.append(" order by t2.name asc,t1.name asc,t1.modify_at desc,t1.create_by asc,t1.create_at desc) t1 " +
                    "limit :start,:end ");
		} else if ("servicenameasc".equals(orderparam)) {
			sbf.append(" order by t1.name asc) t1 limit :start,:end ");
		} else if ("servicenamedesc".equals(orderparam)) {
			sbf.append(" order by t1.name desc) t1 limit :start,:end ");
		} else if ("modifyatasc".equals(orderparam)) {
			sbf.append(" order by t1.modify_at asc) t1 limit :start,:end ");
		} else if ("modifyatdesc".equals(orderparam)) {
			sbf.append(" order by t1.modify_at desc) t1 limit :start,:end ");
		} else if ("createatasc".equals(orderparam)) {
			sbf.append(" order by t1.create_at asc) t1 limit :start,:end ");
		} else if ("createatdesc".equals(orderparam)) {
			sbf.append(" order by t1.create_at desc) t1 limit :start,:end ");
		} else if ("createbyasc".equals(orderparam)) {
			sbf.append(" order by t1.create_by asc) t1 limit :start,:end ");
		} else if ("createbydesc".equals(orderparam)) {
			sbf.append(" order by t1.create_by desc) t1 limit :start,:end ");
		}
		List<SqlRow> list = server.createSqlQuery(sbf.toString()).setParameter("start", start).setParameter("end",
                end).findList();
		return list;
	}

	@Override
	public int queryGroupCount(String groupName) {
		ExpressionList expressionList = this.server.createQuery(ServiceGroup.class).where();
		if (!(groupName == null || groupName.trim().length() == 0)) {
			//expressionList.like("name","%"+groupName+"%");
			expressionList.eq("name", groupName);
		}
		expressionList.eq("isdel", false);
		return expressionList.findCount();
	}

	@Override
	public int queryGroupSqlrowCount(String groupName) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("select t1.* from (select t1.*,t2.name groupname,t2.create_at groupcreateat from (select * from " +
                "service where isdel = 0 ) t1  RIGHT JOIN (select * from service_group where isdel = 0) t2 on t1" +
                ".service_group_id = t2.id where 1=1 ");
		if (!(groupName == null || groupName.trim().length() == 0)) {
			sbf.append(" and t2.name like '%'" + groupName + "'%' ");
		}
		sbf.append(" order by t2.name asc,t1.name asc,t2.create_at desc) t1 ");
		List<SqlRow> list = server.createSqlQuery(sbf.toString()).findList();
		return list.size();
	}

	@Override
	public ServiceGroup queryServiceByName(String groupName) {
		return this.server.createQuery(ServiceGroup.class).where().eq("name", groupName).findOne();
	}

	@Override
	public ServiceGroup queryServiceByNameIsDel(String groupName) {
		return this.server.createQuery(ServiceGroup.class).where().eq("name", groupName).eq("isdel",false).findOne();
	}

	@Override
    public ServiceGroup queryServiceById(String groupId) {
        return this.server.createQuery(ServiceGroup.class).where().eq("id",groupId).eq("isdel",false).findOne();
    }

	@Override
	public void update(ServiceGroup serviceGroup) {
		this.server.update(serviceGroup);
	}

	@Override
	public ServiceGroup queryServiceByInviteCode(String inviteCode) {
		return this.server.createQuery(ServiceGroup.class).where().eq("inviteCode", inviteCode).findOne();
	}

	@Override
	public List<ServiceGroup> queryServiceGroupAll() {
		return this.server.createQuery(ServiceGroup.class).where().eq("isdel", false).findList();
	}

	/**
	 * 根据id查询group
	 *
	 * @param id
	 * @return com.ladeit.pojo.doo.ServiceGroup
	 * @author falcomlife
	 * @date 20-3-20
	 * @version 1.0.0
	 */
	@Override
	public ServiceGroup getGroupById(String id) {
		return this.server.createQuery(ServiceGroup.class).where().eq("id",id).findOne();
	}

    @Override
    public List<SqlRow> queryUsersByGroup(String groupId) {
		return this.server.createSqlQuery("select t1.user_id,t2.username from user_service_group_relation t1 LEFT JOIN user t2 on t1.user_id = t2.id where t1.service_group_id =:groupId").setParameter("groupId",groupId).findList();
    }

    @Override
    public List<Certificate> queryGroupBytoken(String token) {
        return this.server.createQuery(Certificate.class).where().eq("content",token).findList();
    }
}

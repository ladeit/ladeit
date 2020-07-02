package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.UserServiceRelationDao;
import com.ladeit.pojo.doo.UserServiceRelation;
import io.ebean.EbeanServer;
import io.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname UserServiceRelationDaoImpl
 * @Date 2019/11/25 14:39
 */
@Repository
public class UserServiceRelationDaoImpl implements UserServiceRelationDao {

    @Autowired
    private EbeanServer server;

    @Override
    public List<SqlRow> queryUsersByGroupId(int currentPage, int pageSize, String serviceGroupId, String userId) {
        return server.createSqlQuery("select  t1.name,t1.id serviceid,t2.role_num,t2.id from (select * from service where isdel = 0) t1 LEFT OUTER JOIN (select * from user_service_relation where user_id =:userId) t2 on t1.id = t2.service_id  where t1.service_group_id =:serviceGroupId").setParameter("userId",userId).setParameter("serviceGroupId",serviceGroupId).setFirstRow((currentPage - 1) * pageSize).setMaxRows(pageSize).findList();
    }

    @Override
    public List<SqlRow> queryNopagerUsersByGroupId(String serviceGroupId, String userId) {
        return server.createSqlQuery("select  t1.name,t1.id serviceid,t2.role_num,t2.id from (select * from service where isdel =0 ) t1 LEFT OUTER JOIN (select * from user_service_relation where user_id =:userId) t2 on t1.id = t2.service_id  where t1.service_group_id =:serviceGroupId").setParameter("userId",userId).setParameter("serviceGroupId",serviceGroupId).findList();
    }

    @Override
    public int getUserCount(String serviceGroupId, String userId) {
        List<SqlRow>  list = server.createSqlQuery("select  t1.name,t2.role_num from (select * from service where isdel = 0) t1 LEFT OUTER JOIN (select * from user_service_relation where user_id =:userId) t2 on t1.id = t2.service_id  where t1.service_group_id =:serviceGroupId").setParameter("userId",userId).setParameter("serviceGroupId",serviceGroupId).findList();
        return list.size();
    }

    @Override
    public UserServiceRelation getServiceRelation(String userId, String serviceId) {
        return server.createQuery(UserServiceRelation.class).where().eq("userId",userId).eq("serviceId",serviceId).findOne();
    }

    @Override
    public UserServiceRelation getServiceRelationById(String id) {
        return server.createQuery(UserServiceRelation.class).where().eq("id",id).findOne();
    }

    @Override
    public void insert(UserServiceRelation userServiceRelation) {
        server.insert(userServiceRelation);
    }

    @Override
    public void update(UserServiceRelation userServiceRelation) {
        server.update(userServiceRelation);
    }

    @Override
    public void delete(UserServiceRelation userServiceRelation) {
        server.delete(userServiceRelation);
    }

    @Override
    public List<UserServiceRelation> getServiceRelationByServiceId(String serviceId) {
        return server.createQuery(UserServiceRelation.class).where().eq("serviceId",serviceId).findList();
    }

    @Override
    public List<UserServiceRelation> getServiceRelationByUserId(String userId) {
        return server.createQuery(UserServiceRelation.class).where().eq("userId",userId).findList();
    }
}

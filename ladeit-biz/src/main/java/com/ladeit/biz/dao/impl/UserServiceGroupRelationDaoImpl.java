package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.UserServiceGroupRelationDao;
import com.ladeit.pojo.doo.UserServiceGroupRelation;
import io.ebean.EbeanServer;
import io.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname UserServiceGroupRelationDaoImpl
 * @Date 2019/11/25 11:27
 */
@Repository
public class UserServiceGroupRelationDaoImpl implements UserServiceGroupRelationDao {

    @Autowired
    private EbeanServer server;

    @Override
    public void insert(UserServiceGroupRelation userServiceGroupRelation) {
        server.save(userServiceGroupRelation);
    }

    @Override
    public void update(UserServiceGroupRelation userServiceGroupRelation) {
        server.update(userServiceGroupRelation);
    }

    @Override
    public void delete(UserServiceGroupRelation userServiceGroupRelation) {
        server.delete(userServiceGroupRelation);
    }

    @Override
    public List<SqlRow> queryUsersByGroupId(int currentPage, int pageSize, String serviceGroupId) {
        return server.createSqlQuery("select t1.id,t1.user_id,t2.username,t1.create_at,t1.access_level from user_service_group_relation t1 INNER JOIN user t2 on t1.user_id = t2.id where t1.service_group_id =:serviceGroupId and t2.isdel = 0 ").setParameter("serviceGroupId",serviceGroupId).setFirstRow((currentPage - 1) * pageSize).setMaxRows(pageSize).findList();
    }

    @Override
    public List<SqlRow> queryNopagerUsersByGroupId(String serviceGroupId) {
        return server.createSqlQuery("select t1.id,t1.user_id,t2.username,t1.create_at,t1.access_level from user_service_group_relation t1 INNER JOIN user t2 on t1.user_id = t2.id where t1.service_group_id =:serviceGroupId and t2.isdel = 0 ").setParameter("serviceGroupId",serviceGroupId).findList();
    }

    @Override
    public List<UserServiceGroupRelation> queryGrouprelationByUserId(String userId) {
        return server.createQuery(UserServiceGroupRelation.class).where().eq("userId",userId).findList();
    }

    @Override
    public List<UserServiceGroupRelation> queryGrouprelationByGroupId(String groupId) {
        return server.createQuery(UserServiceGroupRelation.class).where().eq("serviceGroupId",groupId).findList();
    }

    @Override
    public int getUserCount(String serviceGroupId) {
        List<SqlRow>  list = server.createSqlQuery("select t1.id,t2.username,t1.create_at,t1.access_level from user_service_group_relation t1 INNER JOIN user t2 on t1.user_id = t2.id where t1.service_group_id =:serviceGroupId and t2.isdel = 0 ").setParameter("serviceGroupId",serviceGroupId).findList();
        return list.size();
    }

    @Override
    public UserServiceGroupRelation getGrouprelation(String userId, String groupId) {
        return server.createQuery(UserServiceGroupRelation.class).where().eq("userId",userId).eq("serviceGroupId",groupId).findOne();
    }

    @Override
    public UserServiceGroupRelation getGroupRelationById(String id) {
        return server.createQuery(UserServiceGroupRelation.class).where().eq("id",id).findOne();
    }
}
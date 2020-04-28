package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.UserClusterRelationDao;
import com.ladeit.pojo.doo.Cluster;
import com.ladeit.pojo.doo.Env;
import com.ladeit.pojo.doo.UserClusterRelation;
import io.ebean.EbeanServer;
import io.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname UserClusterRelationDaoImpl
 * @Date 2020/2/1 15:37
 */
@Repository
public class UserClusterRelationDaoImpl implements UserClusterRelationDao {

    @Autowired
    private EbeanServer server;

    @Override
    public void insert(UserClusterRelation userClusterRelation) {
        server.insert(userClusterRelation);
    }

    @Override
    public void update(UserClusterRelation userClusterRelation) {
        server.update(userClusterRelation);
    }

    @Override
    public void delete(UserClusterRelation userClusterRelation) {
        server.delete(userClusterRelation);
    }

    @Override
    public List<SqlRow> queryNopagerUsersByClusterId(String clusterId) {
        return server.createSqlQuery("select t1.id,t1.user_id,t2.username,t1.create_at,t1.access_level from user_cluster_relation t1 INNER JOIN user t2 on t1.user_id = t2.id where t1.cluster_id =:clusterId and t2.isdel = 0 ").setParameter("clusterId",clusterId).findList();
    }

    @Override
    public UserClusterRelation queryByClusterIdAndUserId(String clusterId, String userId) {
        return server.createQuery(UserClusterRelation.class).where().eq("clusterId",clusterId).eq("userId",userId).findOne();
    }

    @Override
    public Cluster queryClusterByInviteCode(String inviteCode) {
        return server.createQuery(Cluster.class).where().eq("inviteCode",inviteCode).findOne();
    }

    @Override
    public UserClusterRelation queryByClusterRelationId(String id) {
        return server.createQuery(UserClusterRelation.class).where().eq("id",id).findOne();
    }

    @Override
    public List<Env> queryEnvByClusterId(String clusterId) {
        return server.createQuery(Env.class).where().eq("clusterId",clusterId).findList();
    }

    @Override
    public List<UserClusterRelation> queryClusterRelationByUserId(String userId) {
        return server.createQuery(UserClusterRelation.class).where().eq("userId",userId).findList();
    }

    @Override
    public List<UserClusterRelation> queryClusterRelationByClusterId(String clusterId) {
        return server.createQuery(UserClusterRelation.class).where().eq("clusterId",clusterId).findList();
    }
}

package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.UserEnvRelationDao;
import com.ladeit.pojo.doo.UserEnvRelation;
import io.ebean.EbeanServer;
import io.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname UserEnvRelationDaoImpl
 * @Date 2020/2/1 16:30
 */
@Repository
public class UserEnvRelationDaoImpl implements UserEnvRelationDao {

    @Autowired
    private EbeanServer server;


    @Override
    public void insert(UserEnvRelation userEnvRelation) {
        server.insert(userEnvRelation);
    }

    @Override
    public void update(UserEnvRelation userEnvRelation) {
        server.update(userEnvRelation);
    }

    @Override
    public void delete(UserEnvRelation userEnvRelation) {
        server.delete(userEnvRelation);
    }

    @Override
    public UserEnvRelation queryByEnvIdAndUserId(String envId, String userId) {
        return server.createQuery(UserEnvRelation.class).where().eq("envId",envId).eq("userId",userId).findOne();
    }

    @Override
    public UserEnvRelation queryByEnvRelationId(String id) {
        return server.createQuery(UserEnvRelation.class).where().eq("id",id).findOne();
    }

    @Override
    public List<SqlRow> queryNopagerUsersByClusterId(String clusterId, String userId) {
        return server.createSqlQuery("select  t1.namespace,t1.id envid,t2.access_level,t2.id,t1.cluster_id clusterid from (select * from env where isdel =0 ) t1 LEFT JOIN (select * from user_env_relation where user_id =:userId) t2 on t1.id = t2.env_id where t1.cluster_id =:clusterId ").setParameter("userId",userId).setParameter("clusterId",clusterId).findList();
    }

    @Override
    public List<UserEnvRelation> queryByClusterId(String clusterId, String userId) {
        return server.createQuery(UserEnvRelation.class).where().eq("clusterId",clusterId).eq("userId",userId).findList();
    }

    @Override
    public List<UserEnvRelation> queryByEnvId(String envId) {
        return server.createQuery(UserEnvRelation.class).where().eq("envId",envId).findList();
    }
}

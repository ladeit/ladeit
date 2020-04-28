package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.UserSlackRelationDao;
import com.ladeit.pojo.doo.UserSlackRelation;
import io.ebean.EbeanServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname UserSlackRelationDaoImpl
 * @Date 2020/1/7 16:27
 */
@Repository
public class UserSlackRelationDaoImpl implements UserSlackRelationDao {

    @Autowired
    private EbeanServer server;

    @Override
    public void insert(UserSlackRelation userSlackRelation) {
        server.insert(userSlackRelation);
    }

    @Override
    public UserSlackRelation queryUserSlackRelationBySlackUserId(String slackUserId) {
        return server.createQuery(UserSlackRelation.class).where().eq("slackUserId",slackUserId).findOne();
    }

    @Override
    public UserSlackRelation queryUserSlackRelationByUserId(String userId) {
        return server.createQuery(UserSlackRelation.class).where().eq("userId",userId).findOne();
    }

    @Override
    public UserSlackRelation queryUserSlackRelationById(String id) {
        return server.createQuery(UserSlackRelation.class).where().eq("id",id).findOne();
    }

    @Override
    public void delete(UserSlackRelation userSlackRelation) {
        server.delete(userSlackRelation);
    }
}

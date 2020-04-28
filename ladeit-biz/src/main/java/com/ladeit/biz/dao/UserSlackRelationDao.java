package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.UserSlackRelation;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname UserSlackRelationDao
 * @Date 2020/1/7 16:14
 */
public interface UserSlackRelationDao {
    void insert(UserSlackRelation userSlackRelation);

    UserSlackRelation queryUserSlackRelationBySlackUserId(String slackUserId);

    UserSlackRelation queryUserSlackRelationByUserId(String userId);

    UserSlackRelation queryUserSlackRelationById(String id);

    void  delete(UserSlackRelation userSlackRelation);
}

package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Cluster;
import com.ladeit.pojo.doo.Env;
import com.ladeit.pojo.doo.UserClusterRelation;
import com.ladeit.pojo.doo.UserServiceGroupRelation;
import io.ebean.SqlRow;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname UserClusterRelationDao
 * @Date 2019/02/01
 */

public interface UserClusterRelationDao {

    /**
     * 新增
     * @param userClusterRelation
     * @return void
     * @date 2019/11/25
     * @ahthor MddandPyy
     */
    void insert(UserClusterRelation userClusterRelation);

    void update(UserClusterRelation userClusterRelation);

    void delete(UserClusterRelation userClusterRelation);

    List<SqlRow> queryNopagerUsersByClusterId(String clusterId);

    UserClusterRelation queryByClusterIdAndUserId(String clusterId,String userId);

    Cluster queryClusterByInviteCode(String inviteCode);

    UserClusterRelation queryByClusterRelationId(String id);

    List<Env> queryEnvByClusterId(String clusterId);

    List<UserClusterRelation> queryClusterRelationByUserId(String userId);

    List<UserClusterRelation> queryClusterRelationByClusterId(String clusterId);

}

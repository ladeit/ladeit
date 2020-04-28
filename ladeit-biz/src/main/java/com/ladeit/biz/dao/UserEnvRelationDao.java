package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.UserClusterRelation;
import com.ladeit.pojo.doo.UserEnvRelation;
import io.ebean.SqlRow;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname UserEnvRelationDao
 * @Date 2019/02/01
 */

public interface UserEnvRelationDao {

    /**
     * 新增
     * @param userEnvRelation
     * @return void
     * @date 2019/11/25
     * @ahthor MddandPyy
     */
    void insert(UserEnvRelation userEnvRelation);

    void update(UserEnvRelation userEnvRelation);

    void delete(UserEnvRelation userEnvRelation);

    UserEnvRelation queryByEnvIdAndUserId(String envId,String userId);

    UserEnvRelation queryByEnvRelationId(String id);

    /**
     * 查询人员在某集群各环境下的信息
     * @param clusterId, userId
     * @return java.util.List<io.ebean.SqlRow>
     * @date 2020/2/2
     * @ahthor MddandPyy
     */
    List<SqlRow> queryNopagerUsersByClusterId(String clusterId, String userId);

    List<UserEnvRelation> queryByClusterId(String clusterId, String userId);

    List<UserEnvRelation> queryByEnvId(String envId);

}

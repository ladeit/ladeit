package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.UserServiceGroupRelation;
import io.ebean.SqlRow;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname UserServiceGroupRelationDao
 * @Date 2019/11/25 11:25
 */

public interface UserServiceGroupRelationDao {

    /**
     * 新增
     * @param userServiceGroupRelation
     * @return void
     * @date 2019/11/25
     * @ahthor MddandPyy
     */
    void insert(UserServiceGroupRelation userServiceGroupRelation);

    void update(UserServiceGroupRelation userServiceGroupRelation);

    void delete(UserServiceGroupRelation userServiceGroupRelation);

    /**
     * 查询服务组下的人员
     * @param serviceGroupId
     * @return void
     * @date 2019/11/25
     * @ahthor MddandPyy
     */
    List<SqlRow> queryUsersByGroupId(int currentPage, int pageSize,String serviceGroupId);

    List<SqlRow> queryNopagerUsersByGroupId(String serviceGroupId);

    List<UserServiceGroupRelation> queryGrouprelationByUserId(String userId);

    List<UserServiceGroupRelation> queryGrouprelationByGroupId(String groupId);

    /**
     * 获取用户总数量
     * @param serviceGroupId
     * @return int
     * @date 2019/11/25
     * @ahthor MddandPyy
     */
    int getUserCount(String serviceGroupId);

    UserServiceGroupRelation getGrouprelation(String userId,String groupId);

    UserServiceGroupRelation getGroupRelationById(String id);
}

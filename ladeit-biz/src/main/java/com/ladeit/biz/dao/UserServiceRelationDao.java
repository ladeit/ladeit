package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.UserServiceRelation;
import io.ebean.SqlRow;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname UserServiceRelationDao
 * @Date 2019/11/25 14:37
 */
public interface UserServiceRelationDao {
    /**
     * 查询人员在某组各服务下的信息
     * @param serviceGroupId
     * @return void
     * @date 2019/11/25
     * @ahthor MddandPyy
     */
    List<SqlRow> queryUsersByGroupId(int currentPage, int pageSize, String serviceGroupId,String userId);


    /**
     * 查询人员在某组各服务下的信息
     * @param serviceGroupId
     * @return void
     * @date 2019/11/25
     * @ahthor MddandPyy
     */
    List<SqlRow> queryNopagerUsersByGroupId(String serviceGroupId,String userId);

    /**
     * 获取总数量
     * @param serviceGroupId
     * @return int
     * @date 2019/11/25
     * @ahthor MddandPyy
     */
    int getUserCount(String serviceGroupId,String userId);


    /**
     * 查询用户服务权限
     * @param userId, serviceId
     * @return com.ladeit.pojo.doo.UserServiceRelation
     * @date 2019/12/4
     * @ahthor MddandPyy
     */
    UserServiceRelation getServiceRelation(String userId,String serviceId);

    UserServiceRelation getServiceRelationById(String id);

    void insert(UserServiceRelation userServiceRelation);

    void update(UserServiceRelation userServiceRelation);

    void delete(UserServiceRelation userServiceRelation);

    List<UserServiceRelation> getServiceRelationByServiceId(String serviceId);

    List<UserServiceRelation> getServiceRelationByUserId(String userId);
}

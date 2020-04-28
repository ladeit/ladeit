package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Certificate;
import com.ladeit.pojo.doo.ServiceGroup;
import io.ebean.SqlRow;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ServiceGroupDao
 * @Date 2019/11/6 13:30
 */
public interface ServiceGroupDao {

    void insert(ServiceGroup serviceGroup);

    List<ServiceGroup> queryServiceGroupList(String groupName,List<String> groupIdList);

    List<ServiceGroup> queryServiceGroupListByName(String groupName);

    List<ServiceGroup> queryServiceGroupPagerList(int currentPage,int pageSize,String groupName);

    List<SqlRow> queryServiceGroupSqlrowPagerList(int currentPage,int pageSize,String groupName,String orderparam);

    int queryGroupCount(String groupName);

    int queryGroupSqlrowCount(String groupName);

    ServiceGroup queryServiceByName(String groupName);

    ServiceGroup queryServiceByNameIsDel(String groupName);

    ServiceGroup queryServiceById(String groupId);

    void update(ServiceGroup serviceGroup);

    ServiceGroup queryServiceByInviteCode(String inviteCode);

    List<ServiceGroup> queryServiceGroupAll();

    /**
    * 根据idid查询group
    * @author falcomlife
    * @date 20-3-20
    * @version 1.0.0
    * @return com.ladeit.pojo.doo.ServiceGroup
    * @param id
    */
	ServiceGroup getGroupById(String id);

    List<SqlRow> queryUsersByGroup(String groupId);

    List<Certificate> queryGroupBytoken(String token);
}

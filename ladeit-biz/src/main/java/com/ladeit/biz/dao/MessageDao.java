package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Message;
import io.ebean.SqlRow;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MessageDao
 * @Date 2020/3/14 8:30
 */
public interface MessageDao {
    void insert(Message message);

    List<SqlRow> queryMessageSqlrowPagerList(String userId,int currentPage, int pageSize, String readFlag,String serviceGroupId,String type, String level);

    int queryMessageSqlrowCount(String userId,String readFlag,String serviceGroupId,String type);

    Message getMessage(String id);

    List<SqlRow> queryMessageByServiceId(String serviceId,String userId);
}

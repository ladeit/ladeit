package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.MessageState;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MessageStateDao
 * @Date 2020/3/14 9:17
 */
public interface MessageStateDao {

    void insert(MessageState messageState);

    MessageState getState(String id);

    void update(MessageState messageState);

    void delete(MessageState messageState);

    List<MessageState> getStates(String userId);

    List<MessageState> getNoReadStates(String userId);

}

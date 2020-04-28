package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.MessageStateDao;
import com.ladeit.pojo.doo.MessageState;
import io.ebean.EbeanServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MessageStateDaoImpl
 * @Date 2020/3/14 9:18
 */
@Repository
public class MessageStateDaoImpl implements MessageStateDao {

    @Autowired
    private EbeanServer server;

    @Override
    public void insert(MessageState messageState) {
        server.insert(messageState);
    }

    @Override
    public MessageState getState(String id) {
        return server.createQuery(MessageState.class).where().eq("id",id).findOne();
    }

    @Override
    public void update(MessageState messageState) {
        server.update(messageState);
    }

    @Override
    public void delete(MessageState messageState) {
        server.delete(messageState);
    }

    @Override
    public List<MessageState> getStates(String userId) {
        return server.createQuery(MessageState.class).where().eq("userId",userId).findList();
    }

    @Override
    public List<MessageState> getNoReadStates(String userId) {
        return server.createQuery(MessageState.class).where().eq("userId",userId).eq("readFlag",false).findList();
    }
}

package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.ChannelServiceGroupDao;
import com.ladeit.pojo.doo.ChannelServiceGroup;
import io.ebean.EbeanServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ChannelServiceGroupDaoImpl
 * @Date 2020/1/14 11:07
 */
@Repository
public class ChannelServiceGroupDaoImpl implements ChannelServiceGroupDao {

    @Autowired
    private EbeanServer server;

    @Override
    public List<ChannelServiceGroup> queryChannelByServiceGroupId(String serviceGroupId) {
        return server.createQuery(ChannelServiceGroup.class).where().eq("servicegroupId",serviceGroupId).findList();
    }

    @Override
    public List<ChannelServiceGroup> queryInfoByGroupNameAndChannelId(String channelId) {
        return server.createQuery(ChannelServiceGroup.class).where().eq("channelId",channelId).findList();
    }

    @Override
    public ChannelServiceGroup queryChannelById(String id) {
        return server.createQuery(ChannelServiceGroup.class).where().eq("id",id).findOne();
    }

    @Override
    public void delete(ChannelServiceGroup channelServiceGroup) {
        server.delete(channelServiceGroup);
    }

    @Override
    public void insert(ChannelServiceGroup channelServiceGroup) {
        server.insert(channelServiceGroup);
    }

    @Override
    public void update(ChannelServiceGroup channelServiceGroup) {
        server.update(channelServiceGroup);
    }
}

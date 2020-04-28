package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.ChannelServiceGroup;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ChannelServiceGroupDao
 * @Date 2019/12/25 15:52
 */
public interface ChannelServiceGroupDao {

   List<ChannelServiceGroup> queryChannelByServiceGroupId(String serviceGroupId);

   List<ChannelServiceGroup> queryInfoByGroupNameAndChannelId(String channelId);

   ChannelServiceGroup queryChannelById(String id);

   void delete(ChannelServiceGroup channelServiceGroup);

   void insert(ChannelServiceGroup channelServiceGroup);

   void update(ChannelServiceGroup channelServiceGroup);

}

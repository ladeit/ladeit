package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.ServicePublishBot;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ServicePublishBotDao
 * @Date 2020/1/18 10:34
 */
public interface ServicePublishBotDao {
    ServicePublishBot queryServicePublishBotByServiceId(String serviceId);

    void insert(ServicePublishBot servicePublishBot);

    void update(ServicePublishBot servicePublishBot);

    void delete(ServicePublishBot servicePublishBot);
}

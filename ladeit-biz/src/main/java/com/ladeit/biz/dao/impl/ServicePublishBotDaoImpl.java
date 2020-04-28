package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.ServicePublishBotDao;
import com.ladeit.pojo.doo.ServicePublishBot;
import io.ebean.EbeanServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ServicePublishBotDaoImpl
 * @Date 2020/1/18 10:35
 */
@Repository
public class ServicePublishBotDaoImpl implements ServicePublishBotDao {

    @Autowired
    private EbeanServer server;

    @Override
    public ServicePublishBot queryServicePublishBotByServiceId(String serviceId) {
        return server.createQuery(ServicePublishBot.class).where().eq("serviceId",serviceId).findOne();
    }

    @Override
    public void insert(ServicePublishBot servicePublishBot) {
        server.insert(servicePublishBot);
    }

    @Override
    public void update(ServicePublishBot servicePublishBot) {
        server.update(servicePublishBot);
    }

    @Override
    public void delete(ServicePublishBot servicePublishBot) {
        server.delete(servicePublishBot);
    }
}

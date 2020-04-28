package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.YamlDao;
import com.ladeit.pojo.doo.Yaml;
import io.ebean.EbeanServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname YamlDaoImpl
 * @Date 2019/12/31 14:53
 */
@Repository
public class YamlDaoImpl implements YamlDao {

    @Autowired
    private EbeanServer server;

    @Override
    public void insert(Yaml yaml) {
        server.insert(yaml);
    }

    @Override
    public List<Yaml> queryYamls(String serviceGroupId, String serviceId, int currentPage, int pageSize) {
        return server.createQuery(Yaml.class).where().eq("serviceGroupId",serviceGroupId).eq("serviceId",serviceId).setFirstRow((currentPage - 1) * pageSize).setMaxRows(pageSize).orderBy("create_at desc").findList();
    }

    @Override
    public int queryYamlCount(String serviceGroupId, String serviceId) {
        return server.createQuery(Yaml.class).where().eq("serviceGroupId",serviceGroupId).eq("serviceId",serviceId).findCount();
    }

    @Override
    public Yaml queryYaml(String id) {
        return server.createQuery(Yaml.class).where().eq("id",id).findOne();
    }

    @Override
    public List<Yaml> queryYamlsByServiceId(String serviceId) {
        return server.createQuery(Yaml.class).where().eq("serviceId",serviceId).findList();
    }
}

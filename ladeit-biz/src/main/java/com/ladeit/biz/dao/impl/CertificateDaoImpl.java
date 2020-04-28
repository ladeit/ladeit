package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.CertificateDao;
import com.ladeit.pojo.doo.Certificate;
import io.ebean.EbeanServer;
import io.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname CertificateDaoImpl
 * @Date 2019/11/6 14:23
 */
@Repository
public class CertificateDaoImpl implements CertificateDao {

    @Autowired
    private EbeanServer server;

    @Override
    public void insert(Certificate certificate) {
        this.server.insert(certificate);
    }

    @Override
    public void update(Certificate certificate) {
        this.server.update(certificate);
    }

    @Override
    public SqlRow queryGroupInfo(String token) {
        return this.server.createSqlQuery("select t2.id,t2.name,env_id from certificate t1 LEFT JOIN service_group t2 on t1.service_group_id = t2.id where t1.content =:token").setParameter("token",token).findOne();
    }

    @Override
    public Certificate queryCertificateByGroupId(String groupId) {
        return this.server.createQuery(Certificate.class).where().eq("serviceGroupId",groupId).findOne();
    }


}

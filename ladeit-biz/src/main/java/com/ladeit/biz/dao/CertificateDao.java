package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Certificate;
import io.ebean.SqlRow;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname CertificateDao
 * @Date 2019/11/6 14:22
 */
public interface CertificateDao {

    void insert(Certificate certificate);

    void update(Certificate certificate);

    SqlRow queryGroupInfo(String token);

    Certificate queryCertificateByGroupId(String groupId);
}

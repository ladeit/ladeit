package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.MessageDao;
import com.ladeit.pojo.doo.Message;
import com.ladeit.pojo.doo.ServiceGroup;
import io.ebean.EbeanServer;
import io.ebean.ExpressionList;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MessageDaoImpl
 * @Date 2020/3/14 8:31
 */
@Repository
public class MessageDaoImpl implements MessageDao {

    @Autowired
    private EbeanServer server;

    @Override
    public void insert(Message message) {
        server.insert(message);
    }

    @Override
    public List<SqlRow> queryMessageSqlrowPagerList(String userId, int currentPage, int pageSize, String readFlag,String serviceGroupId,String type) {
        StringBuffer sbf = new StringBuffer();
        sbf.append("select * from (select t1.*,t2.id messagestateid,t2.user_id,t2.read_flag,t3.username,t4.name servicename,t5.name servicegroupname from message t1 INNER JOIN message_state t2 on t1.id = t2.message_id LEFT JOIN user t3 on t1.operuser_id = t3.id  LEFT JOIN service t4 on t1.service_id = t4.id  LEFT JOIN service_group t5 on t1.service_group_id = t5.id  where t2.user_id =:userId ");
        if(!(readFlag==null || readFlag.trim().length()==0)){
            if("true".equals(readFlag)){
                sbf.append(" and t2.read_flag = 1 ");
            }else if("false".equals(readFlag)){
                sbf.append(" and t2.read_flag = 0 ");
            }else{ }
        }
        if(!(serviceGroupId==null || serviceGroupId.trim().length()==0)){
           sbf.append(" and t1.service_group_id =:serviceGroupId");
        }
        if(!(type==null || type.trim().length()==0)){
            if("normal".equals(type)){
                sbf.append(" and t1.type!=110");
            }else if("110".equals(type)){
                sbf.append(" and t1.type=110");
            }else{
                sbf.append(" and t1.type=:type");
            }
        }
        int start = (currentPage-1)*pageSize;
        int end = pageSize;
        sbf.append(" order by t1.create_at DESC) t1 limit :start,:end ");
        SqlQuery sqlQuery = server.createSqlQuery(sbf.toString()).setParameter("userId",userId).setParameter("start", start).setParameter("end",end);
        if(!(serviceGroupId==null || serviceGroupId.trim().length()==0)){
            sqlQuery.setParameter("serviceGroupId",serviceGroupId);
        }
        if(!(type==null || type.trim().length()==0)){
            if(!"normal".equals(type) && "110".equals(type)){
                sqlQuery.setParameter("type",type);
            }
        }
        List<SqlRow> list = sqlQuery.findList();
        return list;
    }

    @Override
    public int queryMessageSqlrowCount(String userId, String readFlag,String serviceGroupId,String type) {
        StringBuffer sbf = new StringBuffer();
        sbf.append("select t1.*,t2.id messagestateid,t2.user_id,t2.read_flag from message t1 INNER JOIN message_state t2 on t1.id = t2.message_id  where user_id =:userId ");
        if(!(readFlag==null || readFlag.trim().length()==0)){
            if("true".equals(readFlag)){
                sbf.append(" and t2.read_flag = 1 ");
            }else if("false".equals(readFlag)){
                sbf.append(" and t2.read_flag = 0 ");
            }else{ }
        }
        if(!(serviceGroupId==null || serviceGroupId.trim().length()==0)){
            sbf.append(" and t1.service_group_id=:serviceGroupId");
        }
        if(!(type==null || type.trim().length()==0)){
            sbf.append(" and t1.type=:type");
        }
        sbf.append(" order by t1.create_at DESC");
        SqlQuery sqlQuery = server.createSqlQuery(sbf.toString()).setParameter("userId",userId);
        if(!(serviceGroupId==null || serviceGroupId.trim().length()==0)){
            sqlQuery.setParameter("serviceGroupId",serviceGroupId);
        }
        if(!(type==null || type.trim().length()==0)){
            sqlQuery.setParameter("type",type);
        }
        List<SqlRow> list = sqlQuery.findList();
        return list.size();
    }

    @Override
    public Message getMessage(String id) {
        return server.createQuery(Message.class).where().eq("id",id).findOne();
    }

    @Override
    public List<SqlRow> queryMessageByServiceId(String serviceId,String userId) {
        return this.server.createSqlQuery("select t1.* from (select * from message where service_id =:serviceId and message_type = '2' ) t1 INNER JOIN (select * from message_state  where user_id =:userId and read_flag = 0 ) t2 on t1.id = t2.message_id   where 1=1 order by create_at desc limit 3").setParameter("userId",userId).setParameter("serviceId",serviceId).findList();
    }
}

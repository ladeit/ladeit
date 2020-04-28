package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.HeatMapDao;
import com.ladeit.pojo.doo.HeatMap;
import io.ebean.EbeanServer;
import io.ebean.ExpressionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname HeatMapDaoImpl
 * @Date 2019/12/9 10:41
 */
@Repository
public class HeatMapDaoImpl implements HeatMapDao {

    @Autowired
    private EbeanServer server;

    @Override
    public void insert(HeatMap heatMap) {
        server.insert(heatMap);
    }

    @Override
    public void update(HeatMap heatMap) {
        server.update(heatMap);
    }

    @Override
    public HeatMap queryHeatMapByTargetIdAndDate(String targetId, String date) {
        return server.createQuery(HeatMap.class).where().eq("date",date).eq("targetId",targetId).findOne();
    }

    @Override
    public List<HeatMap> queryHeatMapsByTargetIdAndDate(String targetId, String startDate, String endDate) {
        ExpressionList expressionList = server.createQuery(HeatMap.class).where();
        expressionList.eq("targetId",targetId);
        if(startDate!=null){
            expressionList.ge("date",startDate);
        }
        if(endDate!=null){
            expressionList.le("date",endDate);
        }
        return expressionList.findList();
    }
}

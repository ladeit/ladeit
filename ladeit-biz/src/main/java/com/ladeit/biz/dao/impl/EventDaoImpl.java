package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.EventDao;
import com.ladeit.pojo.doo.Event;
import io.ebean.EbeanServer;
import io.ebean.ExpressionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventDaoImpl implements EventDao {

    @Autowired
    private EbeanServer server;

    @Override
    public void save(Event event) {
        this.server.insert(event);
    }

    @Override
    public Event findByUid(Event event) {
        return this.server.createQuery(Event.class).where().eq("event_uid", event.getEventUid()).findOne();
    }

    @Override
    public List<Event> searchPage(Event event, List<String> uids) {
        ExpressionList<Event> expressionList = this.server.createQuery(Event.class).where();
        if (event.getStartTime() != null) {
                expressionList.gt("time", event.getStartTime());
        }
        if (event.getEndTime() != null) {
            expressionList.lt("time", event.getEndTime());
        }
        expressionList.in("resource_uid",uids);
        return expressionList.orderBy("time desc").setFirstRow((event.getPageNum() - 1) * event.getPageSize()).setMaxRows(event.getPageSize()).findList();
    }

    @Override
    public int searchPageCount(Event event, List<String> uids) {
        ExpressionList<Event> expressionList = this.server.createQuery(Event.class).where();
        if (event.getStartTime() != null) {
            expressionList.gt("time", event.getStartTime());
        }
        if (event.getEndTime() != null) {
            expressionList.lt("time", event.getEndTime());
        }
        expressionList.in("resource_uid",uids);
        return expressionList.findCount();
    }
}

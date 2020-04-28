package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Event;

import java.util.List;

public interface EventDao {
    void save(Event event);

    Event findByUid(Event event);

    List<Event> searchPage(Event event, List<String> uids);

    int searchPageCount(Event event, List<String> uids);
}

package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.doo.Event;
import io.kubernetes.client.ApiException;

import java.io.IOException;
import java.util.List;

public interface EventService {

	void save(Event event);

	/**
	 * events分页查询
	 *
	 * @param serviceId
	 * @param event
	 * @return
	 * @throws IOException
	 */
	ExecuteResult<List<Event>> searchEventsPage(String serviceId, Event event) throws IOException;

	/**
	 * update过程中查询events
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.doo.Event>>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	ExecuteResult<List<Event>> searchEventsInUpdate(String serviceId) throws IOException, ApiException;
}

package com.ladeit.biz.controller;

import com.ladeit.biz.services.EventService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.EventAO;
import com.ladeit.pojo.doo.Event;
import io.kubernetes.client.ApiException;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: ladeit
 * @description: K8sClusterController
 * @author: falcomlife
 * @create: 2019/09/19
 * @version: 1.0.0
 */
@RestController
@RequestMapping(value = "/api/${api.version}/event")
@Api(description = "event操作", tags = "/api/v1/", hidden = true)
public class EventController {

	@Autowired
	private EventService eventService;

	/**
	 * 分页查询events
	 *
	 * @param serviceId
	 * @param eventAO
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.EventAO>>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@PostMapping("/{serviceId}")
	public ExecuteResult<Pager<EventAO>> searchEventsPage(@PathVariable("serviceId") String serviceId,
														  @RequestBody EventAO eventAO) throws IOException {
		ExecuteResult<Pager<EventAO>> result = new ExecuteResult<>();
		Pager<EventAO> pager = new Pager<>();
		pager.setPageNum(eventAO.getPageNum());
		pager.setPageSize(eventAO.getPageSize());
		Event event = new Event();
		BeanUtils.copyProperties(eventAO, event);
		ExecuteResult<List<Event>> events = this.eventService.searchEventsPage(serviceId, event);
		if (events.getResult() != null) {
			List<EventAO> aos = events.getResult().stream().map(e -> {
				EventAO ao = new EventAO();
				BeanUtils.copyProperties(e, ao);
				return ao;
			}).collect(Collectors.toList());
			pager.setRecords(aos);
			pager.setTotalRecord(Integer.valueOf(events.getSuccessMessage()));
			result.setResult(pager);
		}
		return result;
	}

	/**
	* update过程中查询events
	* @author falcomlife
	* @date 20-4-10
	* @version 1.0.0
	* @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.EventAO>>
	* @param releaseId
	*/
	@GetMapping("/{releaseId}")
	public ExecuteResult<List<EventAO>> searchEvents(@PathVariable("releaseId") String releaseId) throws IOException, ApiException {
		ExecuteResult<List<EventAO>> result = new ExecuteResult<>();
		ExecuteResult<List<Event>> events = this.eventService.searchEventsInUpdate(releaseId);
		if (events.getResult() != null) {
			List<EventAO> aos = events.getResult().stream().map(e -> {
				EventAO ao = new EventAO();
				BeanUtils.copyProperties(e, ao);
				return ao;
			}).collect(Collectors.toList());
			result.setResult(aos);
		}
		return result;
	}
}

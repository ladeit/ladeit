package com.ladeit.biz.services.impl;

import com.ladeit.biz.dao.EventDao;
import com.ladeit.biz.manager.K8sContainerManager;
import com.ladeit.biz.manager.K8sWorkLoadsManager;
import com.ladeit.biz.services.*;
import com.ladeit.biz.utils.RAMPager;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.doo.Cluster;
import com.ladeit.pojo.doo.Env;
import com.ladeit.pojo.doo.Event;
import com.ladeit.pojo.doo.Release;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Config;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EventServiceImpl implements EventService {

	@Autowired
	private EventDao eventDao;
	@Autowired
	private K8sWorkLoadsManager k8sWorkLoadsManager;
	@Autowired
	private K8sContainerManager k8sContainerManager;
	@Autowired
	private EnvService envService;
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private ServiceService serviceService;
	@Autowired
	private ReleaseService releaseService;
	@Resource(name = "eventRedisTemplate")
	private RedisTemplate redisTemplate;

	@Override
	public void save(Event event) {
		Event eventAlready = this.eventDao.findByUid(event);
		if (eventAlready == null) {
			this.eventDao.save(event);
		}
	}

	/**
	 * events分页查询
	 *
	 * @param serviceId
	 * @param event
	 * @return
	 * @throws IOException
	 */
	@Override
	public ExecuteResult<List<Event>> searchEventsPage(String serviceId, Event event) throws IOException {
		ExecuteResult<List<Event>> result = new ExecuteResult<>();
		ExecuteResult<com.ladeit.pojo.doo.Service> service = this.serviceService.getById(serviceId);
		String envid = service.getResult().getEnvId();
		Env env = this.envService.getEnvById(envid);
		Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
		List<String> uids = k8sWorkLoadsManager.getResourceBySelector(cluster.getK8sKubeconfig(), env.getNamespace(),
				"serviceId=" + serviceId);
		Set<com.ladeit.pojo.dto.Event> set = redisTemplate.boundZSetOps("service:" + env.getId()).range(0, -1);
		Stream<com.ladeit.pojo.dto.Event> stream = set.stream().filter(e -> uids.contains(e.getResourceUid()));
		if (event.getStartTime() != null) {
			stream = stream.filter(e -> e.getStartTime().compareTo(event.getStartTime()) > 0);
		}
		if (event.getEndTime() != null) {
			stream = stream.filter(e -> e.getEndTime().compareTo(event.getEndTime()) < 0);
		}
		stream = stream.sorted(Comparator.comparing(com.ladeit.pojo.dto.Event::getStartTime).reversed());
		List<Event> origin = stream.map(e -> {
			Event res = new Event();
			BeanUtils.copyProperties(e, res);
			return res;
		}).collect(Collectors.toList());
		RAMPager<Event> pager = new RAMPager<>(origin, event.getPageSize());
		result.setResult(pager.page(event.getPageNum()));
		result.setSuccessMessage(origin.size() + "");
		return result;
	}

	/**
	 * update过程中查询events
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.doo.Event>>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	@Override
	public ExecuteResult<List<Event>> searchEventsInUpdate(String serviceId) throws IOException, ApiException {
		ExecuteResult<List<Event>> result = new ExecuteResult<>();
		List<Event> events = new ArrayList<>();
		ExecuteResult<Release> release = this.releaseService.getInUpdateRelease(serviceId);
		ExecuteResult<com.ladeit.pojo.doo.Service> service = this.serviceService.getById(serviceId);
		String envid = service.getResult().getEnvId();
		Env env = this.envService.getEnvById(envid);
		Cluster cluster = this.clusterService.getClusterById(env.getClusterId());
		ByteArrayInputStream inputStream = new ByteArrayInputStream(cluster.getK8sKubeconfig().getBytes());
		ApiClient apiClient = Config.fromConfig(inputStream);
		List<V1Pod> pods = this.k8sContainerManager.getPodList(env.getNamespace(), cluster.getK8sKubeconfig());
		List<Event> finalEvents = new ArrayList<>();
		pods.stream().forEach(pod -> {
			if (pod.getMetadata().getLabels().get("releaseId") != null && pod.getMetadata().getLabels().get("releaseId"
			).equals(release.getResult().getId())) {
				finalEvents.addAll(pod.getStatus().getConditions().stream().map(v1PodCondition -> {
					Event event = new Event();
					event.setName(pod.getMetadata().getName());
					event.setNamespace(pod.getMetadata().getNamespace());
					event.setType(v1PodCondition.getType());
					event.setNote(v1PodCondition.getStatus());
					event.setTime(v1PodCondition.getLastTransitionTime().toDate());
					return event;
				}).collect(Collectors.toList()));
			}
		});
		events = finalEvents.stream().sorted(new Comparator<Event>() {
			@Override
			public int compare(Event o1, Event o2) {
				return o1.getTime().after(o2.getTime()) ? 1 : -1;
			}
		}).collect(Collectors.toList());
		result.setResult(events);
		return result;
	}
}

package com.ladeit.biz.dao.impl;

import com.ladeit.biz.dao.ServiceDao;
import com.ladeit.biz.dao.ServiceGroupDao;
import com.ladeit.pojo.UserInfo;
import com.ladeit.pojo.doo.*;
import io.ebean.EbeanServer;
import io.ebean.ExpressionList;
import io.ebean.SqlRow;
import io.ebean.UpdateQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @program: ladeit
 * @description: ServiceDaoImpl
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
@Repository
public class ServiceDaoImpl implements ServiceDao {

	@Autowired
	private EbeanServer server;

	@Autowired
	private ServiceGroupDao serviceGroupDao;

	/**
	 * 更新service
	 *
	 * @param service
	 * @return void
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	@Override
	public void update(Service service) {
		UpdateQuery<Service> updateQuery = this.server.update(Service.class).set("status", service.getStatus()).set(
				"modify_at", new Date());
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		if (user == null) {
			updateQuery.set("modify_by", "bot");
		} else {
			updateQuery.set("modify_by", user.getUsername());
		}
		if (StringUtils.isNotBlank(service.getImageVersion())) {
			updateQuery.set("image_version", service.getImageVersion());
		}
		if (StringUtils.isNotBlank(service.getImageId())) {
			updateQuery.set("image_id", service.getImageId());
		}
		if (service.getReleaseAt() != null) {
			updateQuery.set("release_at", service.getReleaseAt());
		}
		updateQuery.where().eq("id", service.getId()).update();
	}

	@Override
	public void insert(Service service) {
		this.server.insert(service);
	}

	@Override
	public Service queryServiceById(String serviceId) {
		return server.createQuery(Service.class).where().eq("id", serviceId).findOne();
	}

	@Override
	public List<Service> queryServiceList(String groupid) {
		return this.server.createQuery(Service.class).where().eq("serviceGroupId", groupid).eq("isdel", false).findList();
	}

// @Override
// public Service queryServiceByGroupAndName(String token, String serviceName) {
//		Certificate certificate = this.server.createQuery(Certificate.class).where().eq("content",token).findOne();
//		return this.server.createQuery(Service.class).where().eq("serviceGroupId",certificate.getServiceGroupId()).eq
// ("name",serviceName).eq("isdel",false).findOne();
//	}

	@Override
	public List<Service> queryServiceListByParam(String serviceId, String serviceGroup, String serviceName) {
		ExpressionList expressionList = this.server.createQuery(Service.class).where();
		expressionList.eq("isdel", false);
		if (!(serviceId == null || serviceId.trim().length() == 0)) {
			expressionList.eq("id", serviceId);
		}
		if (!(serviceGroup == null || serviceGroup.trim().length() == 0)) {
			ServiceGroup group = serviceGroupDao.queryServiceByName(serviceGroup);
			if (group != null) {
				expressionList.eq("serviceGroupId", group.getId());
			} else {
				expressionList.eq("serviceGroupId", "");
			}
		}
		if (!(serviceName == null || serviceName.trim().length() == 0)) {
			expressionList.eq("name", serviceName);
		}
		return expressionList.findList();
	}

	@Override
	public Service queryServiceByGroupAndName(String groupId, String serviceName) {
		return this.server.createQuery(Service.class).where().eq("serviceGroupId", groupId).eq("name", serviceName).eq("isdel", false).findOne();
	}

	@Override
	public void delete(Service service) {
		this.server.update(service);
	}

	/**
	 * 查询service
	 *
	 * @param serviceId
	 * @return com.ladeit.pojo.doo.Service
	 * @author falcomlife
	 * @date 19-12-12
	 * @version 1.0.0
	 */
	@Override
	public Service getById(String serviceId) {
		return this.server.createQuery(Service.class).where().idEq(serviceId).eq("isdel", false).findOne();
	}

	@Override
	public void updateStatusById(Service service) {
		this.server.update(Service.class).set("status", service.getStatus()).where().eq("id", service.getId()).update();
	}

	/**
	 * 根据groupid查询service
	 *
	 * @param groupId
	 * @return
	 */
	@Override
	public List<Service> getServiceByGroupId(String groupId) {
		return this.server.createQuery(Service.class).where().eq("service_group_id", groupId).eq("isdel", false).findList();
	}

	@Override
	public Service getServiceByToken(String token) {
		return this.server.createQuery(Service.class).where().eq("token", token).findOne();
	}

	@Override
	public int getImageNum(String serviceId) {
		return this.server.createQuery(Image.class).where().eq("serviceId", serviceId).eq("isdel", false).findCount();
	}

	@Override
	public List<Service> getServiceByEnvId(String envId) {
		return this.server.createQuery(Service.class).where().eq("envId", envId).eq("isdel", false).findList();
	}

	@Override
	public void updateService(Service service) {
		server.update(service);
	}

	@Override
	public List<Service> queryServiceListByGroupId(String groupId) {
		return server.createQuery(Service.class).where().eq("serviceGroupId", groupId).eq("isdel", false).findList();
	}

	/**
	 * 根据ids和status查询
	 *
	 * @param s
	 * @return java.util.List<com.ladeit.pojo.doo.Service>
	 * @author falcomlife
	 * @date 20-4-21
	 * @version 1.0.0
	 */
	@Override
	public List<Service> getService(Service s) {
		return this.server.createQuery(Service.class).where().idIn(s.getIds()).eq("status",s.getStatus()).findList();
	}


	/**
	 * 根据ids和status查询
	 *
	 * @param s
	 * @return java.util.List<com.ladeit.pojo.doo.Service>
	 * @author falcomlife
	 * @date 20-4-21
	 * @version 1.0.0
	 */
	@Override
	public List<Service> getService(Service s,List<String> status) {
		return this.server.createQuery(Service.class).where().idIn(s.getIds()).in("status",status).findList();
	}
}

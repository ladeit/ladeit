package com.ladeit.biz.dao;

import com.ladeit.pojo.doo.Service;
import io.ebean.SqlRow;

import java.util.List;

/**
 * @program: ladeit
 * @description: ServiceDao
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
public interface ServiceDao {

	/**
	 * 更新service(只更新status。修改人。修改时间)
	 *
	 * @param service
	 * @return void
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	void update(Service service);

	void insert(Service service);

	Service queryServiceById(String serviceId);

	List<Service> queryServiceList(String groupid);

	//目前不使用了
	//Service queryServiceByGroupAndName (String token, String serviceName);

	List<Service> queryServiceListByParam(String serviceId, String serviceGroup, String serviceName);

	Service queryServiceByGroupAndName(String groupId, String serviceName);

	/**
	 * 服务假删除
	 *
	 * @param service
	 * @return void
	 * @date 2019/12/2
	 * @ahthor MddandPyy
	 */
	void delete(Service service);

	/**
	 * 查询service
	 *
	 * @param serviceId
	 * @return com.ladeit.pojo.doo.Service
	 * @author falcomlife
	 * @date 19-12-12
	 * @version 1.0.0
	 */
	Service getById(String serviceId);

	/**
	 * 根据id更新状态
	 *
	 * @param service
	 * @return void
	 * @author falcomlife
	 * @date 20-4-8
	 * @version 1.0.0
	 */
	void updateStatusById(Service service);

	/**
	 * 根绝groupid查询service
	 *
	 * @param groupId
	 * @return
	 */
	List<Service> getServiceByGroupId(String groupId);

	Service getServiceByToken(String token);

	int getImageNum(String serviceId);

	/**
	 * 根绝envid查询service
	 *
	 * @param envId
	 * @return
	 */
	List<Service> getServiceByEnvId(String envId);

	void updateService(Service service);

	List<Service> queryServiceListByGroupId(String groupId);

	/**
	* 根据ids和status查询
	* @author falcomlife
	* @date 20-4-21
	* @version 1.0.0
	* @return java.util.List<com.ladeit.pojo.doo.Service>
	* @param s
	*/
	List<Service> getService(Service s);

	/**
	 * 根据ids和status查询
	 *
	 * @param s
	 * @return java.util.List<com.ladeit.pojo.doo.Service>
	 * @author falcomlife
	 * @date 20-4-21
	 * @version 1.0.0
	 */
	List<Service> getService(Service s, List<String> status);
}

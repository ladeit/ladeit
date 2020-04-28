package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.doo.*;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1ReplicationController;
import io.kubernetes.client.models.V1StatefulSet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @program: ladeit
 * @description: ReleaseService
 * @author: falcomlife
 * @create: 2019/11/06
 * @version: 1.0.0
 */
public interface ReleaseService {
	/**
	 * 新建release
	 *
	 * @param release
	 * @param service
	 * @param candidate
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	ExecuteResult<String> newRelease(String serviceId, Release release, Service service, Candidate candidate,
									 ResourceAO resourceAO,
									 ConfigurationAO configuration) throws IOException,
			ApiException, InvocationTargetException, IllegalAccessException, NoSuchMethodException;


	/**
	 * 自动新建release
	 *
	 * @param release
	 * @param service
	 * @param candidate
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-11-6
	 * @version 1.0.0
	 */
	ExecuteResult<String> newReleaseAuto(String serviceId, Release release, Service service, Candidate candidate,
										 ResourceAO resourceAO,
										 ConfigurationAO configuration) throws IOException,
			ApiException, InvocationTargetException, IllegalAccessException, NoSuchMethodException;

	/**
	 * 根据serviceid得到服役中的release
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Release>
	 * @author falcomlife
	 * @date 19-11-11
	 * @version 1.0.0
	 */
	ExecuteResult<Release> getInUseReleaseByServiceId(String serviceId);


	/**
	 * 查询某个release
	 *
	 * @param releaseId,releaseName
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<ReleaseAO>> queryReleaseInfo(String releaseId, String releaseName);

	/**
	 * 根据releaseID查询某个release
	 *
	 * @param releaseId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	ExecuteResult<QueryReleaseAO> queryOneReleaseInfo(String releaseId);

	/**
	 * 查询服务发布信息
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ReleaseAO>>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<ReleaseAO>> queryReleases(String serviceId, int currentPage, int pageSize);

	/**
	 * 升级服务
	 *
	 * @param release
	 * @param service
	 * @param candidate
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	ExecuteResult<String> refreshRelease(String serviceId, Release release, Service service, Candidate candidate,
										 TopologyAO topology, Boolean auto, ConfigurationAO configuration) throws IOException, ApiException;


	/**
	 * 自动升级服务
	 *
	 * @param release
	 * @param service
	 * @param candidate
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 19-12-13
	 * @version 1.0.0
	 */
	ExecuteResult<String> refreshReleaseAuto(String serviceId, Release release, Service service, Candidate candidate,
											 TopologyAO topology, Boolean auto, ConfigurationAO configuration) throws IOException, ApiException;

	/**
	 * 滚动更新逻辑
	 *
	 * @param service
	 * @param image
	 * @param release
	 * @param deploymentflag
	 * @param statefulSetflag
	 * @param replicationControllerflag
	 * @param deployment
	 * @param statefulSet
	 * @param replicationController
	 * @param config
	 * @param env
	 * @param candidate
	 * @param topology
	 * @return void
	 * @author falcomlife
	 * @date 20-3-18
	 * @version 1.0.0
	 */
	void rollingUpdate(Service service, Image image, Release release, Boolean deploymentflag,
					   Boolean statefulSetflag, Boolean replicationControllerflag, List<V1Deployment> deployment,
					   List<V1StatefulSet> statefulSet, List<V1ReplicationController> replicationController,
					   String config, Env env, Candidate candidate, TopologyAO topology,
					   ConfigurationAO configuration) throws ApiException, IOException;

	/**
	 * ab测试逻辑
	 *
	 * @param service
	 * @param image
	 * @param release
	 * @param deploymentflag
	 * @param statefulSetflag
	 * @param replicationControllerflag
	 * @param deployment
	 * @param statefulSet
	 * @param replicationController
	 * @param config
	 * @param env
	 * @param candidate
	 * @param topology
	 * @return void
	 * @author falcomlife
	 * @date 20-3-18
	 * @version 1.0.0
	 */
	void abTest(Service service, Image image, Release release, Boolean deploymentflag,
				Boolean statefulSetflag, Boolean replicationControllerflag, List<V1Deployment> deployment,
				List<V1StatefulSet> statefulSet, List<V1ReplicationController> replicationController,
				String config, Env env, Candidate candidate, TopologyAO topology, ConfigurationAO configuration) throws ApiException;

	/**
	* 根据serviceid查询release数据，按照部署时间从大到小排列
	* @author falcomlife
	* @date 20-4-10
	* @version 1.0.0
	* @return java.util.List<com.ladeit.pojo.doo.Release>
	* @param id
	*/
	List<Release> getReleaseList(String id);

	/**
	 * 修改状态，同事修改时间
	 *
	 * @param r1
	 * @return void
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	void updateStatus(Release r1);
	
	/**
	 * 查询发布中的release
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.Release>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	ExecuteResult<Release> getInUpdateRelease(String serviceId);
}

package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.doo.Cluster;
import io.ebean.SqlRow;
import io.kubernetes.client.ApiException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @program: ladeit
 * @description: ClusterService
 * @author: falcomlife
 * @create: 2019/11/07
 * @version: 1.0.0
 */
public interface ClusterService {
	Cluster getClusterById(String id);

	/**
	 * 创建cluster
	 *
	 * @param bzK8sClusterBO
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	ExecuteResult<String> createCluster(Cluster bzK8sClusterBO) throws IOException, ApiException, InterruptedException;

	/**
	 * 更新cluster
	 *
	 * @param bzK8sClusterBO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> updateCluster(String clusterId, Cluster bzK8sClusterBO);

	/**
	 * 查询集群列表
	 *
	 * @param bzK8sClusterBO
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	ExecuteResult<List<Cluster>> getCluster(Cluster bzK8sClusterBO);

	/**
	 * 查询cluster列表
	 *
	 * @param bzK8sClusterBO
	 * @author falcomlife
	 * @date 19-9-19
	 * @version 1.0.0
	 */
	ExecuteResult<Cluster> getOneCluster(Cluster bzK8sClusterBO);

	/**
	 * 查询namespace
	 *
	 * @param clusterId
	 * @author falcomlife
	 * @date 19-9-20
	 * @version 1.0.0
	 */
	ExecuteResult<List<String>> listNamespace(String clusterId) throws IOException;

	/**
	 * 查询namespace下的资源
	 *
	 * @param clusterId
	 * @param namespace
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ResourceAO>
	 * @author falcomlife
	 * @date 19-11-15
	 * @version 1.0.0
	 */
	ExecuteResult<ResourceAO> getResourceInNamespace(String serviceId, String clusterId, String namespace) throws ApiException,
			IOException, InvocationTargetException, IllegalAccessException;


	/**
	 * 查询集群下人员信息(不分页)
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.UserClusterRelationAO>>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<UserClusterRelationAO>> queryNoPagerClusterUserInfo(String clusterId);

	/**
	 * 查询要加入的人员信息
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<AddServiceGroupUserAO>> queryAddClusterUserInfo(String clusterId, String userName,
																	String email);

	/**
	 * 添加集群人员
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> addClusterRelation(String clusterId, UserClusterRelationAO userClusterRelationAO);

	/**
	 * 查询集群邀请码
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> inviteUser(String clusterId);

	/**
	 * 添加集群人员(通过邀请码)
	 *
	 * @param inviteCode
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> addClusterRelationByInviteCode(String inviteCode);

	/**
	 * 添加集群人员(多个)
	 *
	 * @param userClusterRelationAOS
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> addClusterRelationList(String clusterId, List<UserClusterRelationAO> userClusterRelationAOS);

	/**
	 * 更新人员集群权限信息
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> updateClusterRelatio(String clusterId, UserClusterRelationAO userClusterRelationAO);

	/**
	 * 更新人员命名空间权限信息
	 *
	 * @param userEnvRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> updateEnvRelatio(String clusterId, UserEnvRelationAO userEnvRelationAO);

	/**
	 * 删除集群人员，及其在集群下所有的命名空间权限
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> deleteClusterRelation(String clusterId, UserClusterRelationAO userClusterRelationAO);

	/**
	 * 查询集群列表(集群和集群下的环境)根据当前登录人
	 *
	 * @param
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ClusterAO>>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<Cluster>> queryClusterByUser();

	/**
	 * 查询集群列表(集群和集群下的环境)分页
	 *
	 * @param currentPage, pageSize
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.ClusterAO>>
	 * @date 2020/2/10
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<ClusterAO>> getClusterAndEnvPager(int currentPage, int pageSize);

	/**
	 * 查询集群列表(集群和集群下的环境)分页
	 *
	 * @param currentPage, pageSize
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.ClusterAO>>
	 * @date 2020/2/10
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<SqlRow>> getClusterAndEnvPagerSqlrow(int currentPage, int pageSize, String orderparam);

	/**
	 * 获取用户-集群关系
	 *
	 * @param userId
	 * @param clusterId
	 * @return java.lang.String
	 * @author MddandPyy
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	String getUserClusterLevel(String userId, String clusterId);

	/**
	 * 获取用户-env关系
	 *
	 * @param userId
	 * @param envId
	 * @return java.lang.String
	 * @author MddandPyy
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	String getUserEnvLevel(String userId, String envId);

	/**
	 * 根据集群名查询集群信息（有权限校验）
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ClusterAO>
	 * @date 2020/3/16
	 * @ahthor MddandPyy
	 */
	ExecuteResult<ClusterAO> getOneClusterById(String clusterId);

	/**
	 * 删除集群,及其在集群下所有的命名空间,以及人员权限
	 *
	 * @param clusterId,clusterAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/3/17
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> deleteCluster(String clusterId, ClusterAO clusterAO);

	/**
	 * 当前登录人离开分组，无需校验权限
	 *
	 * @param userClusterRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/2/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> deleteClusterRelationBylogin(UserClusterRelationAO userClusterRelationAO);

	/**
	 * 查询cluster下的env上挂的service，用于删除集群前的校验
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ServiceAO>
	 * @date 2020/3/18
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<ServiceAO>> getEnvService(String clusterId);

	/**
	 * 更新集群下的命名空间
	 *
	 * @param clusterId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @author falcomlife
	 * @date 20-5-25
	 * @version 1.0.0
	 */
	ExecuteResult<String> refreshNamespace(String clusterId) throws IOException, ApiException;
}
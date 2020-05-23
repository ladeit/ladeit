package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.doo.Certificate;
import com.ladeit.pojo.doo.ServiceGroup;
import io.ebean.SqlRow;
import io.kubernetes.client.ApiException;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ServiceGroupService
 * @Date 2019/11/6 13:23
 */
public interface ServiceGroupService {

	/**
	 * 创建服务组
	 *
	 * @param serviceGroupAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/6
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> addServiceGroup(ServiceGroupAO serviceGroupAO);

	/**
	 * ci调用接口，传递servicetoken，实现对service镜像的添加
	 *
	 * @param addServiceAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/6
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> addService(AddServiceAO addServiceAO) throws IOException, ApiException;

	/**
	 * 删除服务组
	 *
	 * @param groupId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> deleteServiceGroup(String groupId, ServiceGroupAO serviceGroupAO) throws IOException;

	/**
	 * 查询服务组以及组下面的服务信息
	 *
	 * @param groupName
	 * @return com.ladeit.common.ExecuteResult<java.util.List < com.ladeit.pojo.ao.QueryServiceGroupAO>>
	 * @date 2019/11/7
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<QueryServiceGroupAO>> queryServiceGroupInfo(String groupName) throws IOException;

	/**
	 * 查询服务组以及组下面的服务信息(管理员)
	 *
	 * @param groupName
	 * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.QueryServiceGroupAO>>
	 * @author falcomlife
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	ExecuteResult<List<QueryServiceGroupAO>> queryAdminServiceGroupInfo(String groupName) throws IOException;

	/**
	 * 查询服务组以及组下面的服务信息(分页)
	 *
	 * @param groupName
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager < com.ladeit.pojo.ao.QueryServiceGroupAO>>
	 * @date 2020/2/10
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<QueryServiceGroupAO>> queryServiceGroupPagerInfo(int currentPage, int pageSize,
 String groupName);

	/**
	 * 查询服务组以及组下面的服务信息(分页),后期优化查询返回sqlrow
	 *
	 * @param groupName
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager < com.ladeit.pojo.ao.QueryServiceGroupAO>>
	 * @date 2020/2/10
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<SqlRow>> queryServiceGroupSqlrowPagerInfo(int currentPage, int pageSize, String groupName,
String orderparam);

	/**
	 * 查询服务下的镜像
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List < com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/7
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<QueryImageAO>> queryImagesByServiceId(String serviceId);

	/**
	 * 查询服务下的镜像(分页)
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List < com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/7
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<QueryImageAO>> queryPageImagesByServiceId(int currentPage, int pageSize, String serviceId,
String startDate, String endDate) throws ParseException;

	/**
	 * 查询某个服务
	 *
	 * @param serviceId
	 * @return com.ladeit.common.ExecuteResult<java.util.List < com.ladeit.pojo.ao.ImageAO>>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<QueryServiceAO>> queryServiceInfo(String serviceId, String serviceGroup, String serviceName) throws IOException;

	/**
	 * 更新镜像
	 *
	 * @param imageAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/11
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> updateImage(String imageId, ImageAO imageAO);

	/**
	 * 查询服务组下人员信息
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager < com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/11/25
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<ServiceGroupUserAO>> querySeriveGroupUserInfo(String serviceGroupId, int currentPage,
 int pageSize);

	/**
	 * 查询服务组下人员信息(不分页)
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager < com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<ServiceGroupUserAO>> queryNoPagerSeriveGroupUserInfo(String serviceGroupId);

	/**
	 * 查询人员在某服务组下各服务中的权限信息
	 *
	 * @param serviceGroupId,userId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager < com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/11/25
	 * @ahthor MddandPyy
	 */
	ExecuteResult<Pager<ServiceUserAO>> querySeriveUserInfo(int currentPage, int pageSize, String serviceGroupId,
String userId);

	/**
	 * 更新人员服务组权限信息
	 *
	 * @param userServiceGroupRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> updateServiceGroupRelation(String groupId,
  UserServiceGroupRelationAO userServiceGroupRelationAO);

	/**
	 * 更新人员服务权限信息
	 *
	 * @param userServiceRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> updateServiceRelation(String serviceGroupId, UserServiceRelationAO userServiceRelationAO);

	/**
	 * 查询要添加的用户信息
	 *
	 * @param serviceGroupId, userName, email
	 * @return com.ladeit.common.ExecuteResult<java.util.List < com.ladeit.pojo.ao.AddServiceGroupUserAO>>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<AddServiceGroupUserAO>> queryAddSeriveGroupUserInfo(String serviceGroupId, String userName,
String email);

	/**
	 * 往组内添加人员
	 *
	 * @param userServiceGroupRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> addServiceGroupRelation(String groupId,
  UserServiceGroupRelationAO userServiceGroupRelationAO);

	/**
	 * 往组内添加人员(多个)
	 *
	 * @param userServiceGroupRelationListAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> addServiceGroupRelationList(String groupId,
List<UserServiceGroupRelationAO> userServiceGroupRelationListAO);

	/**
	 * 添加服务组人员(通过邀请码)
	 *
	 * @param inviteCode
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/11/6
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> addServiceGroupRelationByInviteCode(String inviteCode);

	/**
	 * 删除组内人员
	 *
	 * @param userServiceGroupRelationAO
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> deleteServiceGroupRelation(String groupId,
  UserServiceGroupRelationAO userServiceGroupRelationAO);

	/**
	 * 当前登录人离开分组
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/1/2
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> leaveServiceGroup(String serviceGroupId);

	/**
	 * 查询服务组token
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.CertificateAO>
	 * @date 2019/12/5
	 * @ahthor MddandPyy
	 */
	ExecuteResult<CertificateAO> getGroupToken(String serviceGroupId);

	/**
	 * 更新分组token
	 *
	 * @param groupId
	 * @param certificateAO
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.CertificateAO>
	 * @author MddandPyy
	 * @date 20-4-10
	 * @version 1.0.0
	 */
	ExecuteResult<CertificateAO> updateGroupToken(String groupId, CertificateAO certificateAO);

	/**
	 * 查询服务组邀请码信息
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager < com.ladeit.pojo.ao.SeriveGroupUserAO>>
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> inviteUser(String serviceGroupId);

	/**
	 * 更新服务组名字
	 *
	 * @param serviceGroupAO
	 * @return
	 * @date 2019/12/4
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> updateGroupName(String groupId, ServiceGroupAO serviceGroupAO);

	/**
	 * 查询服务组绑定的slack channel信息
	 *
	 * @param serviceGroupId
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ChannelServiceGroupAO>
	 * @date 2020/1/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<List<ChannelServiceGroupAO>> getChannelInfo(String serviceGroupId);

	/**
	 * 解绑serviceGroup和channel
	 *
	 * @param channelServiceGroupId
	 * @return com.ladeit.common.ExecuteResult<java.lang.String>
	 * @date 2020/1/14
	 * @ahthor MddandPyy
	 */
	ExecuteResult<String> unbindChannel(String channelServiceGroupId);

	String getServiceGroupId(String serviceId);

	/**
	 * 根据id查询group
	 *
	 * @param id
	 * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.doo.ServiceGroup>
	 * @author falcomlife
	 * @date 20-3-20
	 * @version 1.0.0
	 */
	ExecuteResult<ServiceGroup> getGroupById(String id);

	/**
	 * 查询某服务组下面的服务信息
	 *
	 * @param channelId
	 * @return com.ladeitbot.core.ladeitbotcore.common.ExecuteResult<java.util.List<com.ladeitbot.pojo.ladeitbotpojo.ao.ServiceAO>>
	 * @date 2019/12/25
	 * @ahthor MddandPyy
	 */
	ExecuteResult<BotQueryServiceAO> queryServiceGroupInfoBychannel(String slackUserId,String token, String channelId);


	/**
	 * 加入serviceGroup
	 *
	 * @param joinServiceGroupAO
	 * @return com.ladeitbot.core.ladeitbotcore.common.ExecuteResult<com.ladeitbot.pojo.ladeitbotpojo.ao.ResultAO>
	 * @date 2020/1/8
	 * @ahthor MddandPyy
	 */
	ExecuteResult<ResultAO> joinServiceGroup(String slackUserId,String token,JoinServiceGroupAO joinServiceGroupAO);

	ExecuteResult<BotQueryImageAO> queryServiceImageInfo(String slackUserId,String token, String channelId, String serviceName);

}

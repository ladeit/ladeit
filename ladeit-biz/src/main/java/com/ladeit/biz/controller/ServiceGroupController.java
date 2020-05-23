package com.ladeit.biz.controller;

import com.ladeit.biz.dao.ImageDao;
import com.ladeit.biz.dao.ServiceDao;
import com.ladeit.biz.dao.ServiceGroupDao;
import com.ladeit.biz.services.ImageService;
import com.ladeit.biz.services.ReleaseService;
import com.ladeit.biz.services.ServiceGroupService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Pager;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.doo.Certificate;
import com.ladeit.pojo.doo.Image;
import com.ladeit.pojo.doo.Service;
import com.ladeit.pojo.doo.ServiceGroup;
import io.ebean.SqlRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * 与组相关操作
 * @author MddandPyy
 * @version V1.0
 * @Classname GroupController
 * @Date 2019/11/6 11:37
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/servicegroup")
public class ServiceGroupController {

    @Autowired
    private ServiceGroupService serviceGroupService;

    @Autowired
    private ReleaseService releaseService;

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    private ServiceGroupDao serviceGroupDao;

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private ImageService imageService;

    /**
     * 添加服务组
     * @param serviceGroupAO
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2019/11/6
     * @ahthor MddandPyy
     */
    @PostMapping("/add")
    public ExecuteResult<String> addServiceGroup(@RequestBody ServiceGroupAO serviceGroupAO){
        return serviceGroupService.addServiceGroup(serviceGroupAO);
    }

    /**
     * 删除组
     * @param groupId
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2019/12/2
     * @ahthor MddandPyy
     */
    @DeleteMapping("/del/{group_id}")
    public ExecuteResult<String> deleteServiceGroup(@PathVariable("group_id") String groupId,@RequestBody ServiceGroupAO serviceGroupAO) throws IOException {

        return serviceGroupService.deleteServiceGroup(groupId,serviceGroupAO);
    }

    /**
     * 查询服务组以及组下面的服务信息
     * @param groupName
     * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.QueryServiceGroupAO>>
     * @date 2019/11/7
     * @ahthor MddandPyy
     */
    @GetMapping("/get")
    public ExecuteResult<List<QueryServiceGroupAO>>  queryServiceGroupInfo(@RequestParam(value="GroupName",required=false) String groupName) throws IOException {
        return serviceGroupService.queryServiceGroupInfo(groupName);
    }

    /**
     * 查询服务组以及组下面的服务信息
     * @param groupName
     * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.QueryServiceGroupAO>>
     * @date 2019/11/7
     * @ahthor MddandPyy
     */
    @GetMapping("/get/admin")
    public ExecuteResult<List<QueryServiceGroupAO>>  queryAdminServiceGroupInfo(@RequestParam(value="GroupName",required=false) String groupName) throws IOException {
        return serviceGroupService.queryAdminServiceGroupInfo(groupName);
    }

    /**
     * 查询服务组以及组下面的服务信息(分页)
     * @param groupName
     * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.QueryServiceGroupAO>>
     * @date 2020/2/10
     * @ahthor MddandPyy
     */
    @GetMapping("/getPager")
    public ExecuteResult<Pager<QueryServiceGroupAO>>  queryServiceGroupPagerInfo(@RequestParam("currentPage") int currentPage,
                                                                                 @RequestParam("pageSize") int pageSize,@RequestParam(value="GroupName",required=false) String groupName) throws IOException {
        return serviceGroupService.queryServiceGroupPagerInfo(currentPage,pageSize,groupName);
    }

    /**
     * 查询服务组以及组下面的服务信息(分页),后期优化查询返回sqlrow
     * @param groupName
     * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.QueryServiceGroupAO>>
     * @date 2020/2/10
     * @ahthor MddandPyy
     */
    @GetMapping("/getPagerSqlrow")
    public ExecuteResult<Pager<SqlRow>> queryServiceGroupSqlrowPagerInfo(@RequestParam("currentPage") int currentPage,
                                                                    @RequestParam("pageSize") int pageSize, @RequestParam(value="GroupName",required=false) String groupName,@RequestParam(value="OrderParam",required=false) String orderparam) throws IOException {
        return serviceGroupService.queryServiceGroupSqlrowPagerInfo(currentPage,pageSize,groupName,orderparam);
    }

    /**
     * 查询服务下的镜像
     * @param serviceId
     * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ImageAO>>
     * @date 2019/11/7
     * @ahthor MddandPyy
     */
    @GetMapping("/getImages/{ServiceId}")
    public ExecuteResult<List<QueryImageAO>> queryImagesByServiceId(@PathVariable("ServiceId") String serviceId){
        return serviceGroupService.queryImagesByServiceId(serviceId);
    }

    /**
     * 查询服务下的镜像(分页)
     * @param serviceId
     * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ImageAO>>
     * @date 2019/11/7
     * @ahthor MddandPyy
     */
    @GetMapping("/getPageImages/{ServiceId}")
    public ExecuteResult<Pager<QueryImageAO>> queryPageImagesByServiceId(@RequestParam("currentPage") int currentPage,
                                                                         @RequestParam("pageSize") int pageSize,@PathVariable("ServiceId") String serviceId,@RequestParam(value="StartDate",required=false) String startDate,@RequestParam(value="EndDate",required=false) String endDate) throws ParseException {
        return serviceGroupService.queryPageImagesByServiceId(currentPage,pageSize,serviceId,startDate,endDate);
    }


    /**
     * 查询某个服务
     * @param serviceId
     * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ImageAO>>
     * @date 2019/11/11
     * @ahthor MddandPyy
     */
    @GetMapping("/getService")
    public ExecuteResult<List<QueryServiceAO>> queryServiceInfo(@RequestParam(value="ServiceId",required=false) String serviceId,@RequestParam(value="ServiceGroup",required=false) String serviceGroup,@RequestParam(value="ServiceName",required=false) String serviceName) throws IOException {
        ExecuteResult<List<QueryServiceAO>> result = new ExecuteResult<>();
        if(!(serviceGroup==null || serviceGroup.trim().length()==0)){
            ServiceGroup group = serviceGroupDao.queryServiceByNameIsDel(serviceGroup);
            if(group!=null){
                if(!(serviceName==null || serviceName.trim().length()==0)){
                    Service service = serviceDao.queryServiceByGroupAndName(group.getId(),serviceName);
                    if(service!=null){
                        serviceId = service.getId();
                        result =  serviceGroupService.queryServiceInfo(serviceId,serviceGroup,serviceName);
                    }
                }
            }
        }
        return result;
    }

   /**
    * 根据组名、服务名、镜像版本查询镜像详情
    * @param serviceGroup, serviceName, imageName
    * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.QueryServiceAO>>
    * @date 2020/3/17
    * @ahthor MddandPyy
    */
    @GetMapping("/service/image")
    public ExecuteResult<QueryImageAO> queryImageInfo(@RequestParam(value="ServiceGroup") String serviceGroup,@RequestParam(value="ServiceName") String serviceName,@RequestParam(value="ImageVersion") String imageVersion){
        ExecuteResult<QueryImageAO> result = new ExecuteResult<>();
        if(!(serviceGroup==null || serviceGroup.trim().length()==0)){
            ServiceGroup group = serviceGroupDao.queryServiceByNameIsDel(serviceGroup);
            if(group!=null){
                if(!(serviceName==null || serviceName.trim().length()==0)){
                    Service service = serviceDao.queryServiceByGroupAndName(group.getId(),serviceName);
                    if(service!=null){
                        Image image = imageDao.getImageByServiceAndName(service.getId(),imageVersion);
                        result =  imageService.getImageById(service.getId(),image.getId());
                    }
                }
            }
        }
        return result;
    }

    /**
     * 更新镜像
     * @param imageAO
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2019/11/11
     * @ahthor MddandPyy
     */
    @PutMapping("/Image/{ImageId}")
    public ExecuteResult<String> updateImage(@PathVariable("ImageId") String ImageId,@RequestBody ImageAO imageAO) {
        return serviceGroupService.updateImage(ImageId,imageAO);
    }


    /**
     * 查询服务发布信息
     * @param serviceId
     * @return com.ladeit.common.ExecuteResult<java.util.List<com.ladeit.pojo.ao.ReleaseAO>>
     * @date 2019/11/11
     * @ahthor MddandPyy
     */
    @GetMapping("/getReleases")
    public ExecuteResult<Pager<ReleaseAO>> queryReleases(@RequestParam("ServiceId") String serviceId,@RequestParam("currentPage") int currentPage,
    @RequestParam("pageSize") int pageSize){
        return releaseService.queryReleases(serviceId,currentPage,pageSize);
    }

    /**
     * 查询服务组下人员信息
     * @param serviceGroupId
     * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
     * @date 2019/11/25
     * @ahthor MddandPyy
     */
    @GetMapping("/getUsers")
    public ExecuteResult<Pager<ServiceGroupUserAO>> querySeriveGroupUserInfo(@RequestParam("currentPage") int currentPage,
                                                                             @RequestParam("pageSize") int pageSize,@RequestParam("ServiceGroupId") String serviceGroupId){
        return serviceGroupService.querySeriveGroupUserInfo(serviceGroupId,currentPage,pageSize);
    }

    /**
     * 查询人员在某服务组下各服务中的权限信息
     * @param serviceGroupId,userId
     * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
     * @date 2019/11/25
     * @ahthor MddandPyy
     */
    @GetMapping("/getSeriveUserInfo")
    public ExecuteResult<Pager<ServiceUserAO>> querySeriveUserInfo(@RequestParam("currentPage") int currentPage,
                                                                             @RequestParam("pageSize") int pageSize,@RequestParam("ServiceGroupId") String serviceGroupId,@RequestParam("UserId") String userId){
        return serviceGroupService.querySeriveUserInfo(currentPage,pageSize,serviceGroupId,userId);
    }

    /**
     * 查询服务组下人员信息(不分页)
     * @param serviceGroupId
     * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
     * @date 2019/12/4
     * @ahthor MddandPyy
     */
    @GetMapping("/getNoPagerUsers")
    public ExecuteResult<List<ServiceGroupUserAO>> queryNoPagerSeriveGroupUserInfo(@RequestParam("ServiceGroupId") String serviceGroupId){
        return serviceGroupService.queryNoPagerSeriveGroupUserInfo(serviceGroupId);
    }

    /**
     * 更新人员服务组权限信息
     * @param userServiceGroupRelationAO
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2019/12/4
     * @ahthor MddandPyy
     */
    @PutMapping("/updateServiceGroupRelatio")
    public ExecuteResult<String> updateServiceGroupRelation(@RequestBody UserServiceGroupRelationAO userServiceGroupRelationAO) {
        return serviceGroupService.updateServiceGroupRelation(userServiceGroupRelationAO.getServiceGroupId(),userServiceGroupRelationAO);
    }

    /**
     * 更新人员服务权限信息
     * @param userServiceRelationAO
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2019/12/4
     * @ahthor MddandPyy
     */
    @PutMapping("/updateServiceRelatio")
    public ExecuteResult<String> updateServiceRelation(@RequestBody UserServiceRelationAO userServiceRelationAO) {
        String serviceGroupId = serviceGroupService.getServiceGroupId(userServiceRelationAO.getServiceId());
        return serviceGroupService.updateServiceRelation(serviceGroupId,userServiceRelationAO);
    }

    /**
     * 查询要加入的人员信息
     * @param serviceGroupId
     * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
     * @date 2019/12/4
     * @ahthor MddandPyy
     */
    @GetMapping("/getAddUsers")
    public ExecuteResult<List<AddServiceGroupUserAO>> queryAddSeriveGroupUserInfo(@RequestParam("ServiceGroupId") String serviceGroupId,@RequestParam(value="UserName", required=false) String userName,@RequestParam(value="Email", required=false) String email){
            return serviceGroupService.queryAddSeriveGroupUserInfo(serviceGroupId,userName,email);
    }

    /**
     * 添加服务组人员
     * @param userServiceGroupRelationAO
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2019/11/6
     * @ahthor MddandPyy
     */
    @PostMapping("/addServiceGroupRelation")
    public ExecuteResult<String> addServiceGroupRelation(@RequestBody UserServiceGroupRelationAO userServiceGroupRelationAO){
        return serviceGroupService.addServiceGroupRelation(userServiceGroupRelationAO.getServiceGroupId(),userServiceGroupRelationAO);
    }

    /**
     * 添加服务组人员(通过邀请码)
     * @param inviteCode
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2019/11/6
     * @ahthor MddandPyy
     */
    @PostMapping("/addServiceGroupRelation/{inviteCode}")
    public ExecuteResult<String> addServiceGroupRelationByInviteCode(@PathVariable("inviteCode") String inviteCode){
        return serviceGroupService.addServiceGroupRelationByInviteCode(inviteCode);
    }

    /**
     * 添加服务组人员(多个)
     * @param userServiceGroupRelationListAO
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2019/11/6
     * @ahthor MddandPyy
     */
    @PostMapping("/addServiceGroupRelationList")
    public ExecuteResult<String> addServiceGroupRelationList(@RequestBody List<UserServiceGroupRelationAO> userServiceGroupRelationListAO){
        UserServiceGroupRelationAO userServiceGroupRelationAO = userServiceGroupRelationListAO.get(0);
        return serviceGroupService.addServiceGroupRelationList(userServiceGroupRelationAO.getServiceGroupId(),userServiceGroupRelationListAO);
    }

    /**
     * 删除服务组人员，及其在服务组下所有的服务权限
     * @param userServiceGroupRelationAO
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2019/11/6
     * @ahthor MddandPyy
     */
    @DeleteMapping("/deleteServiceGroupRelation")
    public ExecuteResult<String> deleteServiceGroupRelation(@RequestBody UserServiceGroupRelationAO userServiceGroupRelationAO){
        return serviceGroupService.deleteServiceGroupRelation(userServiceGroupRelationAO.getServiceGroupId(),userServiceGroupRelationAO);
    }

    /**
     * 当前登录人离开分组
     * @param serviceGroupId
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2020/1/2
     * @ahthor MddandPyy
     */
    @DeleteMapping("/leave/{ServiceGroupId}")
    public ExecuteResult<String> leaveServiceGroup(@PathVariable("ServiceGroupId") String serviceGroupId){
        return serviceGroupService.leaveServiceGroup(serviceGroupId);
    }

    /**
     * 查询服务组token信息
     * @param serviceGroupId
     * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
     * @date 2019/12/4
     * @ahthor MddandPyy
     */
    @GetMapping("/getGroupToken")
    public ExecuteResult<CertificateAO> getGroupToken(@RequestParam("ServiceGroupId") String serviceGroupId){
        return serviceGroupService.getGroupToken(serviceGroupId);
    }

    /**
     * 更新服务组token信息
     * @param certificateAO
     * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
     * @date 2019/12/4
     * @ahthor MddandPyy
     */
    @PutMapping("/updateGroupToken")
    public ExecuteResult<CertificateAO> updateGroupToken(@RequestBody CertificateAO certificateAO){
        return serviceGroupService.updateGroupToken(certificateAO.getServiceGroupId(),certificateAO);
    }

    /**
     * 查询服务组邀请码信息
     * @param serviceGroupId
     * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
     * @date 2019/12/4
     * @ahthor MddandPyy
     */
    @GetMapping("/getInviteCode")
    public ExecuteResult<String> inviteUser(@RequestParam("ServiceGroupId") String serviceGroupId){
        return serviceGroupService.inviteUser(serviceGroupId);
    }

    /**
     * 更新服务组名字
     * @param serviceGroupAO
     * @return com.ladeit.common.ExecuteResult<com.ladeit.common.Pager<com.ladeit.pojo.ao.SeriveGroupUserAO>>
     * @date 2019/12/4
     * @ahthor MddandPyy
     */
    @PutMapping("/updateGroupName")
    public ExecuteResult<String> updateGroupName(@RequestBody ServiceGroupAO serviceGroupAO){
        return serviceGroupService.updateGroupName(serviceGroupAO.getId(),serviceGroupAO);
    }

   /**
    * 查询服务组绑定的slack channel信息
    * @param serviceGroupId
    * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ChannelServiceGroupAO>
    * @date 2020/1/14
    * @ahthor MddandPyy
    */
    @GetMapping("/getChannel")
    public ExecuteResult<List<ChannelServiceGroupAO>> getChannelInfo(@RequestParam("ServiceGroupId") String serviceGroupId){
        return serviceGroupService.getChannelInfo(serviceGroupId);
    }

    /**
     * 解绑serviceGroup和channel
     * @param channelServiceGroupId
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2020/1/14
     * @ahthor MddandPyy
     */
    @DeleteMapping("/unbind/{channelServiceGroupId}")
    public ExecuteResult<String> unbindChannel(@PathVariable("channelServiceGroupId") String channelServiceGroupId){
        return serviceGroupService.unbindChannel(channelServiceGroupId);
    }


}

package com.ladeit.api.controller;

import com.ladeit.api.services.ChannelServiceGroupService;
import com.ladeit.biz.services.ServiceGroupService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Token.UserInfo;
import com.ladeit.common.Token.UserInfoUtil;
import com.ladeit.pojo.ao.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 与组相关操作
 * @author MddandPyy
 * @version V1.0
 * @Classname GroupController
 * @Date 2019/11/6 11:37
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/slack/servicegroup")
public class SlackServiceGroupController {

    @Autowired
    private ServiceGroupService serviceGroupService;

    @Autowired
    private ChannelServiceGroupService channelServiceGroupService;

    /**
     * 查询某服务组下面的服务信息
     * @param channelId
     * @return com.ladeitbot.core.ladeitbotcore.common.ExecuteResult<java.util.List<com.ladeitbot.pojo.ladeitbotpojo.ao.ServiceAO>>
     * @date 2019/12/25
     * @ahthor MddandPyy
     */
    @GetMapping("/service")
    public ExecuteResult<BotQueryServiceAO> queryServiceGroupInfo(@RequestParam("SlackUserId") String slackUserId, @RequestParam("token") String token, @RequestParam("ChannelId") String channelId){
        UserInfo u = new UserInfo();
        u.setSlackUserId(slackUserId);
        UserInfoUtil.setInfo(u);
        return serviceGroupService.queryServiceGroupInfoBychannel(slackUserId,token,channelId);
    }



   /**
    * 加入serviceGroup
    * @param joinServiceGroupAO
    * @return com.ladeitbot.core.ladeitbotcore.common.ExecuteResult<com.ladeitbot.pojo.ladeitbotpojo.ao.ResultAO>
    * @date 2020/1/8
    * @ahthor MddandPyy
    */
    @PostMapping("/joinServiceGroup")
    public ExecuteResult<ResultAO> joinServiceGroup(@RequestBody JoinServiceGroupAO joinServiceGroupAO){
        UserInfo u = new UserInfo();
        u.setSlackUserId(joinServiceGroupAO.getSlackUserId());
        UserInfoUtil.setInfo(u);
        return serviceGroupService.joinServiceGroup(joinServiceGroupAO.getSlackUserId(),joinServiceGroupAO.getToken(),joinServiceGroupAO);
    }

    /**
     * 查询某服务下面的镜像信息
     * @param channelId, serviceName
     * @return com.ladeitbot.core.ladeitbotcore.common.ExecuteResult<com.ladeitbot.pojo.ladeitbotpojo.ao.BotQueryServiceAO>
     * @date 2020/3/28
     * @ahthor MddandPyy
     */
    @GetMapping("/service/image")
    public ExecuteResult<BotQueryImageAO> queryServiceImageInfo(@RequestParam("channelId") String channelId, @RequestParam("serviceName") String serviceName, @RequestParam("SlackUserId") String slackUserId, @RequestParam("token") String token){
        UserInfo u = new UserInfo();
        u.setSlackUserId(slackUserId);
        UserInfoUtil.setInfo(u);
        return serviceGroupService.queryServiceImageInfo(slackUserId,token,channelId,serviceName);
    }

    /**
     * 绑定slack的channel和serviceGroup
     * @param channelServiceGroupAO
     * @return com.ladeitbot.core.ladeitbotcore.common.ExecuteResult<java.lang.String>
     * @date 2019/12/26
     * @ahthor MddandPyy
     */
    @PostMapping("/bind")
    public ExecuteResult<ResultAO> channelBindGroup(@RequestBody ChannelServiceGroupAO channelServiceGroupAO){
        UserInfo u = new UserInfo();
        u.setSlackUserId(channelServiceGroupAO.getSlackUserId());
        UserInfoUtil.setInfo(u);
        return channelServiceGroupService.channelBindGroup(channelServiceGroupAO.getSlackUserId(),channelServiceGroupAO.getToken(),channelServiceGroupAO);
    }

}

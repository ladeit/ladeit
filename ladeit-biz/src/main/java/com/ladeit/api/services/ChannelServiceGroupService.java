package com.ladeit.api.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.ChannelServiceGroupAO;
import com.ladeit.pojo.ao.ResultAO;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ServiceGroupService
 * @Date 2019/11/6 13:23
 */
public interface ChannelServiceGroupService {

    /**
     * 绑定slack的channel和serviceGroup
     * @param channelServiceGroupAO
     * @return com.ladeitbot.core.ladeitbotcore.common.ExecuteResult<java.lang.String>
     * @date 2019/12/26
     * @ahthor MddandPyy
     */
    ExecuteResult<ResultAO> channelBindGroup(String slackUserId,String token,ChannelServiceGroupAO channelServiceGroupAO);

}

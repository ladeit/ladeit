package com.ladeit.api.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.PublicationAO;
import com.ladeit.pojo.ao.ResultAO;
import com.ladeit.pojo.ao.UserSlackRelationAO;
import io.kubernetes.client.ApiException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname SlackService
 * @Date 2020/1/7 16:07
 */
public interface SlackService {
    ExecuteResult<String> setupSlackAndLadeit(UserSlackRelationAO userSlackRelationAO);

    ExecuteResult<ResultAO> beforeSetup(String slackUserId);

    ExecuteResult<String> rollingPublication(String slackUserId, String token,String imageId, PublicationAO publicationAO) throws InvocationTargetException, NoSuchMethodException, ApiException, IllegalAccessException, IOException;
}

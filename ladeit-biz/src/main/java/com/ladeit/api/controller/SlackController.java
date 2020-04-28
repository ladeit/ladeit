package com.ladeit.api.controller;

import com.ladeit.api.services.SlackService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.Token.UserInfo;
import com.ladeit.common.Token.UserInfoUtil;
import com.ladeit.pojo.ao.PublicationAO;
import com.ladeit.pojo.ao.ResultAO;
import com.ladeit.pojo.ao.UserSlackRelationAO;
import io.kubernetes.client.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * 与组相关操作
 * @author MddandPyy
 * @version V1.0
 * @Classname GroupController
 * @Date 2019/11/6 11:37
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/slack")
public class SlackController {

    @Autowired
    private SlackService slackService;

   /**
    * slack账户与ladeit账户关联
    * @param userSlackRelationAO
    * @return void
    * @date 2020/1/7
    * @ahthor MddandPyy
    */
    @PostMapping("/setup")
    public ExecuteResult<String> setupSlackAndLadeit(@RequestBody UserSlackRelationAO userSlackRelationAO){
        UserInfo u = new UserInfo();
        u.setSlackUserId(userSlackRelationAO.getSlackUserId());
        UserInfoUtil.setInfo(u);
        return slackService.setupSlackAndLadeit(userSlackRelationAO);
    }

    /**
     * 校验是否可以进行setup操作
     * @param slackUserId
     * @return void
     * @date 2020/1/8
     * @ahthor MddandPyy
     */
    @GetMapping("/beforeSetup")
    public ExecuteResult<ResultAO> beforeSetup(@RequestParam("slackUserId") String slackUserId){
        UserInfo u = new UserInfo();
        u.setSlackUserId(slackUserId);
        UserInfoUtil.setInfo(u);
        return slackService.beforeSetup(slackUserId);
    }

  /**
   * slack执行滚动发布
   * @param imageId
   * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ResultAO>
   * @date 2020/1/9
   * @ahthor MddandPyy
   */
    @PostMapping(value = "/RollingPublication/{imageId}",produces={"application/json;charset=utf-8"})
    public ExecuteResult<String> rollingPublication(@PathVariable("imageId") String imageId, @RequestBody PublicationAO publicationAO) throws NoSuchMethodException, ApiException, IllegalAccessException, IOException, InvocationTargetException {
        UserInfo u = new UserInfo();
        u.setSlackUserId(publicationAO.getSlackUserId());
        UserInfoUtil.setInfo(u);
        return slackService.rollingPublication(publicationAO.getSlackUserId(),publicationAO.getToken(),imageId,publicationAO);
    }


}

package com.ladeit.api.services.impl;

import com.alibaba.fastjson.JSON;
import com.ladeit.api.annotation.SlackLogin;
import com.ladeit.api.services.SlackService;
import com.ladeit.biz.dao.*;
import com.ladeit.biz.services.ReleaseService;
import com.ladeit.biz.utils.HttpHelper;
import com.ladeit.biz.utils.PropertiesUtil;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.*;
import com.ladeit.pojo.ao.release.Candidate;
import com.ladeit.pojo.doo.Image;
import com.ladeit.pojo.doo.Release;
import com.ladeit.pojo.doo.UserSlackRelation;
import com.ladeit.util.redis.RedisUtil;
import io.kubernetes.client.ApiException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname SlackServiceImpl
 * @Date 2020/1/7 16:08
 */
@Service
public class SlackServiceImpl implements SlackService {

    @Autowired
    private UserSlackRelationDao userSlackRelationDao;

    //@Value("${slack-node.host}")
    //@Value("${ladeit-bot-notif-mngr.host}")
    //private String host;

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    private ReleaseService releaseService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ReleaseDao releaseDao;

    @Autowired
    private ServiceGroupDao serviceGroupDao;

    @Autowired
    private PropertiesUtil propertiesUtil;

    /**
     * slack账户与ladeit账户关联
     * @param userSlackRelationAO
     * @return void
     * @date 2020/1/7
     * @ahthor MddandPyy
     */
    @Override
    public ExecuteResult<String> setupSlackAndLadeit(UserSlackRelationAO userSlackRelationAO) {
        ExecuteResult<String> result = new ExecuteResult<String>();
        UserSlackRelation relation = userSlackRelationDao.queryUserSlackRelationBySlackUserId(userSlackRelationAO.getSlackUserId());
        if(relation==null){
            UserSlackRelation userSlackRelation = new UserSlackRelation();
            BeanUtils.copyProperties(userSlackRelationAO,userSlackRelation);
            userSlackRelation.setId(UUID.randomUUID().toString());
            userSlackRelationDao.insert(userSlackRelation);
            try {
                //调用推送slack消息接口
//            Map<String,Object> param = new HashMap();
//            param.put("UserName",userSlackRelationAO.getUserName());
//            param.put("slackUserId",userSlackRelationAO.getSlackUserId());
//            param.put("channelId",userSlackRelationAO.getChannalId());
                //HttpHelper.doSendPost(host+"/setup", JSON.toJSONString(param), null);
                String host = propertiesUtil.getProperty("ladeit-bot-notif-mngr.host");
                HttpHelper.doSendPost(host+"/api/v1/message/setup", JSON.toJSONString(userSlackRelationAO), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Authorization succeeded.
            //result.setResult("授权成功");
            result.setResult("Authorization succeeded.");
        }else{
            // You have already bound.
            //result.setResult("你已经关联过ladeit账户");
            result.setResult("You have already bound a ladeit account.");
        }




        return result;
    }

    /**
     * 校验是否可以进行setup操作
     * @param slackUserId
     * @return void
     * @date 2020/1/8
     * @ahthor MddandPyy
     */
    @Override
    //@SlackLogin
    public ExecuteResult<ResultAO> beforeSetup(String slackUserId) {
        ExecuteResult<ResultAO> result = new ExecuteResult<ResultAO>();
        ResultAO resultAO = new ResultAO();
        UserSlackRelation userSlackRelation = userSlackRelationDao.queryUserSlackRelationBySlackUserId(slackUserId);
        if(userSlackRelation==null){
            resultAO.setFlag(true);
        }else{
            resultAO.setFlag(false);
            // "You have already bound " + userSlackRelation.getUserName()
            //resultAO.setMessage("你已关联了ladeit账户"+userSlackRelation.getUserName());
            resultAO.setMessage("You have bound " + userSlackRelation.getUserName() + " yet.");
        }
        result.setResult(resultAO);
        return result;
    }


    /**
     * slack执行滚动发布
     * @param imageId
     * @return com.ladeit.common.ExecuteResult<com.ladeit.pojo.ao.ResultAO>
     * @date 2020/1/9
     * @ahthor MddandPyy
     */
    @Override
    @SlackLogin
    public ExecuteResult<String> rollingPublication(String slackUserId,String token,String imageId, PublicationAO publicationAO) throws InvocationTargetException, NoSuchMethodException, ApiException, IllegalAccessException, IOException {
        ExecuteResult<String> result = new ExecuteResult<>();
        ReleaseAO releaseAO = new ReleaseAO();
        //Candidate信息
        Candidate c = new Candidate();
        c.setImageId(imageId);
        releaseAO.setCandidate(c);
        //service信息
        Image image = imageDao.getImageById(imageId);
        String serviceId = image.getServiceId();
        com.ladeit.pojo.doo.Service service = serviceDao.getById(serviceId);
        com.ladeit.pojo.ao.release.Service serviceAO = new com.ladeit.pojo.ao.release.Service();
        serviceAO.setEnvId(service.getEnvId());
        serviceAO.setId(service.getId());
        serviceAO.setName(service.getName());
        releaseAO.setService(serviceAO);
        //滚动发布
        releaseAO.setType(8);
        String status = service.getStatus();

        if("-1".equals(status)){
            //首次发布
            Release release = new Release();
            com.ladeit.pojo.doo.Service serviceDO = new com.ladeit.pojo.doo.Service();
            com.ladeit.pojo.doo.Candidate candidate = new com.ladeit.pojo.doo.Candidate();
            BeanUtils.copyProperties(releaseAO, release);
            BeanUtils.copyProperties(releaseAO.getCandidate(), candidate);
            BeanUtils.copyProperties(releaseAO.getService(), serviceDO);
            String name = "release none -> "+image.getVersion();
            release.setName(name);
            release.setOperChannel("Slack");
            this.releaseService.newRelease(serviceDO.getId(),release, serviceDO, candidate, releaseAO.getResourceAO(), releaseAO.getConfiguration());
            // "<@"+publicationAO.getSlackUserId()+"> deployed successfully."
            //result.setResult("<@"+publicationAO.getSlackUserId()+">滚动发布执行成功");
            result.setResult("<@"+publicationAO.getSlackUserId()+"> deployed successfully.");
            return result;
        }else{
            //非首次升级
            Release release = new Release();
            com.ladeit.pojo.doo.Service serviceDO = new com.ladeit.pojo.doo.Service();
            com.ladeit.pojo.doo.Candidate candidate = new com.ladeit.pojo.doo.Candidate();
            TopologyAO topology = new TopologyAO();
            BeanUtils.copyProperties(releaseAO, release);
            BeanUtils.copyProperties(releaseAO.getCandidate(), candidate);
            BeanUtils.copyProperties(releaseAO.getService(), serviceDO);
            if (releaseAO.getTopologyAO() != null) {
                BeanUtils.copyProperties(releaseAO.getTopologyAO(), topology);
            }
            String name = "release "+service.getImageVersion()+" -> "+image.getVersion();
            release.setName(name);
            release.setOperChannel("slack");

            //判断是否release中是否已经存在，同一个service的，创建时间比镜像时间大的release，如果存在说明这个镜像失效了，不能发布
            //true存在，false不存在
            boolean releaseexist = releaseDao.isReleaseNew(serviceId,image.getCreateAt());
            if(releaseexist){
                result.setCode(Code.STATUS_ERROR);
                // "<@"+publicationAO.getSlackUserId()+"> time out to deploy."
                //result.addErrorMessage("<@"+publicationAO.getSlackUserId()+"> 操作超时，无法进行滚动发布");
                result.addErrorMessage("Failed, <@"+publicationAO.getSlackUserId()+"> time out to deploy.");

                // result.setResult("镜像已失效，无法进行滚动发布");
            }else{
                ExecuteResult<String> re = this.releaseService.refreshRelease(serviceDO.getId(),release, serviceDO, candidate, topology,true, null);
                if(re.getCode()== Code.AUTH_ERROR){
                    result.setCode(Code.AUTH_ERROR);
                    // Permission denied.
                    //result.addErrorMessage("<@"+publicationAO.getSlackUserId()+">你的操作权限不足");
                    result.addErrorMessage("<@"+publicationAO.getSlackUserId()+"> Permission denied.");
                }
                if(re.getCode()== Code.STATUS_ERROR){
                    result.setCode(Code.STATUS_ERROR);
                    result.setErrorMessages(re.getErrorMessages());
                }
                else if(re.getCode()== Code.SUCCESS){
                    // "<@"+publicationAO.getSlackUserId()+"> deployed successfully."
                    //result.setResult("<@"+publicationAO.getSlackUserId()+">滚动发布执行成功");
                    result.setResult("<@"+publicationAO.getSlackUserId()+"> deployed successfully.");
                }

            }
            return result;
        }

    }
}

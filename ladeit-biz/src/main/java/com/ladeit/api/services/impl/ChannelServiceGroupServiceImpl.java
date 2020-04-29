package com.ladeit.api.services.impl;

import com.ladeit.api.services.ChannelServiceGroupService;
import com.ladeit.biz.dao.ChannelServiceGroupDao;
import com.ladeit.biz.dao.ServiceDao;
import com.ladeit.biz.dao.ServiceGroupDao;
import com.ladeit.biz.dao.UserSlackRelationDao;
import com.ladeit.biz.utils.PropertiesUtil;
import com.ladeit.common.ExecuteResult;
import com.ladeit.common.system.Code;
import com.ladeit.pojo.ao.ChannelServiceGroupAO;
import com.ladeit.pojo.ao.ResultAO;
import com.ladeit.pojo.doo.ChannelServiceGroup;
import com.ladeit.pojo.doo.ServiceGroup;
import com.ladeit.pojo.doo.UserSlackRelation;
import io.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ServiceGroupServiceImpl
 * @Date 2019/12/25 14:15
 */
@Service
public class ChannelServiceGroupServiceImpl implements ChannelServiceGroupService {

    @Autowired
    private ServiceDao serviceDao;

    @Autowired
    private ChannelServiceGroupDao channelServiceGroupDao;

    @Autowired
    private ServiceGroupDao serviceGroupDao;

    @Autowired
    private PropertiesUtil propertiesUtil;

    @Autowired
    private UserSlackRelationDao userSlackRelationDao;


    /**
     * 绑定slack的channel和serviceGroup
     * @param channelServiceGroupAO
     * @return com.ladeitbot.core.ladeitbotcore.common.ExecuteResult<java.lang.String>
     * @date 2019/12/26
     * @ahthor MddandPyy
     */
    @Override
    //@SlackLogin
    public ExecuteResult<ResultAO> channelBindGroup(String slackUserId,String token,ChannelServiceGroupAO channelServiceGroupAO) {
        ExecuteResult<ResultAO> result = new ExecuteResult<ResultAO>();
        ResultAO resultAO = new ResultAO();

        UserSlackRelation userSlackRelation = userSlackRelationDao.queryUserSlackRelationBySlackUserId(slackUserId);
        if(userSlackRelation==null){
            result.setCode(Code.FAILED);
            // You have not bind a ladeit account, use /ladeit setup to bind.
            //result.addErrorMessage("你还未关联ladeit账户，可以使用 `/ladeit setup` 进行关联");
            result.addErrorMessage("You have not bind a Ladeit account, use `/ladeit setup` to bind");
            return result;
        }


        String url = null;
        String channelId = channelServiceGroupAO.getChannelId();
        String channelName = channelServiceGroupAO.getChannelName();
        String groupparam = channelServiceGroupAO.getServicegroupName();
        String groupName = null;
        String host = propertiesUtil.getProperty("ladeit.host");
        if(groupparam!=null&&groupparam.contains(host)){
            url = groupparam;
            String[] strs = groupparam.split("/");
            groupName = strs[strs.length-1];
        }else{
            url = host+"/group/"+groupparam;
            groupName = groupparam;
        }
        ServiceGroup group = serviceGroupDao.queryServiceByName(groupName);
        if(group==null){
            resultAO.setFlag(false);
            // "There is no group named " + groupName
            //resultAO.setMessage("系统中未找到名为"+groupName+"的group");
            resultAO.setMessage("There is no group named " + groupName);

        }else{
            List<ChannelServiceGroup> channelServiceGroups = channelServiceGroupDao.queryInfoByGroupNameAndChannelId(channelId);
            if(channelServiceGroups.size()==0){
                ChannelServiceGroup channelServiceGroup = new ChannelServiceGroup();
                channelServiceGroup.setId(UUID.randomUUID().toString());
                channelServiceGroup.setCreateAt(new Date());
                channelServiceGroup.setChannelId(channelId);
                channelServiceGroup.setChannelName(channelName);
                channelServiceGroup.setServicegroupId(group.getId());
                channelServiceGroup.setServicegroupName(groupName);
                channelServiceGroupDao.insert(channelServiceGroup);
                resultAO.setFlag(true);
                // "#" + channelName + " bind " + groupName + " successfully."
                //resultAO.setMessage("当前#"+channelName+"成功绑定了服务组"+groupName);
                resultAO.setMessage("#" + channelName + " bind " + groupName + " successfully");

                Map<String,Object> map = new HashMap<>();
                map.put("url",url);
                map.put("channelId",channelId);
                map.put("channelName",channelName);
                map.put("groupName",groupName);
                List<SqlRow> member = serviceGroupDao.queryUsersByGroup(group.getId());
                map.put("member",member);
                map.put("groupId",group.getId());
                resultAO.setInfo(map);
            }else{
                for (ChannelServiceGroup info:channelServiceGroups) {
                    info.setCreateAt(new Date());
                    info.setChannelId(channelId);
                    info.setChannelName(channelName);
                    info.setServicegroupId(group.getId());
                    info.setServicegroupName(groupName);
                    channelServiceGroupDao.update(info);
                    resultAO.setFlag(true);
                    // "#" + channelName + " bind " + groupName + " successfully."
                    //resultAO.setMessage("当前#"+channelName+"成功绑定了服务组"+groupName);
                    resultAO.setMessage("#" + channelName + " bind " + groupName + " successfully");
                    Map<String,Object> map = new HashMap<>();
                    map.put("url",url);
                    map.put("channelId",channelId);
                    map.put("channelName",channelName);
                    map.put("groupName",groupName);
                    map.put("groupId",group.getId());
                    List<SqlRow> member = serviceGroupDao.queryUsersByGroup(group.getId());
                    map.put("member",member);
                    resultAO.setInfo(map);
                }
            }
        }
        result.setResult(resultAO);
        return result;
    }
}

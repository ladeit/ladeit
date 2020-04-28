package com.ladeit.pojo.ao;

import lombok.Data;

import java.util.Date;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname ChannelServiceGroup
 * @Date 2019/12/21 14:38
 */
@Data
public class ChannelServiceGroupAO {
    /**
     * id
     */
    private String id;

    /**
     * channel_id
     */
    private String channelId;

    /**
     * servicegroup_id
     */
    private String servicegroupId;

    /**
     * channel_name
     */
    private String channelName;

    /**
     * servicegroup_name
     */
    private String servicegroupName;

    /**
     * create_at
     */
    private Date createAt;

    private String token;

    private String slackUserId;


}

package com.ladeit.pojo.ao;

import lombok.Data;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname JoinServiceGroupAO
 * @Date 2020/1/8 14:00
 */
@Data
public class JoinServiceGroupAO {

    private String channelId;

    private String slackUserId;

    private String serviceGroupId;

    private String token;

}

package com.ladeit.pojo.ao;

import lombok.Data;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname RedisInfoAO
 * @Date 2020/4/23 16:53
 */
@Data
public class RedisInfoAO {
    //redis类型 0-内嵌，1-外联
    private String type;

    private String database;

    private String host;

    private String port;

    private String password;

}

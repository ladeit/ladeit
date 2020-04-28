package com.ladeit.pojo.ao;

import lombok.Data;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname MysqlInfo
 * @Date 2020/4/23 16:50
 */
@Data
public class MysqlInfoAO {

    //数据库类型 0-内嵌，1-外联
    private String type;

    private String url;

    private String driver;

    private String username;

    private String password;

    //是否同时转移数据
    private Boolean operflag;

}

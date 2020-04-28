package com.ladeit.pojo.ao;

import lombok.Data;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname StartParamInfoAO
 * @Date 2020/4/23 20:05
 */
@Data
public class StartParamInfoAO {

    private MysqlInfoAO mysqlInfoAO;

    private RedisInfoAO redisInfoAO;

    private LadeitInfoAO ladeitInfoAO;

}

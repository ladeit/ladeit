package com.ladeit.biz.services;

import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.LadeitInfoAO;
import com.ladeit.pojo.ao.MysqlInfoAO;
import com.ladeit.pojo.ao.RedisInfoAO;
import com.ladeit.pojo.ao.StartParamInfoAO;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname StartService
 * @Date 2020/4/23 20:15
 */
public interface StartService {

    ExecuteResult<StartParamInfoAO> getStartParam();

    ExecuteResult<String> updateMysql(MysqlInfoAO mysqlInfoAO);

    ExecuteResult<String> updateRedis(RedisInfoAO redisInfoAO);

    ExecuteResult<String> updateLadeit(LadeitInfoAO ladeitInfoAO);
}

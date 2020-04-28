package com.ladeit.biz.controller;

import com.ladeit.biz.services.StartService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname StartController
 * @Date 2020/4/23 16:57
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/start")
public class StartController {

    @Autowired
    private StartService startService;


    @GetMapping
    public ExecuteResult<StartParamInfoAO> getStartParam(){
        return startService.getStartParam();
    }


    /**
     * 更新mysql配置
     * @param mysqlInfoAO
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2020/4/23
     */
    @PutMapping("/mysql")
    public ExecuteResult<String> updateMysql(@RequestBody MysqlInfoAO mysqlInfoAO){
        return startService.updateMysql(mysqlInfoAO);
    }

    /**
     * 更新redis配置
     * @param redisInfoAO
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2020/4/23
     */
    @PutMapping("/redis")
    public ExecuteResult<String> updateRedis(@RequestBody RedisInfoAO redisInfoAO){
        return startService.updateRedis(redisInfoAO);
    }

    /**
     * 更新ladeit配置
     * @param ladeitInfoAO
     * @return com.ladeit.common.ExecuteResult<java.lang.String>
     * @date 2020/4/23
     */
    @PutMapping("/ladeit")
    public ExecuteResult<String> updateLadeit(@RequestBody LadeitInfoAO ladeitInfoAO){
        return startService.updateLadeit(ladeitInfoAO);
    }

}

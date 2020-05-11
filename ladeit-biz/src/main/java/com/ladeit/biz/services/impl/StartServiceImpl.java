package com.ladeit.biz.services.impl;

import com.ladeit.biz.App;
import com.ladeit.biz.services.StartService;
import com.ladeit.common.ExecuteResult;
import com.ladeit.pojo.ao.LadeitInfoAO;
import com.ladeit.pojo.ao.MysqlInfoAO;
import com.ladeit.pojo.ao.RedisInfoAO;
import com.ladeit.pojo.ao.StartParamInfoAO;
import com.ladeit.util.auth.TokenUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname StartServiceImpl
 * @Date 2020/4/23 20:17
 */
@Service
public class StartServiceImpl implements StartService {

    private static String redisroperties = System.getProperty("user.home")+"/.ladeit/config/.redis.conf";
    private static String mysqlproperties = System.getProperty("user.home")+"/.ladeit/config/.mysql.conf";
    private static String ladeitproperties = System.getProperty("user.home")+"/.ladeit/config/.ladeit.conf";
    private static String filepath = System.getProperty("user.home")+"/.ladeit/config";

    @Override
    public ExecuteResult<StartParamInfoAO> getStartParam() {
        ExecuteResult<StartParamInfoAO> result = new ExecuteResult<>();
        StartParamInfoAO startParamInfoAO = new StartParamInfoAO();
        try {
            File fileredis = new File(redisroperties);
            if(fileredis.exists()){
                InputStream redisinput = new FileInputStream(redisroperties);
                Properties reproperties = new Properties();
                reproperties.load(redisinput);
                RedisInfoAO redisInfoAO = new RedisInfoAO();
                redisInfoAO.setType(reproperties.getProperty("spring.redis.type"));
                redisInfoAO.setDatabase(reproperties.getProperty("spring.redis.database"));
                redisInfoAO.setHost(reproperties.getProperty("spring.redis.host"));
                redisInfoAO.setPassword(reproperties.getProperty("spring.redis.password"));
                redisInfoAO.setPort(reproperties.getProperty("spring.redis.port"));
                startParamInfoAO.setRedisInfoAO(redisInfoAO);
            }
            File filemysql = new File(mysqlproperties);
            if(filemysql.exists()){
                InputStream mysqlinput = new FileInputStream(mysqlproperties);
                Properties myproperties = new Properties();
                myproperties.load(mysqlinput);
                MysqlInfoAO mysqlInfoAO = new MysqlInfoAO();
                mysqlInfoAO.setType(myproperties.getProperty("datasource.mysql.type"));
                mysqlInfoAO.setDriver(myproperties.getProperty("datasource.default.driver"));
                mysqlInfoAO.setOperflag(true);
                mysqlInfoAO.setPassword(myproperties.getProperty("datasource.default.password"));
                mysqlInfoAO.setUrl(myproperties.getProperty("datasource.default.url"));
                mysqlInfoAO.setUsername(myproperties.getProperty("datasource.default.username"));
                startParamInfoAO.setMysqlInfoAO(mysqlInfoAO);
            }
            File file = new File(ladeitproperties);
            LadeitInfoAO ladeitInfoAO = new LadeitInfoAO();
            if(file.exists()){
                InputStream ladeitinput = new FileInputStream(ladeitproperties);
                Properties ladeitproperties = new Properties();
                ladeitproperties.load(ladeitinput);
                ladeitInfoAO.setBotmngrhost(ladeitproperties.getProperty("ladeit-bot-notif-mngr.host"));
                ladeitInfoAO.setLadeithost(ladeitproperties.getProperty("ladeit.host"));
                startParamInfoAO.setLadeitInfoAO(ladeitInfoAO);
            }
            result.setResult(startParamInfoAO);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ExecuteResult<String> updateMysql(MysqlInfoAO mysqlInfoAO) {
        ExecuteResult<String> result  = new ExecuteResult<>();
       String type = mysqlInfoAO.getType();
       if("0".equals(type)){
           Properties props = new Properties();
           props.setProperty("datasource.mysql.type","0");
           props.setProperty("port","3306");
           props.setProperty("datasource.default.username","root");
           props.setProperty("datasource.default.url","jdbc:mysql://localhost:3306/ladeit?useUnicode=true&characterEncoding=utf-8&useSSL=false");
           props.setProperty("datasource.default.driver","com.mysql.jdbc.Driver");
//           String ps = null;
//           try {
//               ps = TokenUtil.createToken("ps"+System.currentTimeMillis());
//           } catch (NoSuchAlgorithmException e) {
//               e.printStackTrace();
//           } catch (UnsupportedEncodingException e) {
//               e.printStackTrace();
//           }
           String ps = "mariaDB4j";
           props.setProperty("datasource.default.password",ps);
           try {
               putProperties(props,mysqlproperties);
           } catch (IOException e) {
               e.printStackTrace();
           }
       }else{
           Properties props = new Properties();
           props.setProperty("datasource.mysql.type","1");
           //props.setProperty("port","3306");
           props.setProperty("datasource.default.username",mysqlInfoAO.getUsername());
           props.setProperty("datasource.default.url",mysqlInfoAO.getUrl());
           props.setProperty("datasource.default.driver",mysqlInfoAO.getDriver());
           props.setProperty("datasource.default.password",mysqlInfoAO.getPassword());
           try {
               putProperties(props,mysqlproperties);
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
        restart();
        result.setResult("success");
        return result;
    }

    @Override
    public ExecuteResult<String> updateRedis(RedisInfoAO redisInfoAO) {
        ExecuteResult<String> result  = new ExecuteResult<>();
        String type = redisInfoAO.getType();
        if("0".equals(type)){
            Properties redisprops = new Properties();
            redisprops.setProperty("spring.redis.type","0");
            redisprops.setProperty("spring.redis.database","1");
            redisprops.setProperty("spring.redis.host","127.0.0.1");
            redisprops.setProperty("spring.redis.port","6379");
            redisprops.setProperty("spring.redis.password","123456");
            redisprops.setProperty("spring.redis.timeout","604800000");
            redisprops.setProperty("spring.redis.exprie","604800");
            redisprops.setProperty("spring.redis.jedis.pool.max-idle","10");
            redisprops.setProperty("spring.redis.jedis.pool.max-wait","-1");
            redisprops.setProperty("spring.redis.jedis.pool.max-active","20");
            redisprops.setProperty("spring.redis.jedis.pool.min-idle","5");
            try {
                putProperties(redisprops,redisroperties);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Properties redisprops = new Properties();
            redisprops.setProperty("spring.redis.type","1");
            redisprops.setProperty("spring.redis.database",redisInfoAO.getDatabase());
            redisprops.setProperty("spring.redis.host",redisInfoAO.getHost());
            redisprops.setProperty("spring.redis.port",redisInfoAO.getPort());
            redisprops.setProperty("spring.redis.password",redisInfoAO.getPassword());
            redisprops.setProperty("spring.redis.timeout","604800000");
            redisprops.setProperty("spring.redis.exprie","604800");
            redisprops.setProperty("spring.redis.jedis.pool.max-idle","10");
            redisprops.setProperty("spring.redis.jedis.pool.max-wait","-1");
            redisprops.setProperty("spring.redis.jedis.pool.max-active","20");
            redisprops.setProperty("spring.redis.jedis.pool.min-idle","5");
            try {
                putProperties(redisprops,redisroperties);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        restart();
        result.setResult("success");
        return result;
    }

    @Override
    public ExecuteResult<String> updateLadeit(LadeitInfoAO ladeitInfoAO) {
        ExecuteResult<String> result  = new ExecuteResult<>();
        Properties redisprops = new Properties();
        redisprops.setProperty("ladeit-bot-notif-mngr.host",ladeitInfoAO.getBotmngrhost());
        redisprops.setProperty("ladeit.host",ladeitInfoAO.getLadeithost());
        try {
            putProperties(redisprops,ladeitproperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        restart();
        result.setResult("success");
        return result;
    }

    public void putProperties(Properties props,String properties) throws IOException {
        FileOutputStream oFile = null;
        //File file = new File("./ladeit/config/.config.properties");
        File file = new File(properties);
        if(!file.exists()){
            File dir = new File(filepath);
            if(!dir.exists()){
                dir.mkdirs();
            }
            file.createNewFile();
        }
        oFile = new FileOutputStream(file,false);
        props.store(oFile, "config");
        oFile.close();
    }

    public void restart(){
        ExecutorService threadPool = new ThreadPoolExecutor(1, 1, 0,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.DiscardOldestPolicy());
        threadPool.execute(() -> {
            App.context.close();
            App.context = SpringApplication.run(App.class,
                    App.args);
        });
        threadPool.shutdown();
    }
}

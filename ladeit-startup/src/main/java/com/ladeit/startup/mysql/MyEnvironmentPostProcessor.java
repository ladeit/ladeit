package com.ladeit.startup.mysql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author mddandpyy
 * @version V1.0
 * @Classname MyEnvironmentPostProcessor
 * @Date 2020/4/11 15:03
 */
@Component
public class MyEnvironmentPostProcessor  implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try{
            Properties properties = new Properties();
            Map sysproperties = System.getenv();
            if(sysproperties.get("LADEIT_MYSQL_HOST")==null){
                properties.setProperty("datasource.default.username","root");
                properties.setProperty("datasource.default.url","jdbc:mysql://localhost:3306/ladeit?useUnicode=true&characterEncoding=utf-8&useSSL=false");
                properties.setProperty("datasource.default.driver","com.mysql.jdbc.Driver");
                properties.setProperty("datasource.default.password","");
            }else{
                properties.setProperty("datasource.default.username",(String)sysproperties.get("LADEIT_MYSQL_USER"));
                String port = sysproperties.get("LADEIT_MYSQL_PORT")!=null?(String)sysproperties.get("LADEIT_MYSQL_PORT"):"3306";
                properties.setProperty("datasource.default.url","jdbc:mysql://"+sysproperties.get("LADEIT_MYSQL_HOST").toString()+":"+port+"/ladeit?useUnicode=true&characterEncoding=utf-8&useSSL=false");
                properties.setProperty("datasource.default.driver","com.mysql.jdbc.Driver");
                properties.setProperty("datasource.default.password",(String)sysproperties.get("LADEIT_MYSQL_PASSWORD"));
            }
            if(sysproperties.get("LADEIT_REDIS_HOST")==null){
                properties.setProperty("spring.redis.database","1");
                properties.setProperty("spring.redis.host","127.0.0.1");
                properties.setProperty("spring.redis.port","6379");
                properties.setProperty("spring.redis.password","123456");
            }else{
                String database = sysproperties.get("LADEIT_REDIS_DATABASE")!=null?(String)sysproperties.get("LADEIT_REDIS_DATABASE"):"1";
                properties.setProperty("spring.redis.database",database);
                properties.setProperty("spring.redis.host",(String)sysproperties.get("LADEIT_REDIS_HOST"));
                String port = sysproperties.get("LADEIT_REDIS_PORT")!=null?(String)sysproperties.get("LADEIT_REDIS_PORT"):"6379";
                properties.setProperty("spring.redis.port",port);
                properties.setProperty("spring.redis.password",(String)sysproperties.get("LADEIT_REDIS_PASSWORD"));
            }

            PropertiesPropertySource propertySource = new PropertiesPropertySource("ve", properties);
            environment.getPropertySources().addLast(propertySource);
            System.out.println("====加载配置完毕====");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
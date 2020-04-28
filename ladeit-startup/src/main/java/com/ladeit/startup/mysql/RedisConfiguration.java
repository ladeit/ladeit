package com.ladeit.startup.mysql;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.ladeit.util.auth.TokenUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import redis.embedded.RedisServer;

import java.io.*;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Properties;
import java.util.Set;

/**
 * 嵌入式redis服务启动类
 * @param
 * @return
 * @date 2020/4/3
 * @author MddandPyy
 */
public class RedisConfiguration {

    //private static String properties = "./ladeit/config/.config.conf";
    private static String properties = System.getProperty("user.home")+"/.ladeit/config/.redis.conf";
    private static String filepath = System.getProperty("user.home")+"/.ladeit/redis";
    private static String conpath = System.getProperty("user.home")+"/.ladeit/config";

    /**
     * redis 服务器
     */
    public Boolean startService() {
        try {
//            System.out.println(filepath);
            // 读取配置
            Properties props = new Properties();
            File file = new File(properties);
            //是否需要初始化标识，生成.conf文件
            boolean initflag = true;
            //mysql类型 0-内嵌，1-外联
            String redisType = null;
            if(file.exists()){
                props.load(new FileInputStream(file));
                // 处理启动参数
                final Set<Object> keys = props.keySet();
                for (Object key : keys) {
                    String val = props.getProperty(key.toString());
                    if ("".equals(val)) {
                    } else {
                        if("spring.redis.type".equals(key.toString())){
                            initflag = false;
                            redisType = val;
                        }
                    }
                }
            }
            if(initflag){
                Properties redisprops = new Properties();
                //redis类型 0-内嵌，1-外联
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
                putProperties(redisprops);
                startRedis();
            }else{
                if(redisType!=null){
                    if("0".equals(redisType)){
                        startRedis();
                    }
                }
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void startRedis(){
        System.out.println("启动内嵌redis服务");
        File filem = new File(filepath);
        if(!filem.exists()){
            filem.mkdirs();
        }
        RedisServer redisServer = RedisServer.builder().port(6379)
                .setting("daemonize no")
                .setting("requirepass 123456")
                .setting("pidfile /var/run/redis.pid")
                .setting("tcp-backlog 511")
                .setting("timeout 0")
                .setting("tcp-keepalive 0")
                .setting("loglevel notice")
                .setting("databases 16")
                .setting("save 900 1")
                .setting("save 300 10")
                .setting("save 60 10000")
                .setting("stop-writes-on-bgsave-error yes")
                .setting("rdbcompression yes")
                .setting("rdbchecksum yes")
                .setting("dbfilename dump.rdb")
                .setting("dir "+filepath)
                .setting("slave-serve-stale-data yes")
                .setting("slave-read-only yes")
                .setting("repl-diskless-sync no")
                .setting("repl-diskless-sync-delay 5")
                .setting("repl-disable-tcp-nodelay no")
                .setting("slave-priority 100")
                .setting("appendonly no")
                .setting("appendfilename appendonly.aof")
                .setting("appendfsync everysec")
                .setting("no-appendfsync-on-rewrite no")
                .setting("auto-aof-rewrite-percentage 100")
                .setting("auto-aof-rewrite-min-size 64mb")
                .setting("aof-load-truncated yes")
                .setting("lua-time-limit 5000")
                .setting("slowlog-log-slower-than 10000")
                .setting("latency-monitor-threshold 0")
                .setting("list-max-ziplist-entries 512")
                .setting("list-max-ziplist-value 64")
                .setting("set-max-intset-entries 512")
                .setting("zset-max-ziplist-entries 128")
                .setting("zset-max-ziplist-value 64")
                .setting("hll-sparse-max-bytes 3000")
                .setting("activerehashing yes")
                .setting("hz 10")
                .setting("aof-rewrite-incremental-fsync yes").build();
        redisServer.start();
    }

    public void putProperties(Properties props) throws IOException {
        FileOutputStream oFile = null;
        File file = new File(properties);
        if(!file.exists()){
            File dir = new File(conpath);
            if(!dir.exists()){
                dir.mkdirs();
            }
            file.createNewFile();
        }
        oFile = new FileOutputStream(file,false);
        props.store(oFile, "ladeit-redis-config");
        oFile.close();
    }
}
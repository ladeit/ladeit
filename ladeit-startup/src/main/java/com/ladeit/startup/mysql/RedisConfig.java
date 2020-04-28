package com.ladeit.startup.mysql;

import redis.embedded.RedisServer;

import java.io.File;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname RedisConfig
 * @Date 2020/4/27 14:35
 */
public class RedisConfig {

    private static String filepath = System.getProperty("user.home")+"/.ladeit/redis";

    public boolean startRedis(){
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
        return true;
    }
}

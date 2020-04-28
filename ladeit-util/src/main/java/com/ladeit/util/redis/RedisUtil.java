package com.ladeit.util.redis;

import com.ladeit.util.git.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;
  /**
   * 平台token的hash表名称
   */
//	@Value("${redis.pttablename}")
  private String ptHashTableName;
  /**
   * gitlab token的hash表名称
   */
//	@Value("${redis.gltablename}")
  private String glHashTableName;

  /**
   * 将平台的token放入缓存
   *
   * @param key
   * @param value
   * @return
   */
  public boolean setPT(String key, Object value) {
    try {
      redisTemplate.opsForHash().
              put("PT65695b46", StringUtils.hashKeyForDisk(key), value);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 将gitlab的token放入缓存
   *
   * @param key
   * @param value
   * @return
   */
  public boolean setGL(String key, Object value) {
    try {
      redisTemplate.opsForHash().
              put("GLb273506d", StringUtils.hashKeyForDisk(key), value);
      redisTemplate.expire("GLb273506d", -1, TimeUnit.SECONDS);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  /**
   * 缓存websocket的Session对象
   * @param key
   * @param value
   * @return
   */
  public boolean setWS(String key,Object value) {
	  try {
		  redisTemplate.opsForHash()
		  	.put("WS671auyt1", StringUtils.hashKeyForDisk(key), value);
		  redisTemplate.expire("WS671auyt1", -1, TimeUnit.SECONDS);

	  }catch (Exception e) {
		// TODO: handle exception
	}
	  return false;
  }
  /**
   * 删除对应的session
   * @return
   */
  public boolean removeWS(String key) {
	  try {
	  redisTemplate.opsForHash()
	  .delete("WS671auyt1", StringUtils.hashKeyForDisk(key));
	  }catch (Exception e) {
		  return true;
	  }
	return false;
  }
  
  
  /**
   * 判断key是否存在
   *
   * @param key
   * @return
   */
  public boolean hashKey(String key) {
    try {
      return redisTemplate.hasKey(key);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 获取平台的token
   *
   * @param key
   * @return
   */
  public Object getPT(String key) {
    return key == null ? null : redisTemplate.opsForHash().get("PT65695b46", StringUtils.hashKeyForDisk(key));
  }

  /**
   * 获取gitlab的token
   *
   * @param key
   * @return
   */
  public Object getGL(String key) {
    return key == null ? null : redisTemplate.opsForHash().get("GLb273506d", StringUtils.hashKeyForDisk(key));
  }

  /**
  * 启动设置过期时间
  * @FunctionName configUser
  * @author falcomlife
  * @date 19-7-23
  * @version 1.0.0
  * @Return void
  * @param
  */
//  @Bean
//  public void configUser(){
//    redisTemplate.expire("user", 30, TimeUnit.SECONDS);
//  }
}
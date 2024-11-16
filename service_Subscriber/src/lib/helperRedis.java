package lib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import conf.redisConf;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.*;

public final class helperRedis {
	
    private static JedisPool jedisPool = null;
    private static final Log log = LogFactory.getLog(helperRedis.class);	
    /**
     * 初始化Redis连接池
     */
    static {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxActive(redisConf.DRIVER_Redis_MAX_ACTIVE);
            config.setMaxIdle(redisConf.DRIVER_Redis_MAX_IDLE);
            config.setMaxWait(redisConf.DRIVER_Redis_MAX_WAIT);            
            config.setTestOnBorrow(redisConf.DRIVER_Redis_TEST_ON_BORROW);       
            //20180619 //Could not get a resource from the pool
            config.setTestOnReturn(true);
            //Idle时进行连接扫描
            config.setTestWhileIdle(true);
            //表示idle object evitor两次扫描之间要sleep的毫秒数
            config.setTimeBetweenEvictionRunsMillis(30000);
            //表示idle object evitor每次扫描的最多的对象数
            config.setNumTestsPerEvictionRun(10);
            //表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
            config.setMinEvictableIdleTimeMillis(60000);
            //with password
            if (redisConf.password.isEmpty())
            	jedisPool = new JedisPool(config,redisConf.host,redisConf.port,redisConf.timeOut,null,redisConf.db);
            else
            	jedisPool = new JedisPool(config,redisConf.host,redisConf.port,redisConf.timeOut,redisConf.password,redisConf.db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取Jedis实例
     * @return
     */
    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
            	log.error("jedis get full");
                return null;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        	log.error(e.getMessage());
            return null;
        }
    }
    
    /**
     * 释放jedis资源
     * @param jedis
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnResource(jedis);
        }
    }
}

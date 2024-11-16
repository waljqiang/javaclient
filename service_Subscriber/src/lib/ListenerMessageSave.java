package lib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

public class ListenerMessageSave{
	 @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(ListenerMessageSave.class);
	 
	 public void handleSave(String data,RedisCache redisCache,Jedis jedis){
		 redisCache.addQueueData(data,jedis);
	 }
}
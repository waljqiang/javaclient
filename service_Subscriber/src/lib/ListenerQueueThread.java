package lib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import conf.publicConf;
import conf.redisConf;
import data.Device;
import redis.clients.jedis.Jedis;

public class ListenerQueueThread extends Thread{
	private static final Log log = LogFactory.getLog(ListenerQueueThread.class);
	private Jedis jedis=null;
	
	public ListenerQueueThread(){
		this.jedis = helperRedis.getJedis();
	}
	
	@Override
	public void run(){
		log.fatal("Listener queue server started");
		while(true){
			try{
				long len = jedis.llen(redisConf.DEVICE_QUEUE_DATA);
				if(len > publicConf.queue_max){
					log.fatal("clear the queue,caused queue len["+len+"]");
					jedis.del(redisConf.DEVICE_QUEUE_DATA);
				}else{
					Thread.sleep(600000);
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
}

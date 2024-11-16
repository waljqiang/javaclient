package lib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import conf.redisConf;
import redis.clients.jedis.Jedis;

//队列数据处理
public class HandleAuthDataThread extends Thread{
	private static final Log log = LogFactory.getLog(HandleAuthDataThread.class);
	private Jedis jedis = null;
	private HandleAuthData handleAuthData;
	private String str;
	public HandleAuthDataThread(){
		this.jedis=helperRedis.getJedis();
		this.handleAuthData = new HandleAuthData();
	}
	
	@Override
	public void run() {
		while(true){
			try{
				str = jedis.rpop(redisConf.DEVICE_AUTH_QUEUE_DATA);
				if(str == null || str == ""){
					Thread.sleep(1000);
				}else{
					handleAuthData.handle(str);
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}  	        
	}
}
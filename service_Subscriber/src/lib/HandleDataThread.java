package lib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import conf.redisConf;
import redis.clients.jedis.Jedis;

//队列数据处理
public class HandleDataThread extends Thread{
	private static final Log log = LogFactory.getLog(HandleDataThread.class);
	private Jedis jedis = null;
	private HandleData handleData;
	private String str;
	public HandleDataThread(){
		this.jedis=helperRedis.getJedis();
		this.handleData = new HandleData();
	}
	
	@Override
	public void run() {
		while(true){
			try{
				str = jedis.rpop(redisConf.DEVICE_QUEUE_DATA);
				if(str == null || str == ""){
					Thread.sleep(1000);
				}else{
					handleData.handle(str);
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}  	        
	}
}
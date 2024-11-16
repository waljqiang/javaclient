package lib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import conf.publicConf;
import conf.redisConf;
import redis.clients.jedis.Jedis;

//ip定位
public class SendEmailThread extends Thread{
	private static final Log log = LogFactory.getLog(SendEmailThread.class);
	private Jedis jedis=null;
	
	public SendEmailThread(){
		this.jedis = helperRedis.getJedis();
	}
	
	@Override
	public void run() {
		log.fatal("Send email server started");
		while(true){
			try{
				String data = jedis.rpop(redisConf.EMAIL_QUEUE);
				if(data != null && data !=""){
					log.debug("Send email["+data+"]");
					HttpRequest.sendPostJson(publicConf.cloudnetlot_api_base+"/backend/send/email",data);
				}else{
					Thread.sleep(3000);
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
}
package lib;

import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;

import conf.publicConf;
import conf.redisConf;
import redis.clients.jedis.Jedis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



@SuppressWarnings("deprecation")
public class ListenerMessageThread extends Thread {
    private static final Log log = LogFactory.getLog(ListenerMessageThread.class);
	private CallbackConnection conn;
	private Jedis jedis;
	private RedisCache redisCache;
	private ListenerMessageSave listenerMessageSave;
	 
	public ListenerMessageThread(CallbackConnection con) {
		this.conn=con;
		this.jedis = helperRedis.getJedis();
		this.redisCache = new RedisCache();
		this.listenerMessageSave = new ListenerMessageSave();
	}
	 
	public void run() {
		this.conn.listener(new Listener() {
			public void onConnected() {
				log.fatal("mqtt listener message connected");
			}
			public void onDisconnected() {
				log.fatal("mqtt listener message disconnected");
			}

			public void onPublish(UTF8Buffer topic, org.fusesource.hawtbuf.Buffer payload, Runnable onComplete){
				String s_topic = topic.toString();
				String data = new String(payload.toByteArray());
				log.debug("mqtt receive data[" + data + "]from topic[" + topic + "]");
				String arr[] = null;
				arr = s_topic.split("/");
				
				if(arr.length == 3){
					if(arr[2].equals("dev2app")){//上报通道
						String[] cltidInfo = Helper.decodeCltid(arr[1]);
						if(cltidInfo[0] == null){
							log.error("{\"errorcode\":\"600500130\",\"message\":\"The cltid is error\"}");
						}else{
							//保存数据
							data = "{\"prtid\":\""+arr[0]+"\",\"cltid\":\""+arr[1]+"\",\"mac\":\""+cltidInfo[2]+"\",\"data\":"+data+"}";
							if(tooManyAttemps(cltidInfo[2],publicConf.upinfo_pmax,publicConf.upinfo_lock)){
								log.error("{\"errorCode\":\""+MyException.DEV_LOCKED+"\",\"message\":\"The device["+cltidInfo[2]+"] is locked\"}");
								Helper.sendMqtt(Helper.getTopic(arr[0],arr[1]),"The device is locked");
							}else{
								listenerMessageSave.handleSave(data,redisCache,jedis);
								log.debug("save data[" + data + "]");
							}
						 }
					}else if(arr[2].equals("auth")){//认证询问通道
						String[] cltidInfo = Helper.decodeCltid(arr[1]);
						if(cltidInfo[0] == null){
							log.error("{\"errorcode\":\"600500130\",\"message\":\"The cltid is error\"}");
						}else{
							//保存数据
							data = "{\"prtid\":\""+arr[0]+"\",\"cltid\":\""+arr[1]+"\",\"mac\":\""+cltidInfo[2]+"\",\"data\":"+data+"}";
							redisCache.addAuthQueueData(data,jedis);
							log.debug("save data[" + data + "]");
						 }
					}else{
						log.error("The topic format is error");
					}
				}else{
					log.error("The topic format is error");
				}
			}

			public void onFailure(Throwable value) {
				//conn.disconnect(null);
				log.error("receive message failure");
			}
		});
	}
	
	private boolean tooManyAttemps(String mac,int pmax,int locktime){
		Boolean rs = false;
		String key = redisConf.DEV_PER_UPS + mac;
		String attemps = redisCache.get(key, jedis);	
		if(attemps == null){
			redisCache.setex(key, 60, "1",jedis);
		}else{
			int _attemps = Integer.parseInt(attemps) + 1;
			if(_attemps >= pmax){
				rs = true;
				if(_attemps == pmax){
					redisCache.expire(key, locktime,jedis);
				}
			}
			redisCache.incr(key, jedis);
		}
		return rs;
	}
}

package lib;

import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.CallbackConnection;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.mqtt.client.Listener;

import conf.deviceConf;
import conf.publicConf;
import redis.clients.jedis.Jedis;

//监听设备连接
@SuppressWarnings("deprecation")
public class ListenerOnlineThread extends Thread {
    private static final Log log = LogFactory.getLog(ListenerOnlineThread.class);
	private CallbackConnection conn;
	private String mac;
	private Jedis jedis;
	private RedisCache redisCache;
 	private FrequenceOnOff frequenceOnOff;
	 
	public ListenerOnlineThread(CallbackConnection con){ 
		this.conn=con;
		this.jedis = helperRedis.getJedis();
		this.redisCache = new RedisCache();
	 	this.frequenceOnOff = new FrequenceOnOff(this.redisCache);
	} 
	 
	public void run() {
		this.conn.listener(new Listener() {
            public void onConnected() {
            	log.fatal("mqtt online connected");
            }

            public void onDisconnected() {
            	log.fatal("mqtt online disconnected");
            }

            public void onPublish(UTF8Buffer topic, org.fusesource.hawtbuf.Buffer payload, Runnable onComplete){
        		Date ldate = new Date();
         		Long time = ldate.getTime()/1000;
        		try{
        			String cltid = topic.toString().split("/")[4];
        			mac = Helper.decodeCltid(cltid)[2];
        			if(mac != null){
        				Map<String,String> status = new HashMap<String,String>();
        				Map<String,Object> oldCache = redisCache.getDeviceDynamic(mac,jedis);
        				if(!oldCache.isEmpty() && oldCache.get("status").toString().equals(deviceConf.STATUS_OFFLINE)){//离线->上线
        					Boolean isAlarm = frequenceOnOff.handle(mac,jedis);
        					if(isAlarm){
        						status.put("alarm_app","1");
        					}else{
        						if(oldCache.get("offline_time") != null){
        							Long offlineTime = Long.parseLong(oldCache.get("offline_time").toString());
        							if(time - offlineTime > publicConf.alarm_recovery_time){
        								status.put("alarm_app","0");
        							}
        						}
        					}
        				}
        				status.put("status", deviceConf.STATUS_ONLINE);
        				redisCache.parseDynamic(mac,status, time,jedis);
        			}
        		}catch(Exception e){
        			
        		}
            }

            public void onFailure(Throwable value) {
               // conn.disconnect(null);
                log.error("online receive failure");
            }

        });
	}
}

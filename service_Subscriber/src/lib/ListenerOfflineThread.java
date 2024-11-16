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
public class ListenerOfflineThread extends Thread {
    private static final Log log = LogFactory.getLog(ListenerOfflineThread.class);
	private CallbackConnection conn;
	private RedisCache redisCache;
	private Jedis jedis;
	private FrequenceOnOff frequenceOnOff;
	private String mac;
	 
	public ListenerOfflineThread(CallbackConnection con){ 
		this.conn=con;
		this.jedis = helperRedis.getJedis();
		this.redisCache = new RedisCache();
	 	this.frequenceOnOff = new FrequenceOnOff(this.redisCache);
	} 
	 
	public void run() {
		this.conn.listener(new Listener() {
            public void onConnected() {
            	log.fatal("mqtt offline connected");
            }

            public void onDisconnected() {
            	log.fatal("mqtt offline disconnected");
            }

            public void onPublish(UTF8Buffer topic, org.fusesource.hawtbuf.Buffer payload, Runnable onComplete){
            	Date ldate = new Date();
         		Long time = ldate.getTime()/1000;
        		try{
        			String cltid = topic.toString().split("/")[4];
        			mac = Helper.decodeCltid(cltid)[2];
        			if(mac != null){
        				log.debug("the device[" + mac + "] offline");
        				Map<String,String> status = new HashMap<String,String>();
        				Map<String,Object> oldCache = redisCache.getDeviceDynamic(mac,jedis);
        				if(!oldCache.isEmpty() && oldCache.get("status").toString().equals(deviceConf.STATUS_ONLINE)){//在线->离线
        					Boolean isAlarm = frequenceOnOff.handle(mac,jedis);
        					if(isAlarm){
        						status.put("alarm_app","1");
        					}else{
        						if(oldCache.get("online_time") != null){
        							Long onlineTime = Long.parseLong(oldCache.get("online_time").toString());
        							if(time - onlineTime > publicConf.alarm_recovery_time){
        								status.put("alarm_app","0");
        							}
        						}
        					}
        				}
        				status.put("status", deviceConf.STATUS_OFFLINE);
        				redisCache.parseDynamic(mac,status, time,jedis);
        			}
        		}catch(Exception e){
        			
        		}
            }

            public void onFailure(Throwable value) {
                //conn.disconnect(null);
                log.error("offline receive failure");
            }

        });
	}
}

package lib;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import conf.publicConf;
import conf.redisConf;
import redis.clients.jedis.Jedis;
import data.Device;


public class FrequenceOnOff{
	private static final Log log = LogFactory.getLog(FrequenceOnOff.class);
	private String mac;
	private RedisCache redisCache;
	private Device deviceDB;
	
	public FrequenceOnOff(RedisCache redisCache){
		this.redisCache = redisCache;
		this.deviceDB = new Device();
	}
	
	public Boolean handle(String mac,Jedis jedis){
		boolean rs = false;
		
		JdbcUtils jdbcUtils = new JdbcUtils();
		jdbcUtils.getConnection();
		
		this.mac = mac;
		Date ldate = new Date();
 		Long time = ldate.getTime()/1000;
	 	List<Map<String,Object>> alarmSets = this.redisCache.getAlarmSet(this.mac,jedis);
	 	if(alarmSets.isEmpty()){
	 		alarmSets = this.deviceDB.getAlarmSet(this.mac,jdbcUtils);
			this.redisCache.setDevAlarm(this.mac,alarmSets,jedis);
	 	}

	 	Map<String,Object> alarmSet = new HashMap<String,Object>();
	 	for(int i=0;i<alarmSets.size();i++){
	 		Map<String,Object> value = alarmSets.get(i);
	 		if(value.get("type").toString().equals("1") && value.get("enable").toString().equals("1")){
	 			alarmSet = value;
	 			break;
	 		}
	 	}
	 	if(!alarmSet.isEmpty()){
	 		int ttl = Integer.parseInt(alarmSet.get("max").toString()) * 60;
	 		int interval = Integer.parseInt(alarmSet.get("interval").toString());
	 		if(this.isAlarm(3,ttl,interval,jedis)){//是否报警
	 			rs = true;
	 			this.handleAlarm(alarmSet,time,jdbcUtils,jedis);
	 		}
	 	}
	 	jdbcUtils.releaseConn();
	 	return rs;
	}
	
	private void handleAlarm(Map<String,Object> alarmSet,Long time,JdbcUtils jdbcUtils,Jedis jedis){
		log.debug("Handle the device["+this.mac+"] frequency on-off");
		Map<String,Object> noticeSets = new HashMap<String,Object>();
		noticeSets = this.redisCache.getAlarmNoticeSet(this.mac,jedis);
		if(noticeSets.isEmpty()){
			noticeSets = this.deviceDB.getAlarmNoticeSet(this.mac,jdbcUtils);
			this.redisCache.setDevAlarmNotice(this.mac,noticeSets,jedis);
		}
		if(noticeSets.get("lang") != null && noticeSets.get("user") != null){
			String lang = noticeSets.get("lang").toString();
			try{
				Map<String,Object> device = this.redisCache.getDeviceReport(this.mac,jedis);
				if(device == null){
					device = this.deviceDB.getDevice(this.mac,jdbcUtils);
					if(!device.isEmpty()){
						this.redisCache.setDeviceReport(this.mac,publicConf.cache_registerttl,device,jedis);
					}
				}
				
				if(this.deviceDB.addDeviceAlarm(this.mac,"{\"type\":\"1\",\"value\":\""+alarmSet.get("max").toString()+"\"}", time, jdbcUtils)){
					List<Map<String,String>> notices = new ArrayList<Map<String,String>>();
					Map<String,String> notice = new HashMap<String,String>();
					notice.put("type","1");
					notice.put("value",this.mac);
					notices.add(notice);
					if(alarmSet.get("iswechat").toString().equals("1")){//发微信消息
						SendAlarmByWechatThread wechatThr = new SendAlarmByWechatThread(device,noticeSets.get("user"),notices,lang,time);
						wechatThr.start();
					}
					if(alarmSet.get("isemail").toString().equals("1")){//发送邮件通知
						SendAlarmByEmailThread emailThr = new SendAlarmByEmailThread(device,noticeSets.get("user"),notices,lang,time);
						emailThr.start();
					}
				}else{
					log.debug("Handle the device["+this.mac+"] frequency on-off failure");
				}
			}catch(Exception e){
				log.error("Handle the device["+this.mac+"] frequency on-off failure,caused:"+e.getMessage()); 
			}
		}
	}
	
	private Boolean isAlarm(int pmax,int ttl,int interval,Jedis jedis){
//		this.alarmFlag = false;
		Boolean rs = false;
		//interval内仅发送一次告警
		String attemps = this.redisCache.get(redisConf.DEVICEONOFFCOUNT+this.mac,jedis);
		if(attemps != null){
			int attempsi = Integer.parseInt(attemps)+1;
			if(attempsi >= pmax){//发送告警,interval秒内不再发送告警内容
				if(attempsi == pmax){
					rs = true;
					this.redisCache.expire(redisConf.DEVICEONOFFCOUNT+this.mac,interval,jedis);
				}
			}
			this.redisCache.incr(redisConf.DEVICEONOFFCOUNT+this.mac,jedis);
		}else{
			this.redisCache.setex(redisConf.DEVICEONOFFCOUNT+this.mac,ttl,"1",jedis);
		}
		//是否告警
//		attemps = this.redisCache.get(redisConf.DEVICEONOFFALARM+this.mac,jedis);
//		if(attemps != null){
//			int _attempsi = Integer.parseInt(attemps)+1;
//			if(_attempsi >= pmax){
//				this.alarmFlag = true;
//				this.redisCache.expire(redisConf.DEVICEONOFFALARM+this.mac,ttl,jedis);
//			}
//			this.redisCache.incr(redisConf.DEVICEONOFFALARM+this.mac,jedis);
//		}else{
//			this.redisCache.setex(redisConf.DEVICEONOFFALARM+this.mac,ttl,"1",jedis);
//		}
		return rs;
	}
	
}

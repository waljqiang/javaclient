package lib;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import conf.deviceConf;
import conf.redisConf;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class RedisCache{
	private static final Log log = LogFactory.getLog(RedisCache.class);
	
	public RedisCache(){
		
	}
	
	public void releaseRedis(Jedis jedis){
		helperRedis.returnResource(jedis);
	}
	
	public void addQueueData(String data){
		Jedis jedis = helperRedis.getJedis();
		this.addQueueData(data,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void addQueueData(String data,Jedis jedis){
		jedis.lpush(redisConf.DEVICE_QUEUE_DATA,data);
	}
	
	public void addAuthQueueData(String data){
		Jedis jedis = helperRedis.getJedis();
		this.addAuthQueueData(data,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void addAuthQueueData(String data,Jedis jedis){
		jedis.lpush(redisConf.DEVICE_AUTH_QUEUE_DATA,data);
	}
	
	public void addGroupQueue(String data){
		Jedis jedis = helperRedis.getJedis();
		this.addGroupQueue(data,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void addGroupQueue(String data,Jedis jedis){
		jedis.lpush(redisConf.GROUP_DEVICES_QUEUE, data);
	}
	
	public void addClientNums(String ...data){
		Jedis jedis = helperRedis.getJedis();
		this.addClientNums(jedis,data);
		helperRedis.returnResource(jedis);
	}
	
	public void addClientNums(Jedis jedis,String ...data){
		jedis.lpush(redisConf.DEVICE_QUEUE_CLIENTS, data);
	}
	
	public List<Map<String,String>> getClients(int num){
		Jedis jedis = helperRedis.getJedis();
		List<Map<String,String>> result = this.getClients(num, jedis);
		helperRedis.returnResource(jedis);
		return result;
	}
	
	public List<Map<String,String>> getClients(int num,Jedis jedis){
		List<Map<String,String>> result = new ArrayList<Map<String,String>>();
		
		Pipeline p = jedis.pipelined();
		for(int i=0;i<num;i++){
			p.rpop(redisConf.DEVICE_QUEUE_CLIENTS);
		}
		List<Object> datas = p.syncAndReturnAll();
		for(int j=0;j<datas.size();j++){
			Object data = datas.get(j);
			Map<String,String> client = new HashMap<String,String>();
			if(data != null){
				Map<String,String> tmp = JSON.parseObject(data.toString(),new TypeReference<Map<String,String>>(){});
				for(Map.Entry<String,String> entry : tmp.entrySet()){
					client.put(entry.getKey(), entry.getValue());
				}
				result.add(client);
			}
		}
		return result;
	}
	
	public Map<String,Object> getRegisterInfo(String mac){
		Jedis jedis = helperRedis.getJedis();
		Map<String,Object> info = this.getRegisterInfo(mac);
		helperRedis.returnResource(jedis);
		return info;
	}
	
	public Map<String,Object> getRegisterInfo(String mac,Jedis jedis){
		String info = jedis.get(redisConf.REGISTER + mac);
		return info == null ? null : JSON.parseObject(info,new TypeReference<HashMap<String,Object>>(){});
	}
	
	public void setRegisterInfo(String mac,int ttl,Map<String,Object> info){
		Jedis jedis = helperRedis.getJedis();
		this.setRegisterInfo(mac, ttl, info,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void setRegisterInfo(String mac,int ttl,Map<String,Object> info,Jedis jedis){
		if(info != null && !info.isEmpty()){
			jedis.setex(redisConf.REGISTER + mac, ttl,JSON.toJSONString(info));
		}else{
			jedis.del(redisConf.REGISTER + mac);
		}
	}
	
	public void setDeviceReport(String mac,int ttl,Map<String,Object> info){
		Jedis jedis = helperRedis.getJedis();
		this.setDeviceReport(mac, ttl, info);
		helperRedis.returnResource(jedis);
	}
	
	public void setDeviceReport(String mac,int ttl,Map<String,Object> info,Jedis jedis){
		if(info != null && !info.isEmpty()){
			jedis.setex(redisConf.DEVICE_REPORT + mac, ttl,JSON.toJSONString(info));
		}else{
			jedis.del(redisConf.DEVICE_REPORT + mac);
		}
	}
	
	public Map<String,Object> getDeviceReport(String mac){
		Jedis jedis = helperRedis.getJedis();
		Map<String,Object> info = this.getDeviceReport(mac);
		helperRedis.returnResource(jedis);
		return info;
	}
	
	public Map<String,Object> getDeviceReport(String mac,Jedis jedis){
		Map<String,Object> result = null;
		String info = jedis.get(redisConf.DEVICE_REPORT+mac);
		if(info != null){
			result = JSON.parseObject(info,new TypeReference<HashMap<String,Object>>(){});
		}
		return result;
	}
	
	public String getDynamicWithField(String mac,String field){
		Jedis jedis = helperRedis.getJedis();
		String result = this.getDynamicWithField(mac, field,jedis);
		helperRedis.returnResource(jedis);
		return result;
	}
	
	public String getDynamicWithField(String mac,String field,Jedis jedis){
		String result = jedis.get(redisConf.DEVICE_DYNAMIC+mac);
		if(result != null){
			JSONObject resultObj = JSON.parseObject(result);
			return resultObj.get(field).toString();
		}else{
			return null;
		}
		/*String result = jedis.hget(redisConf.DEVICE_DYNAMIC+mac, field);
		return result;*/
	}
	
	public Map<String,Object> getDeviceDynamic(String mac){
		Jedis jedis = helperRedis.getJedis();
		Map<String,Object> result = this.getDeviceDynamic(mac,jedis);
		helperRedis.returnResource(jedis);
		return result;
	}
	
	public Map<String,Object> getDeviceDynamic(String mac,Jedis jedis){
		String str = jedis.get(redisConf.DEVICE_DYNAMIC+mac);
		Map<String,Object> result = new HashMap<String,Object>();
		if(str != null){
			result = JSON.parseObject(str,new TypeReference<HashMap<String,Object>>(){});
			if(result.isEmpty()){
				Map<String,Object> data = new HashMap<String,Object>();
				data.put("cpu_use","0");
				data.put("memory_use","0");
				data.put("runtime","0");
				data.put("status","0");
				data.put("link","-1");
				data.put("rssi","-1");
				this.setDeviceDynamic(mac,data);
				String status = data.get("status").toString();
				if(status == null || status.equals(deviceConf.STATUS_OFFLINE)){
					result.put("cpu_use","0");
					result.put("memory_use","0");
					result.put("status","0");
					result.put("link","-1");
					result.put("rssi","-1");
				}
			}
		}
		return result;
	}
	
	public void setDeviceDynamic(String mac,Map<String,Object> data){
		Jedis jedis = helperRedis.getJedis();
		this.setDeviceDynamic(mac, data,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void setDeviceDynamic(String mac,Map<String,Object> data,Jedis jedis){
		Map<String,Object> oldDynamic = this.getDeviceDynamic(mac,jedis);
		if(oldDynamic == null || oldDynamic.isEmpty()){
			jedis.set(redisConf.DEVICE_DYNAMIC+mac,JSON.toJSONString(data));
		}else{
			for(Map.Entry<String,Object> entry : data.entrySet()){
				oldDynamic.put(entry.getKey(),entry.getValue());
			}
			jedis.set(redisConf.DEVICE_DYNAMIC+mac,JSON.toJSONString(oldDynamic));
		}
		//jedis.hmset(redisConf.DEVICE_DYNAMIC+mac,data);
	}
	
	public void setDeviceParams(String mac,String type,String value){
		Jedis jedis = helperRedis.getJedis();
		this.setDeviceParams(mac, type, value,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void setDeviceParams(String mac,String type,String value,Jedis jedis){
		jedis.hset(redisConf.DEVICE_PARAMS + mac,type,value);
	}
	
	public void addLocation(String data){
		Jedis jedis = helperRedis.getJedis();
		this.addLocation(data, jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void addLocation(String data,Jedis jedis){
		jedis.lpush(redisConf.DEVICE_QUEUE_LOCATION,data);
	}
	
	public Map<String,String> getLocation(){
		Jedis jedis = helperRedis.getJedis();
		Map<String,String> result = this.getLocation(jedis);
		helperRedis.returnResource(jedis);
		return result;
	}
	
	public Map<String,String> getLocation(Jedis jedis){
		Map<String,String> result = new HashMap<String,String>();
		String data = jedis.rpop(redisConf.DEVICE_QUEUE_LOCATION);
		if(data != null && data !=""){
			JSONObject dataJson = JSON.parseObject(data);
			for(Map.Entry<String,Object> entry : dataJson.entrySet()){
				result.put(entry.getKey(), entry.getValue().toString());
			}
		}
		return result;
	}
	
	public void deleteTopgraphy(String uid,String gid){
		Jedis jedis = helperRedis.getJedis();
		this.deleteTopgraphy(uid,gid);
		helperRedis.returnResource(jedis);
	}
	
	public void deleteTopgraphy(String uid,String gid,Jedis jedis){
		jedis.del(redisConf.TOPGRAPHY + uid + ":" + gid);
	}
	
	public void setDevAlarm(String mac,List<Map<String,Object>> alarmSets){
		Jedis jedis = helperRedis.getJedis();
		this.setDevAlarm(mac, alarmSets,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void setDevAlarm(String mac,List<Map<String,Object>> alarmSets,Jedis jedis){
		if(alarmSets != null && !alarmSets.isEmpty()){
			jedis.set(redisConf.DEVALARMSET+mac,JSON.toJSONString(alarmSets));
		}else{
			jedis.del(redisConf.DEVALARMSET+mac);
		}
	}
	
	public List<Map<String,Object>> getAlarmSet(String mac){
		Jedis jedis = helperRedis.getJedis();
		List<Map<String,Object>> result = this.getAlarmSet(mac, jedis);
		helperRedis.returnResource(jedis);
		return result;
	}
	
	public List<Map<String,Object>> getAlarmSet(String mac,Jedis jedis){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		String resultStr = jedis.get(redisConf.DEVALARMSET+mac);
		if(resultStr != null){
			JSONArray resultJson = JSON.parseArray(resultStr);
			for(int i=0;i<resultJson.size();i++){
				Map<String,Object> alarmInfo =  JSONObject.parseObject(resultJson.getJSONObject(i).toJSONString(), new TypeReference<Map<String, Object>>(){});
				result.add(alarmInfo);
			}
		}
		return result;
	}
	
	public void setDevAlarmNotice(String mac,Map<String,Object>noticeSets){
		Jedis jedis = helperRedis.getJedis();
		this.setDevAlarmNotice(mac, noticeSets, jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void setDevAlarmNotice(String mac,Map<String,Object>noticeSets,Jedis jedis){
		if(noticeSets != null && !noticeSets.isEmpty()){
			jedis.set(redisConf.DEVALARMNOTICESET+mac,JSON.toJSONString(noticeSets));
		}else{
			jedis.del(redisConf.DEVALARMNOTICESET+mac);
		}
	}
	
	public Map<String,Object> getAlarmNoticeSet(String mac){
		Jedis jedis = helperRedis.getJedis();
		Map<String,Object> result = this.getAlarmNoticeSet(mac, jedis);
		helperRedis.returnResource(jedis);
		return result;
	}
	
	public Map<String,Object> getAlarmNoticeSet(String mac,Jedis jedis){
		Map<String,Object> result = new HashMap<String,Object>();
		String resultStr = jedis.get(redisConf.DEVALARMNOTICESET+mac);
		if(resultStr != null){
			JSONObject resultJson = JSON.parseObject(resultStr);
			result.put("lang", resultJson.get("lang"));
			result.put("user", resultJson.getJSONArray("user"));
		}
		return result;
	}
	
	public void setDeviceAlarmTime(String mac,String type,String value){
		Jedis jedis = helperRedis.getJedis();
		this.setDeviceAlarmTime(mac, type, value,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void setDeviceAlarmTime(String mac,String type,String value,Jedis jedis){
		jedis.hset(redisConf.DEVICEALARMTIME + mac,type,value);
	}
	
	public String getDeviceAlarmTime(String mac,String type){
		Jedis jedis = helperRedis.getJedis();
		String result = this.getDeviceAlarmTime(mac, type,jedis);
		helperRedis.returnResource(jedis);
		return result;
	}
	
	public String getDeviceAlarmTime(String mac,String type,Jedis jedis){
		return jedis.hget(redisConf.DEVICEALARMTIME+mac,type);
	}
	
	public String getDeviceParams(String mac,String type){
		Jedis jedis = helperRedis.getJedis();
		String result = this.getDeviceParams(mac, type,jedis);
		helperRedis.returnResource(jedis);
		return result;
	}
	
	public String getDeviceParams(String mac,String type,Jedis jedis){
		return jedis.hget(redisConf.DEVICE_PARAMS+mac,type);
	}
	
	public void delDeviceParamsField(String mac,String type){
		Jedis jedis = helperRedis.getJedis();
		this.delDeviceParamsField(mac, type, jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void delDeviceParamsField(String mac,String type,Jedis jedis){
		jedis.hdel(redisConf.DEVICE_PARAMS+mac,type);
	}
	
	public void parseDynamic(String mac,Map<String,String> value,Long time){
		Jedis jedis = helperRedis.getJedis();
		this.parseDynamic(mac, value, time, jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void parseDynamic(String mac,Map<String,String> value,Jedis jedis){
		//更新cpu使用率、内存使用率、运行时间、设备状态
		Map<String,Object> data = new HashMap<String,Object>();
		Map<String,Object> oldCache = new HashMap<String,Object>();
		String str = jedis.get(redisConf.DEVICE_DYNAMIC+mac);
		if(str != null){
			oldCache = JSON.parseObject(str,new TypeReference<HashMap<String,Object>>(){});
			data = oldCache;
		}
		
		String status = value.get("status");
		String online_time = value.get("online_time");
		String alarmApp = value.get("alarm_app");
		String alarmDev = value.get("alarm_dev");
		String alarm = value.get("alarm");

		if(status != null){
			data.put("status",status);
		}
		if(online_time != null){
			data.put("online_time", online_time);
		}
		if(alarmApp != null){
			data.put("alarm_app",alarmApp);
		}
		if(alarmDev != null){
			data.put("alarm_dev",alarmDev);
		}
		if(alarm != null){
			data.put("alarm", alarm);
		}else{
			if(alarmApp != null || alarmDev != null){
				String oldAlarmapp = !oldCache.isEmpty() && oldCache.get("alarm_app") != null ? oldCache.get("alarm_app").toString() : "0";
				String oldAlarmdev = !oldCache.isEmpty() && oldCache.get("alarm_dev") != null ? oldCache.get("alarm_dev").toString() : "0";
				alarmApp = alarmApp != null ? alarmApp : oldAlarmapp;
				alarmDev = alarmDev != null ? alarmDev : oldAlarmdev;
				if(alarmApp.equals("1") || alarmDev.equals("1")){
					data.put("alarm","1");
				}else{
					data.put("alarm","0");
				}
			}
		}
		
		if(!data.isEmpty())
			jedis.set(redisConf.DEVICE_DYNAMIC+mac,JSON.toJSONString(data));
	}
	
	public void parseDynamic(String mac,Map<String,String> value,Long time,Jedis jedis){
		//更新cpu使用率、内存使用率、运行时间、设备状态
		Map<String,Object> data = new HashMap<String,Object>();
		Map<String,Object> oldCache = new HashMap<String,Object>();
		String str = jedis.get(redisConf.DEVICE_DYNAMIC+mac);
		if(str != null){
			oldCache = JSON.parseObject(str,new TypeReference<HashMap<String,Object>>(){});
			data = oldCache;
		}
		
		String oldStatus = !oldCache.isEmpty() && oldCache.get("status") != null ? oldCache.get("status").toString() : "0";
		String newStatus = value.get("status") == null ? oldStatus : value.get("status");
		String cpu_use = value.get("cpu_use");
		String memory_use = value.get("memory_use");
		String runtime = value.get("runtime");
		String parent = value.get("parent");
		String link = value.get("link");
		String rssi = value.get("rssi");
		String ability = value.get("ability");
		String alarmApp = value.get("alarm_app");
		String alarmDev = value.get("alarm_dev");
		String alarm = value.get("alarm");
		if(ability != null && !ability.isEmpty()){
			data.put("ability",JSON.parseArray(ability));
		}
		if(alarmApp != null){
			data.put("alarm_app",alarmApp);
		}
		if(alarmDev != null){
			data.put("alarm_dev",alarmDev);
		}
		if(alarm != null){
			data.put("alarm", alarm);
		}else{
			if(alarmApp != null || alarmDev != null){
				String oldAlarmapp = !oldCache.isEmpty() && oldCache.get("alarm_app") != null ? oldCache.get("alarm_app").toString() : "0";
				String oldAlarmdev = !oldCache.isEmpty() && oldCache.get("alarm_dev") != null ? oldCache.get("alarm_dev").toString() : "0";
				alarmApp = alarmApp != null ? alarmApp : oldAlarmapp;
				alarmDev = alarmDev != null ? alarmDev : oldAlarmdev;
				if(alarmApp.equals("1") || alarmDev.equals("1")){
					data.put("alarm","1");
				}else{
					data.put("alarm","0");
				}
			}
		}
		if(oldStatus == null || oldStatus.equals(deviceConf.STATUS_OFFLINE)){
			if(newStatus.equals(deviceConf.STATUS_OFFLINE)){//离线->离线
				log.debug("handle "+mac+" status:offline------->offline");
				if(runtime != null){
					data.put("runtime",runtime);
					data.put("cpu_use","0");
					data.put("memory_use","0");
					data.put("status",deviceConf.STATUS_OFFLINE);
					data.put("offline_time",time.toString());
				}
			}else{//离线->在线
				log.debug("handle "+mac+" status:offline------->online");
				if(cpu_use != null){
					data.put("cpu_use",cpu_use);
				}
				if(memory_use != null){
					data.put("memory_use",memory_use);
				}
				if(runtime != null){
					data.put("runtime",runtime);
				}
				data.put("status",newStatus);
				data.put("online_time", time.toString());
			}
		}else{
			if(newStatus.equals(deviceConf.STATUS_OFFLINE)){//在线->离线
				log.debug("handle "+mac+" status:online------->offline");
				if(runtime != null){
					data.put("runtime",runtime);
				}
				data.put("cpu_use","0");
				data.put("memory_use","0");
				data.put("status",deviceConf.STATUS_OFFLINE);
				data.put("offline_time",time.toString());
			}else{//在线->在线
				log.debug("handle "+mac+" status:online------->online");
				if(cpu_use != null){
					data.put("cpu_use",cpu_use);
				}
				if(memory_use != null){
					data.put("memory_use",memory_use);
				}
				if(runtime != null){
					data.put("runtime",runtime);
				}
				data.put("status",newStatus);
			}
			if(parent != null){
				data.put("parent", parent);
				if(link == null){
					data.put("link","-1");
				}else{
					data.put("link", link);
				}
				if(rssi == null){
					data.put("rssi","-1");
				}else{
					data.put("rssi",rssi);
				}
			}
		}
		if(!data.isEmpty())
			jedis.set(redisConf.DEVICE_DYNAMIC+mac,JSON.toJSONString(data));
	}
	
	public void updateOrderDeviceStatus(String orderID,Map<String,String> datas){
		Jedis jedis = helperRedis.getJedis();
		this.updateOrderDeviceStatus(orderID, datas,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void updateOrderDeviceStatus(String orderID,Map<String,String> datas,Jedis jedis){
		jedis.hmset(redisConf.DEVICE_UPGRADE_STATUS + orderID, datas);
	}
	
	public void setHandledChilds(String mac,int total,int ttl){
		Jedis jedis = helperRedis.getJedis();
		this.setHandledChilds(mac, total, ttl, jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void setHandledChilds(String mac,int total,int ttl,Jedis jedis){
		jedis.setex(redisConf.DEVICECHILDHANDLED+mac,ttl, String.valueOf(total));
	}
	
	public int getHandledChilds(String mac){
		Jedis jedis = helperRedis.getJedis();
		int total = this.getHandledChilds(mac,jedis);
		helperRedis.returnResource(jedis);
		return total;
	}
	
	public int getHandledChilds(String mac,Jedis jedis){
		int total = 0;
		String num = jedis.get(redisConf.DEVICECHILDHANDLED+mac);
		if(num != null){
			total = Integer.parseInt(num);
		}
		return total;
	}
	
	public void delHandledChilds(String mac){
		Jedis jedis = helperRedis.getJedis();
		this.delHandledChilds(mac, jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void delHandledChilds(String mac,Jedis jedis){
		jedis.del(redisConf.DEVICECHILDHANDLED+mac);
	}
	
	public String getAuthClientKey(String radioid,String vapid){
		return this.getAuthClientKey(radioid, vapid,"auth");
	}
	
	public String getAuthClientKey(String radioid,String vapid,String value){
		if(!(radioid == null || radioid.isEmpty()) && !(vapid == null || vapid.isEmpty())){
			value = value + ":" + radioid + ":" + vapid;
		}
		return value;
	}
	
	public Map<String,HashMap<String,HashMap<String,String>>> getAuthAllow(String devMac){
		Jedis jedis = helperRedis.getJedis();
		Map<String,HashMap<String,HashMap<String,String>>> info = this.getAuthAllow(devMac,jedis);
		helperRedis.returnResource(jedis);
		return info;
	}
	
	/**
	 * 数据结构
	 * 记录了终端在哪个radio及vap上的认证信息，包括认证方式，认证时间，有效期，认证状态
	 * {\"52:08:3D:B1:39:DD\":{\"auth:0:0\":{\"login_time\":\"1729157025\",\"authmethod\":\"1\",\"mid\":\"0\",\"expire_in\":\"1729329825\",\"status\":\"1\"}}}
	 */
	public Map<String,HashMap<String,HashMap<String,String>>> getAuthAllow(String devMac,Jedis jedis){
		String key = redisConf.AUTH_ALLOW + devMac;
		Map<String,HashMap<String,HashMap<String,String>>> info = new HashMap<String,HashMap<String,HashMap<String,String>>>();
		String str = jedis.get(key);
		if(str != null && !str.equals("[]")){
			info = JSON.parseObject(str,new TypeReference<Map<String,HashMap<String,HashMap<String,String>>>>(){});
		}
		return info;
	}
	
	public String getAuthAllowStatus(String devMac,String clientMac,String radioid,String vapid){
		Jedis jedis = helperRedis.getJedis();
		String rs = this.getAuthAllowStatus(devMac, clientMac, radioid, vapid,jedis);
		helperRedis.returnResource(jedis);
		return rs;
	}
	
	public String getAuthAllowStatus(String devMac,String clientMac,String radioid,String vapid,Jedis jedis){
		String rs = "2";
		Map<String,HashMap<String,HashMap<String,String>>>authInfo = this.getAuthAllow(devMac,jedis);
		String authClientKey = this.getAuthClientKey(radioid,vapid);
		HashMap<String,HashMap<String,String>> authClient = authInfo.get(clientMac);
		if(authClient != null){
			HashMap<String,String> authClientInfo = authClient.get(authClientKey);
			String expireIn = authClientInfo.get("expire_in");
			if(!expireIn.equals("0") && new Date().getTime()/1000 > Long.parseLong(expireIn)){
				rs = "0";
				if(!authClientInfo.get("status").equals("0")){
					authClientInfo.put("status","0");
					authClient.put(authClientKey,authClientInfo);
					authInfo.put(clientMac,authClient);
					//写入redis
					this.setAuthAllow(devMac, JSON.toJSONString(authInfo),jedis);
				}
			}else{
				rs = authClientInfo.get("status");
			}
		}
		return rs;
	}
	
	public String getAuthAllowStatus(Map<String,HashMap<String,HashMap<String,String>>>authInfo,String devMac,String clientMac,String radioid,String vapid){
		Jedis jedis = helperRedis.getJedis();
		String rs = this.getAuthAllowStatus(authInfo,devMac, clientMac, radioid, vapid,jedis);
		helperRedis.returnResource(jedis);
		return rs;
	}
	
	public String getAuthAllowStatus(Map<String,HashMap<String,HashMap<String,String>>>authInfo,String devMac,String clientMac,String radioid,String vapid,Jedis jedis){
		String rs = "2";
		String authClientKey = this.getAuthClientKey(radioid,vapid);
		HashMap<String,HashMap<String,String>> authClient = authInfo.get(clientMac);
		if(authClient != null){
			HashMap<String,String> authClientInfo = authClient.get(authClientKey);
			if(authClientInfo == null){
				rs = "0";
			}else{
				String expireIn = authClientInfo.get("expire_in");
				if(!expireIn.equals("0") && new Date().getTime()/1000 > Long.parseLong(expireIn)){
					rs = "0";
					if(!authClientInfo.get("status").equals("0")){
						authClientInfo.put("status","0");
						authClient.put(authClientKey,authClientInfo);
						authInfo.put(clientMac,authClient);
						//写入redis
						this.setAuthAllow(devMac, JSON.toJSONString(authInfo),jedis);
					}
				}else{
					rs = authClientInfo.get("status");
				}
			}
		}
		return rs;
	}
	
	public Boolean synsAuthAllow(String devMac,String clientMac,String radioid,String vapid,String status,String expireIn,Boolean isadd){
		Jedis jedis = helperRedis.getJedis();
		Boolean rs = this.synsAuthAllow(devMac, clientMac, radioid, vapid, status, expireIn,isadd,jedis);
		helperRedis.returnResource(jedis);
		return rs;
	}
	
	public Boolean synsAuthAllow(String devMac,String clientMac,String radioid,String vapid,String status,String expireIn,Boolean isadd,Jedis jedis){
		Map<String,HashMap<String,HashMap<String,String>>>authInfo = this.getAuthAllow(devMac,jedis);

		String authClientKey = this.getAuthClientKey(radioid,vapid);
		HashMap<String,String> authClientInfo = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> authClient = new HashMap<String,HashMap<String,String>>();
		
		if(authInfo.get(clientMac) != null){
			if(authInfo.get(clientMac).get(authClientKey) != null){
				authClient = authInfo.get(clientMac);
				authClientInfo = authClient.get(authClientKey);
				if(!(authClient.get(authClientKey).get("status").equals("2") && status.equals("1"))){
					expireIn = authClient.get(authClientKey).get("expire_in");
				}
			}
		}
		authClientInfo.put("status",status);
		authClientInfo.put("expire_in",expireIn);
		
		if(!isadd && authClient.isEmpty()){
			return true;
		}
		authClient.put(authClientKey, authClientInfo);
		authInfo.put(clientMac,authClient);
		return this.setAuthAllow(devMac,JSON.toJSONString(authInfo),jedis);
	}
	
	public Boolean setAuthAllow(String devMac,String data){
		Jedis jedis = helperRedis.getJedis();
		Boolean rs = this.setAuthAllow(devMac, data,jedis);
		helperRedis.returnResource(jedis);
		return rs;
	}
	
	public Boolean setAuthAllow(String devMac,String data,Jedis jedis){
		String key = redisConf.AUTH_ALLOW + devMac;
		jedis.set(key, data);
		return true;
	}
	
	public Map<String,List<String>> getAuthAllowWithRoam(String gid){
		Jedis jedis = helperRedis.getJedis();
		Map<String,List<String>> rs = this.getAuthAllowWithRoam(gid,jedis);
		helperRedis.returnResource(jedis);
		return rs;
	}
	/**
	 * 数据结构
	 * 记录了同组内每个设备上认证过的终端
	 * {devmac => [authed_clientmac]}
	 */
	public Map<String,List<String>> getAuthAllowWithRoam(String gid,Jedis jedis){
		Map<String,List<String>> rs = new HashMap<String,List<String>>();
		String datas = jedis.get(redisConf.AUTH_ROAM_ALLOW+gid);
		if(datas != null){
			try{
				rs = JSON.parseObject(datas,new TypeReference<HashMap<String,List<String>>>(){});
			}catch(Exception e){
				
			}
		}
		return rs;
	}
	
	public void setAuthAllowWithRoam(String gid,String datas){
		Jedis jedis = helperRedis.getJedis();
		jedis.set(redisConf.AUTH_ROAM_ALLOW+gid,datas);
		helperRedis.returnResource(jedis);
	}
	
	public void setAuthAllowWithRoam(String gid,String datas,Jedis jedis){
		jedis.set(redisConf.AUTH_ROAM_ALLOW+gid,datas);
	}
	
	public void setDeviceAuthWithPolicy(String mac,String radioid,String vapid,int ttl,Map<String,Object> info){
		Jedis jedis = helperRedis.getJedis();
		this.setDeviceAuthWithPolicy(mac,radioid,vapid,ttl,info);
		helperRedis.returnResource(jedis);
	}
	
	public void setDeviceAuthWithPolicy(String mac,String radioid,String vapid,int ttl,Map<String,Object> info,Jedis jedis){
		if(!info.isEmpty()){
			String key = redisConf.DEVICE_AUTH_POLICY + mac;
			if(!(radioid == null || radioid.isEmpty()) && !(vapid == null || vapid.isEmpty())){
				key = key + ":" + radioid + ":" + vapid;
			}
			jedis.setex(key, ttl,JSON.toJSONString(info));
		}
	}
	
	public Map<String,Object> getDeviceAuthWithPolicy(String mac,String radioid,String vapid){
		Jedis jedis = helperRedis.getJedis();
		Map<String,Object> info = this.getDeviceAuthWithPolicy(mac,radioid,vapid);
		helperRedis.returnResource(jedis);
		return info;
	}
	
	public Map<String,Object> getDeviceAuthWithPolicy(String mac,String radioid,String vapid,Jedis jedis){
		String key = redisConf.DEVICE_AUTH_POLICY + mac;
		if(!(radioid == null || radioid.isEmpty()) && !(vapid == null || vapid.isEmpty())){
			key = key + ":" + radioid + ":" + vapid;
		}
		Map<String,Object> result = null;
		String info = jedis.get(key);
		if(info != null){
			result = JSON.parseObject(info,new TypeReference<HashMap<String,Object>>(){});
		}
		return result;
	}
	
	public void clearDeviceAuthWithPolicy(String mac){
		Jedis jedis = helperRedis.getJedis();
		this.clearDeviceAuthWithPolicy(mac, jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void clearDeviceAuthWithPolicy(String mac,Jedis jedis){
		Set<String> keys = jedis.keys(redisConf.DEVICE_AUTH_POLICY + mac + "*"); 
		if(!keys.isEmpty()){
			jedis.del(keys.toArray(new String[keys.size()]));
		}
	}
	
	public Boolean clearDeviceAuth(String mac,String gid,Jedis jedis){
		Boolean rs = true;
		//同组认证
		 Map<String,List<String>> roam = this.getAuthAllowWithRoam(gid,jedis);
		 if(roam.containsKey(mac)){
			 roam.remove(mac);
			 this.setAuthAllowWithRoam(gid, JSON.toJSONString(roam), jedis);
		 }
		 Set<String> authpolicykeys = jedis.keys(redisConf.DEVICE_AUTH_POLICY+mac+"*");
		 authpolicykeys.add(redisConf.AUTH_ALLOW + mac);
		 String[] keys = authpolicykeys.toArray(new String[authpolicykeys.size()]);
		jedis.del(keys);
		return rs;
	}
	
	public Map<String,Object> getAuthMember(String mid){
		Jedis jedis = helperRedis.getJedis();
		Map<String,Object> rs = this.getAuthMember(mid, jedis);
		helperRedis.returnResource(jedis);
		return rs;
	}
	
	public Map<String,Object> getAuthMember(String mid,Jedis jedis){
		Map<String,Object> result = new HashMap<String,Object>();
		String rs = jedis.get(redisConf.AUTH_MEMBER + mid);
		if(rs != null && !rs.isEmpty()){
			result = JSON.parseObject(rs,new TypeReference<HashMap<String,Object>>(){});
		}
		return result;
	}
	
	public Boolean setAuthMember(String mid,String data){
		Jedis jedis = helperRedis.getJedis();
		Boolean rs = this.setAuthMember(mid,data,jedis);
		helperRedis.returnResource(jedis);
		return rs;
	}
	
	public Boolean setAuthMember(String mid,String data,Jedis jedis){
		jedis.set(redisConf.AUTH_MEMBER+mid,data);
		return true;
	}
	
	public Map<String,Object> getCloudLatestPackageByType(String type){
		Jedis jedis = helperRedis.getJedis();
		Map<String,Object> rs = this.getCloudLatestPackageByType(type,jedis);
		helperRedis.returnResource(jedis);
		return rs;
	}
	
	public Map<String,Object> getCloudLatestPackageByType(String type,Jedis jedis){
		String str = jedis.get(redisConf.CLOUD_LATEST_PACKAGE + type);
		if(str == null){
			return null;
		}
		return JSON.parseObject(str,new TypeReference<Map<String,Object>>(){});
	}
	
	public Boolean setCloudLatestPackageByType(String type,Map<String,Object> newPackage){
		Jedis jedis = helperRedis.getJedis();
		this.setCloudLatestPackageByType(type, newPackage,jedis);
		helperRedis.returnResource(jedis);
		return true;
	}
	
	public Boolean setCloudLatestPackageByType(String type,Map<String,Object> data,Jedis jedis){
		jedis.set(redisConf.CLOUD_LATEST_PACKAGE+type,JSON.toJSONString(data));
		return true;
	}
	
	public void set(String key,String value){
		Jedis jedis = helperRedis.getJedis();
		this.set(key, value,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void set(String key,String value,Jedis jedis){
		jedis.set(key, value);
	}
	
	public String get(String key){
		Jedis jedis = helperRedis.getJedis();
		String result = this.get(key);
		helperRedis.returnResource(jedis);
		return result;
	}
	
	public String get(String key,Jedis jedis){
		return jedis.get(key);
	}
	
	public void incr(String key){
		Jedis jedis = helperRedis.getJedis();
		this.incr(key,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void incr(String key,Jedis jedis){
		jedis.incr(key);
	}
	
	public void expire(String key,int seconds){
		Jedis jedis = helperRedis.getJedis();
		this.expire(key,seconds,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void expire(String key,int seconds,Jedis jedis){
		jedis.expire(key, seconds);
	}
	
	public void setex(String key,int seconds,String value){
		Jedis jedis = helperRedis.getJedis();
		this.setex(key,seconds,value,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void setex(String key,int seconds,String value,Jedis jedis){
		jedis.setex(key, seconds, value);
	}
	
	public void lpush(String key,String data){
		Jedis jedis = helperRedis.getJedis();
		this.lpush(key,data,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void lpush(String key,String data,Jedis jedis){
		jedis.lpush(key,data);
	}
	
	public void delete(String key){
		Jedis jedis = helperRedis.getJedis();
		this.delete(key);
		helperRedis.returnResource(jedis);
	}
	
	public void delete(String key,Jedis jedis){
		jedis.del(key);
	}
	
	public void delete(String[] keys){
		Jedis jedis = helperRedis.getJedis();
		this.delete(keys);
		helperRedis.returnResource(jedis);
	}
	
	public void delete(String[] keys,Jedis jedis){
		jedis.del(keys);
	}
	
	public void handleEth(String mac,List<Map<String,String>> eth){
		Jedis jedis = helperRedis.getJedis();
		this.handleEth(mac,eth,jedis);
		helperRedis.returnResource(jedis);
	}
	
	public void handleEth(String mac,List<Map<String,String>> eth,Jedis jedis){
		if(eth.size() > 0){
			Pipeline p = jedis.pipelined();
			for(Map<String,String> data : eth){
				String ethMac = data.get("mac");
				if(ethMac != null){
					p.set(redisConf.MAC_ETH_TO_DEV + ethMac, mac);
				}
			}
			p.syncAndReturnAll();
		}
	}
	
	public String getParentMac(String ethMac){
		String res = "";
		Jedis jedis = helperRedis.getJedis();
		res = this.getParentMac(ethMac,jedis);
		helperRedis.returnResource(jedis);
		return res;
	}
	
	public String getParentMac(String ethMac,Jedis jedis){
		return jedis.get(redisConf.MAC_ETH_TO_DEV + ethMac);
	}
}
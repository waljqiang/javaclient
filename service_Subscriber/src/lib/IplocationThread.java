package lib;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import conf.publicConf;
import redis.clients.jedis.Jedis;
import data.Device;

//ip定位
public class IplocationThread extends Thread{
	private static final Log log = LogFactory.getLog(IplocationThread.class);
	private Jedis jedis=null;
	
	@Override
	public void run() {
		while(true){
			Boolean flag = true;
			jedis=helperRedis.getJedis();
		 	RedisCache redisCache = new RedisCache();
		 	Device deviceDB = new Device();
		 	Map<String,String> data = new HashMap<String,String>();
		 	String mac = null;
		 	String net_ip = null;
		 	Date ldate = new Date();
	 		Long time = ldate.getTime()/1000;
			while(flag){
				data = redisCache.getLocation(jedis);
				if(data.isEmpty()){
					flag = false;
				}else{
					final JdbcUtils jdbcUtils = new JdbcUtils();
				 	jdbcUtils.getConnection();
					mac = data.get("mac");
					net_ip = data.get("net_ip");
					log.fatal("start location the device["+mac+"] by ip");
					try{
						this.handleData(mac,net_ip,time,deviceDB,jedis,jdbcUtils);
						log.fatal("location the device["+mac+"] by ip success");
					}catch(MyException e){
						log.error(e.getMessage());
					}
					jdbcUtils.releaseConn();
					mac = null;
					net_ip = null;
					data.clear();
				}
			}
			if(jedis != null){
				helperRedis.returnResource(jedis);
	  		}
			try{
				Thread.sleep(3000);
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
	
	public void handleData(String mac,String net_ip,Long time,Device deviceDB,Jedis jedis,JdbcUtils jdbcUtils) throws MyException{
		try{
			Map<String,String> locationInfo = this.getlocationWithIP(mac,net_ip);
			if(locationInfo.isEmpty()){
				throw new MyException("location device["+mac+"] with IP["+net_ip+"] remote failure",MyException.IP_LOCATION_FAIL);
			}
			Map<String,Object> area = deviceDB.getDeviceAreaByCity(locationInfo.get("city").toString(),jdbcUtils);
			String areaCode = area.isEmpty() ? "0" : area.get("code").toString();
			if(!(deviceDB.saveLocation(mac,areaCode,locationInfo,time,jdbcUtils))){
				throw new MyException("location device["+mac+"] with IP["+net_ip+"] failure",MyException.IP_LOCATION_FAIL);
			}
		}catch(MyException e){
			throw new MyException("location device["+mac+"] with IP["+net_ip+"] remote failure,cause:["+e.getMessage()+"]",MyException.IP_LOCATION_FAIL);
		}catch(Exception e){
			throw new MyException("location device["+mac+"] with IP["+net_ip+"] remote failure,cause:["+e.getMessage()+"]",MyException.IP_LOCATION_FAIL);
		}
	}
	
	public Map<String,String> getlocationWithIP(String mac,String ip) throws MyException{
		Map<String,String> res = new HashMap<String,String>();
		String url = String.format(publicConf.iplocation_baiduapi,ip);
		String locationInfoStr = HttpRequest.sendGet(url,null);
		JSONObject locationInfoObj = JSON.parseObject(locationInfoStr);
		if(locationInfoObj != null && locationInfoObj.getIntValue("status") == 0){
			res.put("country", publicConf.iplocation_china);
			res.put("province",Helper.jsonGet(locationInfoObj, "content.address_detail.province", ""));
			res.put("city", Helper.jsonGet(locationInfoObj,"content.address_detail.city",""));
			res.put("address", Helper.jsonGet(locationInfoObj,"content.address_detail.district","")+Helper.jsonGet(locationInfoObj,"content.address_detail.street","")+Helper.jsonGet(locationInfoObj, "content.address_detail.street_number",""));
			res.put("longitude",Helper.jsonGet(locationInfoObj, "content.point.x",""));
			res.put("latitude", Helper.jsonGet(locationInfoObj, "content.point.y",""));
		}else if(locationInfoObj.getIntValue("status") == 2){
			throw new MyException(locationInfoObj.getString("message"),MyException.IP_LOCATION_FAIL);
		}else{
			url = String.format(publicConf.iplocation_ipapi,ip,"zh-CN");
			locationInfoStr = HttpRequest.sendGet(url,null);
			locationInfoObj = JSON.parseObject(locationInfoStr);
			if(locationInfoObj != null && locationInfoObj.getString("status").toLowerCase() == "success"){
				res.put("country",Helper.jsonGet(locationInfoObj, "country",""));
				res.put("province",Helper.jsonGet(locationInfoObj, "regionName", ""));
				res.put("city", Helper.jsonGet(locationInfoObj,"city",""));
				res.put("address", Helper.jsonGet(locationInfoObj,"district",""));
				res.put("longitude",Helper.jsonGet(locationInfoObj, "lon",""));
				res.put("latitude", Helper.jsonGet(locationInfoObj, "lat",""));
			}else{
				throw new MyException(locationInfoObj.getString("message"),MyException.IP_LOCATION_FAIL);
			}
		}
		return res;
	}
}
package lib;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import conf.publicConf;
import data.Device;

public class LocationExecutor extends ExcutorTask{
	private static final Log log = LogFactory.getLog(LocationExecutor.class);
	@Override
	public void run(){
		log.fatal("Start location with latitude and longitude task");
		Boolean flag = true;
		final JdbcUtils jdbcUtils = new JdbcUtils();
		jdbcUtils.getConnection();
		Date ldate = new Date();
 		Long time = ldate.getTime()/1000;
		Device deviceDB = new Device();
		List<String> failures = new ArrayList<String>();	
		while(flag){
			List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>();
			try{
				datas = deviceDB.getDatasWithNolocation(publicConf.location_task_num,jdbcUtils);
			}catch(Exception e){
				log.error(e.getMessage());
			}
			if(datas.isEmpty()){
				flag = false;
				break;
			}else{
				for(int i=0;i<datas.size();i++){
					Map<String,Object> device = datas.get(i);
					String mac = device.get("dev_mac").toString();
					String lng = device.get("longitude").toString();
					String lat = device.get("latitude").toString();
					if(lng.equals("0E-7")){
						lng = "0.000000";
					}
					if(lat.equals("0E-7")){
						lat = "0.000000";
					}
					log.debug("Start location the device["+mac+"]");
					Map<String,String> rs = this.getLocationWithLatLon(lng,lat);
					if(!rs.isEmpty()){//成功设备
						rs.put("lstatus","4");
						if(deviceDB.upLocationWithlnglat(mac,rs,time,jdbcUtils)){
							log.debug("Location the device["+mac+"] success");
						}else{
							log.error("Location the device["+mac+"] failure,cause:mysql exec failure");
						}
					}else{
						log.error("Location the device["+mac+"] failure,cause location failure");
						//失败设备
						failures.add(mac);
					}
				}
				//更新失败设备状态
				if(!failures.isEmpty())
					deviceDB.upLocationLstatus(failures,"3",time,jdbcUtils);
			}
		}
		jdbcUtils.releaseConn();
		log.fatal("End location with latitude and longitude task");
	}
	
	public Map<String,String> getLocationWithLatLon(String longitude,String latitude){
		Map<String,String> result = new HashMap<String,String>();
		String url = String.format(publicConf.location_api,"{'lon':"+longitude+",'lat':"+latitude+",'ver':1}");
		try{
			String locationInfoStr = HttpRequest.sendGet(url,null);
			JSONObject locationInfoObj = JSON.parseObject(locationInfoStr);
			if(locationInfoObj != null && locationInfoObj.getString("status").equals("0")){
				result.put("country",Helper.jsonGet(locationInfoObj, "result.addressComponent.nation",""));
				result.put("province", Helper.jsonGet(locationInfoObj, "result.addressComponent.province",""));
				result.put("city",Helper.jsonGet(locationInfoObj, "result.addressComponent.city",""));
				result.put("address", Helper.jsonGet(locationInfoObj, "result.formatted_address",""));
			}
		}catch(Exception e){
			
		}
		return result;
	}
	
	public String getName(){
		return "location-lnglat";
	}
}
package lib;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import conf.publicConf;
import data.Device;

public class ClientStaticsExecutor extends ExcutorTask{
	private static final Log log = LogFactory.getLog(ClientStaticsExecutor.class);
		
	@Override
	public void run(){
		log.fatal("Start statics clients for device by hours task");
		Boolean flag = true;
		final JdbcUtils jdbcUtils = new JdbcUtils();
		jdbcUtils.getConnection();
		Device deviceDB = new Device();
		RedisCache redisCache = new RedisCache();
		while(flag){
	 		Long time = new Date().getTime()/1000;
	 		List<Map<String,String>> datas = redisCache.getClients(publicConf.client_statics_task_num);
	 		if(datas.isEmpty()){
	 			flag = false;
	 		}else{
	 			JSONObject keyDatas = new JSONObject();
				JSONObject resultDatas = new JSONObject();
	 			int j = 0;
	 			//按小时统计设备上在线人数
				for(int i=0;i<datas.size();i++){
					Map<String,String> data = datas.get(i);
					String mac = data.get("mac");
					String hours = Helper.getTimeEndHour(data.get("created_at"));				
					if(keyDatas.getJSONObject(mac) != null){
						if(keyDatas.getJSONObject(mac).getString(hours) != null){
							JSONObject resultDatasJ = resultDatas.getJSONObject(keyDatas.getJSONObject(mac).getString(hours));
							resultDatasJ.put("onlines",data.get("onlines"));
						}else{
							JSONObject keyMacDatas = keyDatas.getJSONObject(mac);
							keyMacDatas.put(hours, j);
							keyDatas.put(mac,keyMacDatas);
							
							JSONObject resultDatasJ = new JSONObject();
							resultDatasJ.put("mac", data.get("mac"));
							resultDatasJ.put("uid", data.get("uid"));
							resultDatasJ.put("onlines", data.get("onlines"));
							resultDatasJ.put("hours",hours);
							resultDatasJ.put("created_at", time);
							resultDatasJ.put("updated_at", time);
							resultDatas.put(String.valueOf(j),resultDatasJ);
						}
					}else{
						JSONObject keyMacDatas = new JSONObject();
						keyMacDatas.put(hours, j);
						keyDatas.put(mac,keyMacDatas);
						JSONObject resultDatasJ = new JSONObject();
						resultDatasJ.put("mac", data.get("mac"));
						resultDatasJ.put("uid", data.get("uid"));
						resultDatasJ.put("onlines", data.get("onlines"));
						resultDatasJ.put("hours",hours);
						resultDatasJ.put("created_at", time);
						resultDatasJ.put("updated_at", time);
						resultDatas.put(String.valueOf(j),resultDatasJ);
					}
					j = j+1;
				}
				//按小时统计用户活跃度
				JSONObject userDatas = new JSONObject();
				for(Map.Entry<String,Object> entry : resultDatas.entrySet()){
					JSONObject value = JSON.parseObject(entry.getValue().toString());
					String uid = value.getString("uid");
					String hours = value.getString("hours");
	    			if(userDatas.getJSONObject(uid) != null){
	    				JSONObject userDatasJ = userDatas.getJSONObject(uid);
	    				if(userDatas.getJSONObject(uid).getJSONObject(hours) != null){
	    					JSONObject userDatasHours = userDatas.getJSONObject(uid).getJSONObject(hours);
	    					userDatasHours.put("onlines",userDatasHours.getIntValue("onlines")+value.getIntValue("onlines"));
	    					userDatasJ.put(hours, userDatasHours);
	    					userDatas.put(uid,userDatasJ);
	    				}else{
		    				JSONObject userDatasHoursObj = new JSONObject();
		    				userDatasHoursObj.put("uid", uid);
		    				userDatasHoursObj.put("onlines",value.getString("onlines"));
		    				userDatasHoursObj.put("hours", hours);
		    				userDatasHoursObj.put("created_at", value.getString("created_at"));
		    				userDatasHoursObj.put("updated_at", value.getString("updated_at"));
		    				userDatasJ.put(hours, userDatasHoursObj);
		    				userDatas.put(uid, userDatasJ);
	    				}
	    			}else{
	    				JSONObject userDatasHours = new JSONObject();
	    				JSONObject userDatasHoursObj = new JSONObject();
	    				userDatasHoursObj.put("uid", uid);
	    				userDatasHoursObj.put("onlines",value.getString("onlines"));
	    				userDatasHoursObj.put("hours", hours);
	    				userDatasHoursObj.put("created_at", value.getString("created_at"));
	    				userDatasHoursObj.put("updated_at", value.getString("updated_at"));
	    				userDatasHours.put(hours, userDatasHoursObj);
	    				userDatas.put(uid, userDatasHours);
	    			}
				}
				jdbcUtils.setAutoCommit(false);
				Boolean rs1 = deviceDB.saveUserStatics(userDatas,jdbcUtils);
				Boolean rs2 = deviceDB.saveClientsStatics(resultDatas,jdbcUtils);			
				if(!rs2 || !rs2){
					log.error("ClientsForDeviceByHours error,cause:mysql exec failure rs1["+rs1+"]rs2["+rs2+"]");
					jdbcUtils.rollback();
				}else{
					jdbcUtils.commit();
				}
	 		}
		}
		jdbcUtils.setAutoCommit(true);
		jdbcUtils.releaseConn();
		log.fatal("End statics clients for device by hours task");
	}
	
	public String getName(){
		return "client-statics";
	}
	
}
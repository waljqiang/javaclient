package deviceup;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import conf.deviceConf;
import conf.mysqlConf;
import conf.yunlotConf;
import deviceup.HttpRequest;
import deviceup.Device;
import lib.JdbcUtils;
import lib.MqttClientManager;
import org.fusesource.mqtt.client.CallbackConnection;

public class DeviceUpBase {
	
//	private static final Log logger = LogFactory.getLog(DeviceUpBase.class);
//	
//	private static String token = "";
//	//定义连接全局变量
//    private List<CallbackConnection> globalConnections = new ArrayList<>();
//    
//	 public List<CallbackConnection> getGlobalConnections() {
//	        return globalConnections;
//    }
//	//绑定设备
//	 public void bindedDevices(List<String> macs) {
//	        int i = 1;
//	        for (String mac : macs) {
//	            bindDeviceData(String.valueOf(i), mac);
//	            i++;
//	        }
//	    }
//      //绑定设备数据处理
//    public void bindDeviceData(String identity, String mac) {
//	        try {
//	            // 获取客户端信息
//	        	JSONObject clients = DeviceUpBase.getClient(mac); 
//	        	
//	            Thread.sleep(100); // Java中sleep的单位是毫秒
//	            logger.error("client is :"+clients.get("cltid"));
//	            
//	            
//	           // 绑定设备
//	             String bind = DeviceUpBase.bindDevice(mac); // 
//	            if (clients.isEmpty() || bind.isEmpty()) {
//	            	logger.error("bind device mac  is:" + mac + " bind failure");
//	            } else {
//	                Device device = new Device();
//	        		device.setMac(mac);
//	        		device.setPrtid(deviceConf.prtid);
//	        		device.setType(deviceConf.dev_type);
//	        		device.setCltid(clients.getString("cltid"));
//	    			device.setMqtthost(clients.getString("server"));
//	    			device.setMqttport(clients.getInteger("port"));
//	                logger.fatal("bind device[" + identity + "][" + mac + "] ok");
//	            }
//	        } catch (Exception e) {
//	            // 处理异常情况
//	            System.out.println("bind device[" + identity + "][" + mac + "] failure");
//	            logger.error("bind device[" + identity + "][" + mac + "] failure, caused:" + e.getMessage());
//	        }
//	    }
//	// 获取客户端信息
////    public static JSONObject getClient(String mac) {
////    	 logger.debug("请求mac地址：："+mac);
////        String url = yunlotConf.cloudnetlot_api_getclient;
////        logger.debug("请求网址："+url);
////        String buffers = String.format("{\"appid\":\"%s\",\"secret\":\"%s\",\"prtid\":\"%s\",\"mac\":\"%s\",\"type\":\"%s\",\"mark\":\"%s\",\"rebind\":\"%s\",\"encrypt\":\"%s\"}",
////        		deviceConf.app_id, deviceConf.app_secret, deviceConf.prtid, mac, deviceConf.dev_type, deviceConf.mark, deviceConf.is_rebind, deviceConf.encrypt);	
////        logger.debug("请求参数："+buffers);
////        try {
////        	String result = HttpRequest.sendPostJson(url,buffers);
////        	logger.debug("接口返回："+result);
////        	
////        	JSONObject resultO = JSON.parseObject(result);
////            if(resultO.getString("status").equals("10000")){
////				return resultO.getJSONObject("data");
////			}else{
////				logger.error("get the client for the device is :"+mac+" 接口返回是："+result);
////				return null;
////			}
////        	
////        } catch (Exception e) {
////        	logger.error("获取客户端信息失败");
////			return null;
////        }
////    }
//	
//	/*
//	 * 绑定设备
//	 */
//	public static String bindDevice(String mac){
//		String bind = "";
////		if(token.isEmpty()){
////			generateToken();
////		}
////	    String url = yunlotConf.cloudnetlot_api_bind;
////		String datas = "{\"mac\":\""+mac+"\",\"username\":\"admin\",\"password\":\"admin\",\"gid\":\""+deviceConf.gid+"\"}";
////		logger.debug("绑定设备请求数据："+datas);
////	    logger.debug("绑定设备请求网址："+url);
////		Map<String,String> header = new HashMap<String,String>();
////		header.put("Authorization","Bearer "+token);
////		String result = HttpRequest.sendPostJson(yunlotConf.cloudnetlot_api_bind,header,datas);
////		try{
////			JSONObject resultO = JSON.parseObject(result);
////			logger.debug("绑定设备返回数据："+resultO);
////			if(resultO.getString("status").equals("10000")){
////				bind = resultO.getJSONObject("data").getString("bind");
////			}else if(resultO.getJSONArray("errorCode").get(0).toString().equals("600100104")){
////				token = "";
////				bind = bindDevice(mac);
////			}else{
////				logger.error("bind the device mac failure, "+"mac is ："+mac);
////			}
////		}catch(Exception e){
////			logger.error("bind the device mac failure, "+"mac is ："+mac+"message is "+e.getMessage());
////		}
//		return bind;
//	}
//	
//	public String getNextMac(String mac){
//		Long macdec = this.hexdec(this.parseMac(mac))+1;
//		return this.setMac(this.padLeft(this.dechex(macdec),12,'0'));
//	}
//	
//	public String setMac(String mac){
//		return mac.substring(0,2) + ":" + mac.substring(2,4) + ":" + mac.substring(4,6) + ":" + mac.substring(6,8) + ":" + mac.substring(8,10) + ":" + mac.substring(10,12);
//	}
//	
//	public String parseMac(String mac){
//		return mac.replace(":","");
//	}
//	
//	public Long hexdec(String str){
//		long sum=0,tmp=0;
//		for(int i=0;i<str.length();i++){
//			char c = str.charAt(i);
//			if(c>='0'&&c<='9'){
//				tmp = c-'0';
//			}else if(c>='A'&&c<='F'){
//				tmp=c-'A'+10;
//			}else{
//				break;
//			}
//			sum=sum*16+tmp;
//		}
//		return sum; 
//	}
//	
//	public String dechex(long num) {
//        return Long.toHexString(num).toUpperCase();
//    }
//	
//	public String padLeft(String str,int len,char ch){
//		int diff = len - str.length();
//		if(diff <= 0){
//			return str;
//		}
//		int charLength = len - str.length();
//		char[] charr = new char[charLength];
//		for(int i = 0;i < charLength;i++){
//			charr[i] = ch;
//		}
//		return new String(charr) + str;
//	}
//	
//	/*
//	 * 获取token
//	 */
//	private static void generateToken(){
////		String datas = "{\"username\":\""+yunlotConf.user_account+"\",\"password\":\""+yunlotConf.user_password+"\"}";
////		logger.debug("登录请求数据："+datas);
////		String result = HttpRequest.sendPostJson(yunlotConf.cloudnetlot_api_gettoken, datas);
////		logger.debug("登录返回数据："+result);
////		JSONObject resultO = JSON.parseObject(result);
////		if(resultO.getString("status").equals("10000")){
////			token = resultO.getJSONObject("data").getString("access_token");
////		}else{
////			logger.error("login failure,caused:"+result);
////		}
//	}
//	
//	//初始化设备
//	public void initDevices(List<String> macs) {
//		
//		List<Device> devices = new ArrayList<>(); // 定义devices集合
//		 // 查询设备
//        String sql = "SELECT prtid, cltid, dev_mac, type, bind FROM " + mysqlConf.prefix + "device WHERE dev_mac IN (";
//        StringBuilder inClause = new StringBuilder();
//        List<Object> params = new ArrayList<>();
//        for (int i = 0; i < macs.size(); i++) {
//            inClause.append("?");
//            params.add(macs.get(i)); // 添加参数到列表
//            if (i < macs.size() - 1) {
//                inClause.append(",");
//            }
//        }
//        sql += inClause.toString() + ") ORDER BY dev_mac ASC";
//       // Device device = new Device();
//		//
//        JdbcUtils jdbcUtils = new JdbcUtils();
//        MqttClientManager mqttClientManager = new MqttClientManager();
//        jdbcUtils.getConnection();
//    	try{
//             List<Map<String, Object>> result = jdbcUtils.findModeResult(sql, params);
//             int i = 1;
//             for (Map<String, Object> line : result) {
//            	 Device  device = new Device();
//            	   // device.setIdenty(String.valueOf(i));
//            	    device.setPrtid((String) line.get("prtid"));
//            	    device.setCltid((String) line.get("cltid"));
//            	    device.setMac((String) line.get("mac"));
//            	    device.setType((String) line.get("type"));
//            	    device.setBind((String) line.get("bind"));
//            	    
//            	    CallbackConnection connection = mqttClientManager.getMqttClient(device);
//            	  //  globalConnections.add(connection);//保存当前连接进全局变量
//            	    devices.add(device);
//            	    //mqttClientManager.setMqttClient(connection);
//            	    i++;
//            	}
//             jdbcUtils.releaseConn();
//            
//    	}
//            catch(Exception e){
//        	logger.error("handleBindsStatisticDay:"+e);
//		}
//    	
//
//	}
//	/**
//	 * 获取等待时间
//	 */
//	//public int getReportwait(){
//	//	int mintime = deviceConf.DEV_REPORT_WAIT_MIN;
//		//int maxtime = deviceConf.DEV_REPORT_WAIT_MAX;
//		//return mintime + (int)(Math.random()*(maxtime-mintime+1));
//	//}
}

package lib.threads;
import lib.Helper;
import lib.JdbcUtils;
import lib.JdbcUtilsPool;
import lib.MqttClientManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.mqtt.client.CallbackConnection;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import  conf.deviceConf;
import conf.mqttConf;
import conf.mysqlConf;
import conf.publicConf;
import conf.yunlotConf;
import deviceup.Device;
import deviceup.DeviceUpBase;
import deviceup.DeviceUpThread;
import deviceup.HttpRequest;

public class PerfThread  extends Thread  {
	private static final Log logger = LogFactory.getLog(PerfThread.class);
	
	private ArrayList<Device> devices = new ArrayList<Device>();
	private String token = "";
	
	public PerfThread(){

	}
	
	
	
	public void run (){
		try {
	        List<String> macs = Helper.generateMacs(deviceConf.dev_nums);

			//写绑定业务逻辑
			if(deviceConf.is_rebind){
				logger.debug("binding start");
				this.handleBinds(macs);
				logger.debug("binding end");
			}else{
				this.initDevices(macs);
			}
			
			//上报
			//parse device
			this.parseDevice();
			//上报数据
			for(Device device:this.devices){
				DeviceUpThread deviceUpThr = new DeviceUpThread(device);
				deviceUpThr.start();
			}
			
		
		}catch (Exception e) {
            // 记录详细的错误信息
            System.err.println("[FATAL] " + new Date() + " [" + Thread.currentThread().getName() + "] [" + this.getClass().getSimpleName() + ".run:" + 32 + "] process failure:");
            e.printStackTrace();
            // 根据需要，可以选择重新抛出异常或进行其他错误处理
        }
	}
	
	private void parseDevice(){
		List<Integer> waittimes = new ArrayList<Integer>();
		for(int i=0;i<this.devices.size();i++){
			waittimes.add((int)(Math.random()*(publicConf.report_start_time+1)));
		}
		Collections.sort(waittimes);

		for(int i=0;i<this.devices.size();i++){
			devices.get(i).setWaittime(waittimes.get(i));
			devices.get(i).setReportInterval(publicConf.report_interval);
		}
		
	}
	
	private void initDevices(List<String> macs){
		// 查询设备
		String sql = "SELECT prtid, cltid, dev_mac, type, bind FROM " + mysqlConf.prefix + "device WHERE dev_mac IN (";
		StringBuilder inClause = new StringBuilder();
		List<Object> params = new ArrayList<>();
		for (int i = 0; i < macs.size(); i++) {
			inClause.append("?");
			params.add(macs.get(i)); // 添加参数到列表
			if (i < macs.size() - 1) {
				inClause.append(",");
			}
		}
		sql += inClause.toString() + ") ORDER BY dev_mac ASC";

		JdbcUtils jdbcUtils = new JdbcUtils();
		jdbcUtils.getConnection();
		try{
            List<Map<String, Object>> result = jdbcUtils.findModeResult(sql, params);
            int i = 1;
            for (Map<String, Object> line : result) {
           	 Device  device = new Device();
           	    device.setIdenty(String.valueOf(i));
           	    device.setPrtid((String) line.get("prtid"));
           	    device.setCltid((String) line.get("cltid"));
           	    device.setMac((String) line.get("mac"));
           	    device.setType((String) line.get("type"));
           	    device.setBind((String) line.get("bind"));
           	    device.setMqtthost(mqttConf.address);
           	    device.setMqttport(mqttConf.port);

           	    devices.add(device);
           	    i++;
           	} 
		}catch(Exception e){
			logger.error("init devices failure,caused:"+e);
		}
		jdbcUtils.releaseConn();
   	
	}
	
	private void handleBinds(List<String> macs){
		int i = 1;
        for (String mac : macs) {
            this.handleBind(String.valueOf(i), mac);
            i++;
        }
	}
	
	 //绑定设备数据处理
    public void handleBind(String identity, String mac) {
        try {
            // 获取客户端信息
        	JSONObject clients = this.getClient(mac); 
        	
            Thread.sleep(100); // Java中sleep的单位是毫秒
                   
           // 绑定设备
             String bind = this.bindDevice(mac); // 
            if (clients.isEmpty() || bind.isEmpty()) {
            	logger.error("bind device mac  is:" + mac + " bind failure");
            } else {
                Device device = new Device();
                device.setIdenty(identity);
        		device.setPrtid(deviceConf.prtid);
        		device.setCltid(clients.getString("cltid"));
        		device.setMac(mac);
        		device.setType(deviceConf.dev_type);
    			device.setMqtthost(clients.getString("server"));
    			device.setMqttport(clients.getInteger("port"));
    			this.devices.add(device);
                logger.fatal("bind device[" + identity + "][" + mac + "] ok");
            }
        } catch (Exception e) {
            // 处理异常情况
            System.out.println("bind device[" + identity + "][" + mac + "] failure");
            logger.error("bind device[" + identity + "][" + mac + "] failure, caused:" + e.getMessage());
        }
    }
    
 // 获取客户端信息
    public JSONObject getClient(String mac) {
    	 logger.debug("请求mac地址：："+mac);
        String url = publicConf.cloudnetlot_api_getclient;
        logger.debug("请求网址："+url);
        String buffers = String.format("{\"appid\":\"%s\",\"secret\":\"%s\",\"prtid\":\"%s\",\"mac\":\"%s\",\"type\":\"%s\",\"mark\":\"%s\",\"rebind\":\"%s\",\"encrypt\":\"%s\"}",
        		deviceConf.app_id, deviceConf.app_secret, deviceConf.prtid, mac, deviceConf.dev_type, deviceConf.mark, deviceConf.is_rebind, deviceConf.encrypt);	
        logger.debug("请求参数："+buffers);
        try {
        	String result = HttpRequest.sendPostJson(url,buffers);
        	logger.debug("接口返回："+result);
        	
        	JSONObject resultO = JSON.parseObject(result);
            if(resultO.getString("status").equals("10000")){
				return resultO.getJSONObject("data");
			}else{
				logger.error("get the client for the device is :"+mac+" 接口返回是："+result);
				return null;
			}
        	
        } catch (Exception e) {
        	logger.error("获取客户端信息失败");
			return null;
        }
    }
    
    /*
	 * 绑定设备
	 */
	public String bindDevice(String mac){
		String bind = "";
		if(this.token.isEmpty()){
			this.generateToken();
		}
	    String url = publicConf.cloudnetlot_api_bind;
		String datas = "{\"mac\":\""+mac+"\",\"username\":\"admin\",\"password\":\"admin\",\"gid\":\""+deviceConf.gid+"\"}";
		logger.debug("绑定设备请求数据："+datas);
	    logger.debug("绑定设备请求网址："+url);
		Map<String,String> header = new HashMap<String,String>();
		header.put("Authorization","Bearer "+this.token);
		String result = HttpRequest.sendPostJson(publicConf.cloudnetlot_api_bind,header,datas);
		try{
			JSONObject resultO = JSON.parseObject(result);
			logger.debug("绑定设备返回数据："+resultO);
			if(resultO.getString("status").equals("10000")){
				bind = resultO.getJSONObject("data").getString("bind");
			}else if(resultO.getJSONArray("errorCode").get(0).toString().equals("600100104")){
				this.token = "";
				bind = bindDevice(mac);
			}else{
				logger.error("bind the device mac failure, "+"mac is ："+mac);
			}
		}catch(Exception e){
			logger.error("bind the device mac failure, "+"mac is ："+mac+"message is "+e.getMessage());
		}
		return bind;
	}
	
	/*
	 * 获取token
	 */
	private void generateToken(){
		String datas = "{\"username\":\""+publicConf.user_account+"\",\"password\":\""+publicConf.user_password+"\"}";
		logger.debug("登录请求数据："+datas);
		String result = HttpRequest.sendPostJson(publicConf.cloudnetlot_api_gettoken, datas);
		logger.debug("登录返回数据："+result);
		JSONObject resultO = JSON.parseObject(result);
		if(resultO.getString("status").equals("10000")){
			this.token = resultO.getJSONObject("data").getString("access_token");
		}else{
			logger.error("login failure,caused:"+result);
		}
	}
}

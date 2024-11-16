package test.devicereport;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import test.com.Helper;
import test.com.HttpRequest;

public class ReportBase {

	private static final Log logger = LogFactory.getLog(ReportBase.class);
	
	private static String token = "";
	
	/**
	 * 获取客户端
	 * @param device
	 * @return
	 */
	public JSONObject getClient(Device device){
		String datas = "{\"appid\":\""+publicConf.APPID+"\","
				+ "\"secret\":\""+publicConf.SECRET+"\","
						+ "\"prtid\":\""+device.getPrtid()+"\","
								+ "\"mac\":\""+device.getMac()+"\","
										+ "\"type\":\""+device.getType()+"\"}";
		String result = HttpRequest.sendPostJson(publicConf.CLOUDNETLOT_API_GETCLIENT,datas);
		try{
			JSONObject resultO = JSON.parseObject(result);
			if(resultO.getString("status").equals("10000")){
				return resultO.getJSONObject("data");
			}else{
				logger.error("get the client for the device["+device.getIdenty()+"]["+device.getMac()+"] failure,caused:"+result);
				return null;
			}
		}catch(Exception e){
			logger.error("get the client for the device["+device.getIdenty()+"]["+device.getMac()+"] failure,caused:result["+result+"]message["+e.getMessage()+"]");
			return null;
		}
	}
	
	/*
	 * 绑定设备
	 */
	public String bindDevice(Device device){
		String bind = "";
		if(token.isEmpty()){
			this.generateToken();
		}
		String datas = "{\"mac\":\""+device.getMac()+"\",\"username\":\"admin\",\"password\":\"admin\",\"gid\":\""+publicConf.GID+"\"}";
		Map<String,String> header = new HashMap<String,String>();
		header.put("Authorization","Bearer "+token);
		String result = HttpRequest.sendPostJson(publicConf.CLOUDNETLOT_API_BIND,header,datas);
		try{
			JSONObject resultO = JSON.parseObject(result);
			if(resultO.getString("status").equals("10000")){
				bind = resultO.getJSONObject("data").getString("bind");
			}else if(resultO.getJSONArray("errorCode").get(0).toString().equals("600100104")){
				token = "";
				bind = this.bindDevice(device);
			}else{
				logger.error("bind the device["+device.getIdenty()+"]["+device.getMac()+"] failure,caused:"+result);
			}
		}catch(Exception e){
			logger.error("bind the device["+device.getIdenty()+"]["+device.getMac()+"] failure,caused:result["+result+"]message["+e.getMessage()+"]");
		}
		return bind;
	}
	
	public String getNextMac(String mac){
		Long macdec = Helper.hexdec(Helper.parseMac(mac))+1;
		return Helper.setMac(Helper.padLeft(Helper.dechex(macdec),12,'0'));
	}
	
	/*
	 * 获取token
	 */
	private void generateToken(){
		String datas = "{\"username\":\""+publicConf.USER_ACCOUNT+"\",\"password\":\""+publicConf.USER_PASSWORD+"\"}";
		String result = HttpRequest.sendPostJson(publicConf.CLOUDNETLOT_API_GETTOKEN, datas);
		JSONObject resultO = JSON.parseObject(result);
		if(resultO.getString("status").equals("10000")){
			token = resultO.getJSONObject("data").getString("access_token");
		}else{
			logger.error("login failure,caused:"+result);
		}
	}
	
	/**
	 * 获取等待时间
	 */
	public int getReportwait(){
		int mintime = publicConf.DEV_REPORT_WAIT_MIN;
		int maxtime = publicConf.DEV_REPORT_WAIT_MAX;
		return mintime + (int)(Math.random()*(maxtime-mintime+1));
	}
}

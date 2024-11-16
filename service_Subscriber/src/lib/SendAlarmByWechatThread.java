package lib;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import conf.publicConf;

public class SendAlarmByWechatThread extends Thread{
	private static final Log log = LogFactory.getLog(SendAlarmByWechatThread.class);
	private Map<String,Object> device;
	private List<Map<String,String>> noticeUsers = new ArrayList<Map<String,String>>();
	private List<Map<String,String>> wechatNotices;
	private String lang;
	private Long time;
	
	public SendAlarmByWechatThread(Map<String,Object> device,Object noticeUsersObj,List<Map<String,String>>wechatNotices,String lang,Long time){
		this.device = device;
		this.wechatNotices = wechatNotices;
		this.lang = lang;
		this.time = time;
		JSONArray noticeUsersJson = JSON.parseArray(noticeUsersObj.toString());
		for(int i=0;i<noticeUsersJson.size();i++){
			this.noticeUsers.add(JSONObject.parseObject(noticeUsersJson.getJSONObject(i).toJSONString(), new TypeReference<Map<String,String>>(){}));
		}
	}
	
	@Override
	public void run(){
		log.debug("Send alarm notice by wechat");
		if(!this.noticeUsers.isEmpty()){
			String deviceName = this.device.get("name") == null || this.device.get("name").toString().isEmpty() ? this.device.get("dev_mac").toString() : this.device.get("dev_mac").toString() + "\\n"+this.device.get("name").toString();
			String datas = "";
			for(int j=0;j<this.noticeUsers.size();j++){
				Map<String,String> value = this.noticeUsers.get(j);
				String openid = value.get("open_id");
				if(!openid.isEmpty()){
					log.debug("Send alarm notice by wechat to openid["+openid+"]");
					datas += "{\"openid\":\"" + openid + "\","
							+ "\"lang\":\""+this.lang+"\","
							+ "\"devname\":\""+deviceName+"\","
							+"\"time\":\""+Helper.convUnixToZoneGm(String.valueOf(time),value.get("timeZone"),value.get("isSummerTime"))+"\","
							+"\"data\":"+JSON.toJSONString(this.wechatNotices)+"},";
				}
			}

			if(!datas.isEmpty()){
				datas = "{\"datas\":[" + datas.substring(0,datas.length()-1) + "]}";
				HttpRequest.sendPostJson(publicConf.cloudnetlot_api_base+"/backend/sendalarmwechat",datas);
			}
		}
	}
}
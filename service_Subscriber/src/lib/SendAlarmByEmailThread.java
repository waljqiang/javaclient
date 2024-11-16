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

public class SendAlarmByEmailThread extends Thread{
	private static final Log log = LogFactory.getLog(SendAlarmByEmailThread.class);
	private Map<String,Object> device;
	private List<Map<String,String>> noticeUsers = new ArrayList<Map<String,String>>();
	private List<Map<String,String>> emailNotices;
	private String lang;
	private Long time;
	
	public SendAlarmByEmailThread(Map<String,Object> device,Object noticeUsersObj,List<Map<String,String>>emailNotices,String lang,Long time){
		this.device = device;
		this.emailNotices = emailNotices;
		this.lang = lang;
		this.time = time;
		JSONArray noticeUsersJson = JSON.parseArray(noticeUsersObj.toString());
		for(int i=0;i<noticeUsersJson.size();i++){
			this.noticeUsers.add(JSONObject.parseObject(noticeUsersJson.getJSONObject(i).toJSONString(), new TypeReference<Map<String,String>>(){}));
		}
	}
	
	@Override
	public void run(){
		log.debug("Send alarm notice by email");
		String datas = "";
		String emails = "";
		if(!this.noticeUsers.isEmpty()){
			for(int i=0;i<this.noticeUsers.size();i++){
				Map<String,String> value = this.noticeUsers.get(i);
				String email = value.get("email");
				if(!email.isEmpty()){
					log.debug("Send alarm notice by email to email["+email+"]");
					emails += "\""+email+"\",";
				}
			}
		}
		if(!emails.isEmpty()){
			emails = emails.substring(0,emails.length()-1);
			String deviceName = this.device.get("name") == null || this.device.get("name").toString().isEmpty() ? this.device.get("dev_mac").toString() : this.device.get("dev_mac").toString() + "("+this.device.get("name").toString() + ")";
			datas = "{" + "\"emails\":[" + emails+"],"
					+"\"lang\":\"" + this.lang + "\","
					+"\"devname\":\""+deviceName+"\","
					+"\"data\":"+JSON.toJSONString(this.emailNotices)
					+"}";
			HttpRequest.sendPostJson(publicConf.cloudnetlot_api_base+"/backend/sendalarmemail",datas);
		}
	}
}
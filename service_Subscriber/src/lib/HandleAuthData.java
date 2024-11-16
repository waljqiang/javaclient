package lib;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import conf.deviceConf;
import conf.publicConf;
import conf.yunlotConf;
import lib.yuncorelot.Body;
import lib.yuncorelot.Header;
import lib.yuncorelot.Message;
import lib.yuncorelot.Yuncorelot;
import lib.yuncorelot.body.Auth;
import lib.yuncorelot.body.Child;
import lib.yuncorelot.body.Ichild;
import lib.yuncorelot.body.Wifi;
import lib.yuncorelot.body.WifiRadio;
import lib.yuncorelot.body.WifiRadioVap;
import redis.clients.jedis.Jedis;
import data.Device;

public class HandleAuthData{
	private static final Log log = LogFactory.getLog(HandleAuthData.class);
	//消息id
	private String mid;
	private String prtid;
	private String cltid;
	private String dev_gid;
	//设备MAC
	private String mac;
	private Map<String,Jedis> jedisResourse = new HashMap<String,Jedis>();
	private RedisCache redisCache;
	private Device deviceDB;
	
	private Integer pos = 0;
	
    public  HandleAuthData() {
    	for(int i =0;i<publicConf.handle_jedis_num;i++){
    		this.jedisResourse.put("jedis"+i,helperRedis.getJedis());
    	}
    	this.redisCache = new RedisCache();
    	this.deviceDB = new Device();
    }
    
    private Jedis getJedisResourse(){
    	Jedis jedis = null;
    	synchronized(this.pos){
           if(this.pos >= this.jedisResourse.size()){
        	   this.pos = 0;
           }
           jedis = this.jedisResourse.get("jedis"+this.pos);
           //轮询
           this.pos++;
        }
    	return jedis;
    }
    
    public void handle(String str){
 		try{
 			Message message = null;
 			Yuncorelot yuncorelotData = null;
 			Header yuncorelotDataHeader = null;
 			Body yuncorelotDataBody = null;
 			try{
	 			message = JSON.parseObject(str, new TypeReference<Message>(){});
	 			yuncorelotData = message.getData();
	 			yuncorelotDataHeader = yuncorelotData.getHeader();
	 			yuncorelotDataBody = yuncorelotData.getBody();
	 			this.mid = yuncorelotDataHeader.getMid();
	 			this.prtid = message.getPrtid();
	 			this.cltid = message.getCltid();
	 			this.mac = message.getMac();
 			}catch(Exception e){
 				throw new MyException("The format of the data is error",MyException.YUNLOT_UPINFO_ERROR);
 			}
 			Date ldate = new Date();
 			Long time = ldate.getTime()/1000;
	 		log.debug("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"]");
	 		//获取设备注册信息
	 		Map<String,Object> registerInfo = this.getRegisterInfo();
	 		try{
   		 		if(!yuncorelotDataHeader.getType().equals("1")){
   		 			throw new MyException("the type of the upinfo header is error",MyException.YUNLOT_UPINFO_ERROR);
   		 		}
   		 		String bind = yuncorelotDataHeader.getBind();
   		 		if(bind == null || bind.isEmpty()){
   		 			throw new MyException("The bind code["+bind+"] is error",MyException.BINDCODE_ERROR);
   		 		}else{
   		 			String bindInfo[] = Helper.parseBindCode(bind,Helper.parseMac(this.mac));//toUid,devMac,gid
		 			if(bindInfo[0] == null){
		 				throw new MyException("The bind code is error",MyException.BINDCODE_ERROR);
		 			}
		 			//数据校验
		 			this.checkData(registerInfo,bind,bindInfo[0]);
		 			this.dev_gid = registerInfo.get("gid").toString();
   		 			this.handleData(registerInfo,yuncorelotDataBody,yuncorelotData.getNow(), time, false);			
   		 		}
   		 		//调试模式下，回处理成功消息给设备
   				if(yuncorelotDataHeader.getDebug() != null && yuncorelotDataHeader.getDebug()){
   					Helper.sendMqtt(Helper.getTopic(this.prtid,this.cltid),"Handle the message["+this.mid+"] success");
   				}
   				log.debug("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"] success");
	 		}catch(MyException e){
	 			log.error("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"] failure,caused:"+"{\"errorCode\":\""+e.getCode()+"\",\"message\":\""+e.getMessage()+"\"}");
	 			if(e.getCode().equals(MyException.YUNLOT_REPORT_NO_BIND) && !registerInfo.get("dev_username").toString().isEmpty() && !registerInfo.get("weblogin_pwd").toString().isEmpty()){
	 				String command = "{\"type\":\"bind\",\"bind\":\""+registerInfo.get("bind").toString()+"\",\"token\":\""+Helper.generateDevToken(registerInfo.get("dev_username").toString(),registerInfo.get("weblogin_pwd").toString(),registerInfo.get("mac").toString(), time)+"\"";
					if(registerInfo.get("name") != null){
						command += ",\"name\":\""+registerInfo.get("name").toString()+"\"";
					}
					command += "}";
	 				String sendMessage = "{\"header\":{\"protocol\":\""+yunlotConf.VERSION+"\",\"type\":\""+yunlotConf.LOTTYPE_DOWN+"\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(6)+time+"\"},\"body\":{\"comm_id\":\""+Helper.getCommID(yunlotConf.LOTTYPE_DOWN,deviceConf.TYPE_INFO_BIND,time)+"\",\"command\":"+command+"},\"now\":\""+time+"\"}";
 					Helper.sendMqtt(Helper.getTopic(this.prtid,this.cltid), sendMessage);
	 			}else{
		 			//回错误消息给设备
		 			String sendMessage = "{\"header\":{\"protocol\":\""+yunlotConf.VERSION+"\",\"type\":\""+yunlotConf.LOTTYPE_DOWN+"\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(6)+time+"\"},\"body\":{\"comm_id\":\""+Helper.getCommID(yunlotConf.LOTTYPE_DOWN,deviceConf.TYPE_INFO_UPINFOFAIL,time)+"\",\"command\":{\"type\":\"upinfofail\",\"mid\":\""+this.mid+"\",\"errorCode\":\""+e.getCode()+"\",\"message\":\""+e.getMessage()+"\"}},\"now\":\""+time+"\"}";
		 			Helper.sendMqtt(Helper.getTopic(this.prtid,this.cltid), sendMessage);
	 			}
	 		}
 		}catch(NullPointerException e){
 			log.error("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"] failure,caused:Null is give up");
 		}catch(MyException e){
 			log.error("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"] failure,caused:{\"errorCode\":\""+e.getCode()+"\",\"message\":\""+e.getMessage()+"\"}");
 		}catch(Exception e){
 			log.error("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"] failure"+e.getMessage());
 		}
 		this.mid = null;
 		this.prtid = null;
 		this.cltid = null;
 		this.dev_gid = null;
    }
    
    private void checkData(Map<String,Object> registerInfo,String bind,String toUid) throws MyException{
    	if(registerInfo.isEmpty()){//未注册产品，不处理
    		throw new MyException("The product["+this.prtid+"] is not exists",MyException.PRT_NO);
    	}else{
    		if(!registerInfo.get("bind").toString().equals(bind)){
    			String bind_status = registerInfo.get("bind_status").toString();
    			if(bind_status.equals("0")){
    				throw new MyException("The device is not binded to cloudnetlot",MyException.CLOUD_NOT_BIND);
    			}else if(bind_status.equals("1")){
    				throw new MyException("Please report the data with no bind",MyException.YUNLOT_REPORT_NO_BIND);
    			}else{
    				throw new MyException("The bind code["+bind+"] is error",MyException.BINDCODE_ERROR);
    			}
    		}
    		if(!registerInfo.get("aud_status").toString().equals("4") && !registerInfo.get("developUid").toString().equals(toUid)){//未发布产品只能绑定到开发者账号下
    			throw new MyException("The product["+this.prtid+"] unpublished,just only bind to the develop",MyException.PRT_STATUS_NO_ALLOW);
    		}
    		if(registerInfo.get("cltid").toString().isEmpty()){//设备未连接过云平台，不能进行绑定
    			throw new MyException("The device of the product is not connect to cloudnetlot",MyException.DEV_NO_CONNECT);
    		}
    		if(!registerInfo.get("bindUid").toString().isEmpty() && !registerInfo.get("bindUid").toString().equals("0") && !registerInfo.get("bindUid").toString().equals(toUid)){//上报信息与绑定用户不符合
    			throw new MyException("The device is binded to another user",MyException.DEV_BINDED);
    		}
    	}
    }
    
    private Map<String,Object> getRegisterInfo() throws MyException{
    	 Map<String,Object> info = this.redisCache.getRegisterInfo(this.mac,this.getJedisResourse());
    	 if(info == null){//缓存没有查询数据库
    		 try{
    			 info = this.deviceDB.getRegisterInfoPool(this.mac);
    		 }catch(Exception e){
    			 throw new MyException(e.getMessage(),MyException.MYSQL_EXEC_ERROR);
    		 }
    	 }
    	 if(info.isEmpty()){
    		 throw new MyException("The device is not exists",MyException.DEVICE_NO);
    	 }
    	 this.redisCache.setRegisterInfo(this.mac,publicConf.cache_registerttl,info,this.getJedisResourse());
    	 return info;
    }
    
    private void handleData(Map<String,Object> registerInfo,Body data,String now,Long time,Boolean onlySysFlag)throws MyException{
    	if(data != null){
    		try{
	    		if(data.getChild() != null){
	    			if(!onlySysFlag){
						this.handleChild(registerInfo,data.getChild(),time,now);
					}
	    		}
	    		if(data.getAuth() != null){
	    			if(!onlySysFlag){
	    				this.handleAuth(registerInfo,data.getAuth(),time);
	    			}
	    		}
    		}catch(Exception e){
    			throw new MyException(e.getMessage(),MyException.DATA_HANDLE_FAILURE);
	    	}
    	}
    }
    
    //处理子设备数据
    private void handleChild(Map<String,Object> pdevice,Child value,Long time,String now) throws MyException{
    	log.debug("Start handle the child data of the device["+this.mac+"] for mid["+this.mid+"]");
    	List<Ichild> list = value.getList();
    	try{
    		Ichild child = new Ichild();
    		for(int i=0;i<list.size();i++){
    			child = list.get(i);
    			this.mac = child.getMac();
    			Map<String,Object> device = this.redisCache.getDeviceReport(this.mac,this.getJedisResourse());
    			if(device == null){
    				device = this.deviceDB.getDevicePool(this.mac);
    				if(!device.isEmpty()){
    					this.redisCache.setDeviceReport(this.mac,publicConf.cache_registerttl,device,this.getJedisResourse());
    				}else{
    					throw new MyException("The device is not exists",MyException.DEVICE_NO);
    				}
    			}
    			if(child.getAuth() != null){
	    			this.handleAuth(device,child.getAuth(),time);
	    		}
    			this.mac = pdevice.get("dev_mac").toString();
    		}
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	log.debug("Handle increment childs of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleAuth(Map<String,Object> deviceRegisterInfo,Auth value,Long time) throws MyException{
    	log.debug("Start handle the auth data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String radioid = value.getRadioid();
    	String vapid = value.getVapid();
    	String client_mac = value.getClient_mac();
    	String client_ip = value.getClient_ip();
    	String status = value.getStatus();
    	String token = value.getToken();
 
    	if(client_mac == null || status == null || !status.equals("2")){
    		throw new MyException("The auth data is error",MyException.YUNLOT_UPINFO_ERROR);
    	}
   	
    	if(radioid != null && !radioid.isEmpty()){//无线认证
    		if(vapid == null || vapid.isEmpty()){
    			throw new MyException("The auth data is error",MyException.YUNLOT_UPINFO_ERROR);
    		}
    		//无线是否存在
    		try{
	    		String wifiStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_WIFI,this.getJedisResourse());
	        	Wifi wifi = JSON.parseObject(wifiStr, new TypeReference<Wifi>(){});
	        	List<WifiRadio> radios = wifi.getRadios();
	        	Boolean wifiFlag = false;
	        	for(int i=0;i<radios.size();i++){
	        		if(radios.get(i).getRadioid().equals(radioid)){
	        			List<WifiRadioVap> vaps = radios.get(i).getVap();
	        			for(int j=0;j<vaps.size();j++){
	        				if(vaps.get(j).getId().equals(vapid)){
	        					wifiFlag = true;
	        					break;
	        				}
	        			}
	        			break;
	        		}
	        	}
	        	if(!wifiFlag){
	        		throw new MyException("The wifi is not exists",MyException.YUNLOT_UPINFO_ERROR);
	        	}
    		}catch(Exception e){
    			throw new MyException("The wifi is not exists",MyException.MYSQL_EXEC_ERROR);
    		}
    	}else{//有线认证
    		
    	}
    	log.debug("Handle ask allow");
		this.handleAllow(radioid,vapid,client_mac,client_ip,token,time);
    	log.debug("Handle the auth data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleAllow(String radioid,String vapid,String client_mac,String client_ip,String token,Long time) throws MyException{
    	String status = "0";//1放行
    	
    	Map<String,Object> deviceAuthInfo = this.redisCache.getDeviceAuthWithPolicy(this.mac,radioid,vapid,this.getJedisResourse());
    	if(deviceAuthInfo == null){
    		deviceAuthInfo = this.deviceDB.getDeviceAuthWithPolicy(this.mac,radioid,vapid);
    		if(!deviceAuthInfo.isEmpty()){
    			this.redisCache.setDeviceAuthWithPolicy(this.mac,radioid,vapid,2952000,deviceAuthInfo,this.getJedisResourse());
    		}
    	}

    	if(!deviceAuthInfo.isEmpty() && deviceAuthInfo.get("enable").toString().equals("1")){//开启认证配置了
    		if(this.dev_gid == null || this.dev_gid.isEmpty()){
    			throw new MyException("The device is no binded",MyException.DEVICE_NO);
    		}
    		Map<String,List<String>> roam = this.redisCache.getAuthAllowWithRoam(this.dev_gid,this.getJedisResourse());
    		if(roam.containsKey(this.mac)){//开启了同组漫游
    			for(Map.Entry<String,List<String>> entry : roam.entrySet()){
					if(entry.getValue().contains(client_mac)){
						//单个设备判断
						status = this.handleOneAllow(deviceAuthInfo,this.mac,radioid,vapid,client_mac,client_ip,token,time,false);
						break;
					}
				}    			
    		}else{//未开启同组漫游
    			//单个设备
    			status = this.handleOneAllow(deviceAuthInfo,this.mac,radioid,vapid,client_mac,client_ip,token,time,true);
    		}
    	}else{
    		status = "1";
    	}
    	String sendMessage = "{\"header\":{\"protocol\":\""+yunlotConf.VERSION+"\",\"type\":\""+yunlotConf.LOTTYPE_DOWN+"\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(6)+time+"\"},\"body\":{\"comm_id\":\""+Helper.getCommID(yunlotConf.LOTTYPE_DOWN,deviceConf.TYPE_INFO_AUTH_REMOTE,time)+"\",\"command\":{\"type\":\"set\",\"auth\":[{\"clientmac\":\""+client_mac+"\",";
    	if(!(radioid == null || radioid.isEmpty())){
    		sendMessage += "\"radioid\":\""+radioid+"\",";
    	}
    	if(!(vapid == null || vapid.isEmpty())){
    		sendMessage += "\"vapid\":\""+vapid+"\",";
    	}
    	if(!(client_ip == null || client_ip.isEmpty())){
    		sendMessage += "\"client_ip\":\""+client_ip+"\",";
    	}
    	sendMessage += "\"status\":\""+status+"\"}]}},\"now\":\""+time+"\"}";
		Helper.sendMqtt(Helper.getTopic(this.prtid,this.cltid), sendMessage);
    }
    
    //单个设备放行处理
    String handleOneAllow(Map<String,Object> deviceAuthInfo,String devMac,String radioid,String vapid,String client_mac,String client_ip,String token,Long time,Boolean isFind)throws MyException{
    	String status = "0";
    	Map<String,HashMap<String,HashMap<String,String>>>authInfo = this.redisCache.getAuthAllow(devMac,this.getJedisResourse());
    	if(isFind){
	    	String devAllow = this.redisCache.getAuthAllowStatus(authInfo,devMac,client_mac,radioid,vapid,this.getJedisResourse());
			if(!devAllow.equals("1")){
				status = "0";
			}else{
				status = "1";
			}
    	}else{
    		status = "1";
    	}
		if(token != null){
			String str1 = token.substring(0, 32);
        	String str2 = token.substring(32);
        	if(!DigestUtils.md5Hex(devMac+client_mac).equals(str1)){
        		throw new MyException("The token is invalid",MyException.TOKEN_INVALID);
        	}
        	Long expireTime = Long.parseLong(Helper.decodeUid(str2));
        	if(!deviceAuthInfo.get("expire_in").equals("0") && time >= expireTime){//认证过期
        		status = "0";
        		log.debug("The token is expired");
        	}
		}
		if(deviceAuthInfo.get("black_mac") != null){
			String black_mac = deviceAuthInfo.get("black_mac").toString();
    		if(black_mac.indexOf(client_mac) != -1){
    			status = "0";
    			log.debug("client_mac["+client_mac+"]in black_mac["+black_mac+"] of the devmac["+devMac+"]");
    		}
    	}
		//会员认证特殊
		HashMap<String,HashMap<String,String>> authClient = authInfo.get(client_mac);
		if(authClient != null && !authClient.isEmpty()){
			String authmethod = "";
			String mid = "";
			String authClientKey = this.redisCache.getAuthClientKey(radioid,vapid);
			HashMap<String,String> authClientInfo = authClient.get(authClientKey);
			if(authClientInfo != null && !authClientInfo.isEmpty()){
				authmethod = authClientInfo.get("authmethod");
				mid = authClientInfo.get("mid");
			}
			if(authmethod != null && authmethod.equals("2")){//会员认证
				if(mid == null || mid.isEmpty()){
					status = "0";
				}else{
					//查看会员是否存在
					Map<String,Object> member = this.redisCache.getAuthMember(mid,this.getJedisResourse());
					if(member == null || member.isEmpty()){
						member = this.deviceDB.getAuthMemberPool(mid);
						this.redisCache.setAuthMember(mid,JSON.toJSONString(member),this.getJedisResourse());
					}
					if(member == null || member.isEmpty()){
						status = "0";
					}else{
						if(member.get("pwd_change_time") != null){//密码修改了
							String pwdChange_time = member.get("pwd_change_time").toString();
							String loginTime = authClientInfo.get("login_time");
							if(loginTime != null && Integer.parseInt(pwdChange_time) > Integer.parseInt(loginTime)){
								status = "0";
							}
						}
					}
				}
			}
		}
		return status;
    }
     
}
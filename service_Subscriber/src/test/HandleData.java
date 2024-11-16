package test;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import conf.deviceConf;
import conf.publicConf;
import conf.redisConf;
import conf.yunlotConf;
import lib.DruidUtils;
import lib.Helper;
import lib.JdbcUtils;
import lib.MyException;
import lib.RedisCache;
import lib.SendAlarmByEmailThread;
import lib.SendAlarmByWechatThread;
import lib.helperRedis;
import lib.yuncorelot.Body;
import lib.yuncorelot.Header;
import lib.yuncorelot.Message;
import lib.yuncorelot.Yuncorelot;
import lib.yuncorelot.body.Alarm;
import lib.yuncorelot.body.AllChangeUp;
import lib.yuncorelot.body.Ask;
import lib.yuncorelot.body.Auth;
import lib.yuncorelot.body.Child;
import lib.yuncorelot.body.ClientConfig;
import lib.yuncorelot.body.CommResult;
import lib.yuncorelot.body.Dmz;
import lib.yuncorelot.body.FilterIpport;
import lib.yuncorelot.body.FilterMac;
import lib.yuncorelot.body.FilterUrl;
import lib.yuncorelot.body.Ichild;
import lib.yuncorelot.body.IpGroup;
import lib.yuncorelot.body.IpportMap;
import lib.yuncorelot.body.Network;
import lib.yuncorelot.body.RateLimit;
import lib.yuncorelot.body.TimeGroup;
import lib.yuncorelot.body.TimeReboot;
import lib.yuncorelot.body.Upgrade;
import lib.yuncorelot.body.User;
import lib.yuncorelot.body.Wifi;
import lib.yuncorelot.body.WifiRadio;
import lib.yuncorelot.body.WifiRadioVap;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import data.Device;

public class HandleData{
	private static final Log log = LogFactory.getLog(HandleData.class);
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
	
    public  HandleData() {
    	for(int i =0;i<5;i++){
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
    
    public void handle(Message message){
 		try{
 			Yuncorelot yuncorelotData = null;
 			Header yuncorelotDataHeader = null;
 			Body yuncorelotDataBody = null;
 			try{
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
   		 		if(bind == null || bind.isEmpty()){//无绑定码
   		 			if(registerInfo.get("bind").toString().isEmpty()){//首次上报处理，处理系统信息
	 					this.handleData("0","0",yuncorelotDataBody,yuncorelotData.getNow(),time,true);
   		 			}else if(yuncorelotDataBody.getComm_result() != null){
   		 				this.handleCommResult(null,"0",yuncorelotDataBody.getComm_result(),time);
   		 			}else{//设备已绑定,发绑定码给绑定用户
   		 				//不处理数据,发送绑定命令给设备
   		 				if(registerInfo.get("bind_status").toString().equals("1") || registerInfo.get("bind_status").toString().equals("2")){
   		 					if(registerInfo.get("dev_username").toString().isEmpty() || registerInfo.get("weblogin_pwd").toString().isEmpty()){
   		 						throw new MyException("The device["+this.mac+"] is not binded to cloudnetlot",MyException.CLOUD_NOT_BIND);
   		 					}
   		 					if(yuncorelotDataBody.getSystem() != null){
	   		 					String sendMessage = "{\"header\":{\"protocol\":\""+yunlotConf.VERSION+"\",\"type\":\""+yunlotConf.LOTTYPE_DOWN+"\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(6)+time+"\"},\"body\":{\"comm_id\":\""+Helper.getCommID(yunlotConf.LOTTYPE_DOWN,deviceConf.TYPE_INFO_BIND,time)+"\",\"command\":{\"type\":\"bind\",\"bind\":\""+registerInfo.get("bind").toString()+"\",\"token\":\""+Helper.generateDevToken(registerInfo.get("dev_username").toString(),registerInfo.get("weblogin_pwd").toString(),registerInfo.get("mac").toString(), time)+"\"}},\"now\":\""+time+"\"}";
	   		 					Helper.sendMqtt(Helper.getTopic(this.prtid,this.cltid), sendMessage);
   		 					}
   		 				}
   		 			}
   		 		}else{//有绑定码
   		 			String bindInfo[] = Helper.parseBindCode(bind,Helper.parseMac(this.mac));//toUid,devMac,gid
   		 			if(bindInfo[0] == null){
   		 				throw new MyException("The bind code is error",MyException.BINDCODE_ERROR);
   		 			}
   		 			//数据校验
   		 			this.checkData(registerInfo,bind,bindInfo[0]);
   		 			this.dev_gid = registerInfo.get("gid").toString();
   		 			this.handleData(bindInfo[0], bindInfo[2],yuncorelotDataBody,yuncorelotData.getNow(), time, false);
   		 		}
   		 		//调试模式下，回处理成功消息给设备
   				if(yuncorelotDataHeader.getDebug() != null && yuncorelotDataHeader.getDebug()){
   					Helper.sendMqtt(Helper.getTopic(this.prtid,this.cltid),"Handle the message["+this.mid+"] success");
   				}
   				log.debug("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"] success");
	 		}catch(MyException e){
	 			log.error("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"] failure,caused:"+"{\"errorCode\":\""+e.getCode()+"\",\"message\":\""+e.getMessage()+"\"}");
	 			if(e.getCode().equals(MyException.YUNLOT_REPORT_NO_BIND) && !registerInfo.get("dev_username").toString().isEmpty() && !registerInfo.get("weblogin_pwd").toString().isEmpty()){
	 				String sendMessage = "{\"header\":{\"protocol\":\""+yunlotConf.VERSION+"\",\"type\":\""+yunlotConf.LOTTYPE_DOWN+"\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(6)+time+"\"},\"body\":{\"comm_id\":\""+Helper.getCommID(yunlotConf.LOTTYPE_DOWN,deviceConf.TYPE_INFO_BIND,time)+"\",\"command\":{\"type\":\"bind\",\"bind\":\""+registerInfo.get("bind").toString()+"\",\"token\":\""+Helper.generateDevToken(registerInfo.get("dev_username").toString(),registerInfo.get("weblogin_pwd").toString(),registerInfo.get("mac").toString(), time)+"\"}},\"now\":\""+time+"\"}";
 					Helper.sendMqtt(Helper.getTopic(this.prtid,this.cltid), sendMessage);
	 			}else{
		 			//回错误消息给设备
		 			String sendMessage = "{\"header\":{\"protocol\":\""+yunlotConf.VERSION+"\",\"type\":\""+yunlotConf.LOTTYPE_DOWN+"\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(6)+time+"\"},\"body\":{\"comm_id\":\""+Helper.getCommID(yunlotConf.LOTTYPE_DOWN,deviceConf.TYPE_INFO_UPINFOFAIL,time)+"\",\"command\":{\"type\":\"upinfofail\",\"mid\":\""+this.mid+"\",\"errorCode\":\""+e.getCode()+"\",\"message\":\""+e.getMessage()+"\"}},\"now\":\""+time+"\"}";
		 			Helper.sendMqtt(Helper.getTopic(this.prtid,this.cltid), sendMessage);
	 			}
	 		}
 		}catch(NullPointerException e){
 			log.debug("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"] failure,caused:Null is give up");
 		}catch(MyException e){
 			log.debug("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"] failure,caused:{\"errorCode\":\""+e.getCode()+"\",\"message\":\""+e.getMessage()+"\"}");
 		}catch(Exception e){
 			log.debug("Handle the data of the device["+this.mac+"] for the mid["+this.mid+"] failure,caused:"+e.getMessage());
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
    				throw new MyException("The device["+this.mac+"] is not binded to cloudnetlot",MyException.CLOUD_NOT_BIND);
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
    		 if(info.isEmpty()){
        		 throw new MyException("The device is not exists",MyException.DEVICE_NO);
        	 }
        	 this.redisCache.setRegisterInfo(this.mac,publicConf.cache_registerttl,info,this.getJedisResourse());
    	 }
    	 return info;
    }
    
    private void handleData(String toUid,String gid,Body data,String now,Long time,Boolean onlySysFlag)throws MyException{
    	if(data != null){
    		//ntp校准
    		if(Math.abs(time - Long.parseLong(now)) > deviceConf.NTP_EXPIRE){
    			String commID = Helper.getCommID(yunlotConf.LOTTYPE_DOWN,deviceConf.TYPE_INFO_NTP,time);
    	    	String message = "{\"header\":{\"protocol\":\""+yunlotConf.VERSION+"\",\"type\":\""+yunlotConf.LOTTYPE_DOWN+"\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(6)+time+"\"},\"body\":{\"comm_id\":\""+commID+"\",\"command\":{\"type\":\"ntp\",\"time\":\""+time+"\"}},\"now\":\""+time+"\"}";
    			Helper.sendMqtt(Helper.getTopic(prtid,cltid), message);
    		}
    		try{
    			Map<String,Object> device = this.redisCache.getDeviceReport(this.mac,this.getJedisResourse());
    			if(device == null){
    				device = this.deviceDB.getDevicePool(this.mac);
    				if(!device.isEmpty()){
    					this.redisCache.setDeviceReport(this.mac,publicConf.cache_registerttl,device,this.getJedisResourse());
    				}else{
    					device = null;
    				}
    			}
	    		//设备状态更新
    			Map<String,Object> oldCache = this.redisCache.getDeviceDynamic(this.mac,getJedisResourse());
	    		if(!onlySysFlag){
	    			Map<String,String>newDynamic = new HashMap<String,String>();
	    			String oldStatus = oldCache.get("status").toString();
	    			if(data.getSystem() != null && (oldCache == null || oldStatus.equals(deviceConf.STATUS_OFFLINE))){
	    				newDynamic.put("status",deviceConf.STATUS_ONLINE);
		    			newDynamic.put("online_time",Long.toString(time));
	    			}
	    			if(oldCache.get("offline_time") != null && oldCache.get("alarm_app") != null){
						Long offlineTime = Long.parseLong(oldCache.get("offline_time").toString());
						if(time - offlineTime > publicConf.alarm_recovery_time){
							newDynamic.put("alarm_app","0");
						}
					}
	    			this.redisCache.parseDynamic(this.mac,newDynamic,this.getJedisResourse());
	    		}
	    		
/*	    		if(!onlySysFlag && data.getSystem() != null && (oldStatus == null || oldStatus.equals(deviceConf.STATUS_OFFLINE))){
	    			Map<String,Object>newDynamic = new HashMap<String,Object>();
	    			newDynamic.put("status",deviceConf.STATUS_ONLINE);
	    			newDynamic.put("online_time",Long.toString(time));
	    			this.redisCache.setDeviceDynamic(this.mac,newDynamic,this.getJedisResourse());
	    		}*/
	    		//数据处理
	    		if(data.getSystem() != null){
	    			this.handleSystem(device,toUid,gid,data.getSystem(),time,"");
	    		}
	    		if(data.getNetwork() != null){
	    			if(!onlySysFlag){
						this.handleNetwork(device,data.getNetwork(),time);
					}
	    		}
	    		if(data.getWifi() != null){
	    			if(!onlySysFlag){
						this.handleWifi(device,data.getWifi(),time);
					}
	    		}
	    		if(data.getUser() != null){
	    			if(!onlySysFlag){
						this.handleUser(device,data.getUser(),time,now);
					}
	    		}
	    		if(data.getTime_reboot() != null){
	    			if(!onlySysFlag){
						this.handleTimereboot(device,data.getTime_reboot(),time);
					}
	    		}
	    		if(data.getChild() != null){
	    			if(!onlySysFlag){
						this.handleChild(device,toUid,gid,data.getChild(),time,now);
					}
	    		}
	    		if(data.getComm_result() != null){
	    			if(!onlySysFlag){
						this.handleCommResult(device,toUid,data.getComm_result(),time);
					}
	    		}
	    		if(data.getUpgrade() != null){
	    			if(!onlySysFlag){
						this.handleUpgrade(device,data.getUpgrade(),time);
					}
	    		}
	    		if(data.getAsk() != null){
	    			if(!onlySysFlag){
	    				this.handleAsk(device,data.getAsk(),time);
	    			}
	    		}
	    		if(data.getAlarm() != null){
	    			if(!onlySysFlag){
	    				this.handleAlarm(device,data.getAlarm(),time);
	    			}
	    		}
	    		if(data.getAuth() != null){
	    			if(!onlySysFlag){
	    				this.handleAuth(device,data.getAuth(),time);
	    			}
	    		}
	    		if(data.getTime_group() != null){
	    			if(!onlySysFlag){
	    				this.handleTimegroup(device,data.getTime_group(),time);
	    			}
	    		}
	    		if(data.getIp_group() != null){
	    			if(!onlySysFlag){
	    				this.handleIpgroup(device,data.getIp_group(),time);
	    			}
	    		}
	    		if(data.getRate_limit() != null){
	    			if(!onlySysFlag){
	    				this.handleRateLimit(device,data.getRate_limit(),time);
	    			}
	    		}
	    		if(data.getFilter_url() != null){
	    			if(!onlySysFlag){
	    				this.handleFilterUrl(device,data.getFilter_url(),time);
	    			}
	    		}
	    		if(data.getFilter_iport() != null){
	    			if(!onlySysFlag){
	    				this.handleFilterIpport(device,data.getFilter_iport(),time);
	    			}
	    		}
	    		if(data.getFilter_mac() != null){
	    			if(!onlySysFlag){
	    				this.handleFilterMac(device,data.getFilter_mac(),time);
	    			}
	    		}
	    		if(data.getIpport_map() != null){
	    			if(!onlySysFlag){
	    				this.handleIpportMap(device,data.getIpport_map(),time);
	    			}
	    		}
	    		if(data.getDmz() != null){
	    			if(!onlySysFlag){
	    				this.handleDmz(device,data.getDmz(),time);
	    			}
	    		}
	    		if(data.getClient_config() != null){
	    			if(!onlySysFlag){
	    				this.handleClientConfig(device,data.getClient_config(),time);
	    			}
	    		}
    		}catch(Exception e){
    			throw new MyException(e.getMessage(),MyException.DATA_HANDLE_FAILURE);
	    	}
    	}
    }
    //系统数据处理
    private void handleSystem(Map<String,Object> device,String toUid,String gid,lib.yuncorelot.body.System value,Long time,String pmac)throws MyException{
    	log.debug("Start handle the system data of the device["+this.mac+"] for mid["+this.mid+"]");
    	if(!this.mac.equals(value.getMac())){
    		throw new MyException("The mac of the device is error",MyException.YUNLOT_UPINFO_ERROR);
    	}
    	String mac = (device == null || device.isEmpty()) ? value.getMac() : device.get("dev_mac").toString();

    	try{
    		Map<String,String> addData = new HashMap<String,String>();//增加数据
    		Map<String,String> updateData = new HashMap<String,String>();//更新数据
    		String parent = null;//拓扑关系
    		//兼容设备未上报频繁上下线能力
    		List<String> ability = value.getAbility();
    		if(!ability.contains("000D0001")){
    			ability.add("000D0001");
    			value.setAbility(ability);
    		}
    		Boolean upgradeFlag = false;
    		if(device == null || device.isEmpty()){
    			addData = this.getDeviceAddData(mac,pmac,toUid,gid,value,time);
    			if(pmac.isEmpty()){
    				if(value.getParent() != null && value.getParent().get("mac") != null){
    					parent = this.getParentMac(value.getParent().get("mac").toString());
    				}
    			}else{
    				parent = pmac;
    			}
    			//如果没有设置位置信息,则需要ip进行位置定位
        		if(value.getLocation() == null && value.getNet_ip() != null){
        			this.redisCache.addLocation("{\"mac\":\""+mac+"\",\"net_ip\":\""+value.getNet_ip()+"\"}",this.getJedisResourse());
        		}
        		upgradeFlag = true;
    		}else{
    			updateData = this.getDeviceUpdateData(device,mac,pmac,toUid,gid,value,time);
    			if(pmac.isEmpty()){
    				if(value.getParent() != null && value.getParent().get("mac") != null){
    					parent = this.getParentMac(value.getParent().get("mac").toString());
    				}else if(device.get("rid").toString().isEmpty()){
    					parent = "";
    				}
    			}else{
    				if(this.isDifferentNoNull(pmac, device.get("pid").toString())){
    					parent = pmac;
    				}
    			}
    			//如果没有设置位置信息,则需要ip进行位置定位
        		if(value.getLocation() == null && value.getNet_ip() != null && this.isDifferentNoNull(value.getNet_ip(), device.get("net_ip").toString())){
        			this.redisCache.addLocation("{\"mac\":\""+mac+"\",\"net_ip\":\""+value.getNet_ip()+"\"}",this.getJedisResourse());
        		}
        		if(device.get("newversion") == null || device.get("newversion").toString().isEmpty()){
        			upgradeFlag = true;
        		}
    		}
    		Boolean rs1 = true;
			Boolean rs2 = true;
			Boolean rs3 = true;
    		if(!addData.isEmpty() || !updateData.isEmpty() || parent != null || upgradeFlag){//数据库操作
    			JdbcUtils jdbcUtils = new JdbcUtils();
    			jdbcUtils.getConnection();
    			jdbcUtils.setAutoCommit(false);
    			//在线升级处理
    			if(upgradeFlag){
    				String type = "";
    				if(!value.getType().isEmpty()){
    					type = value.getType();
    				}else if(!device.get("type").toString().isEmpty() && (device.get("package_fid") == null || device.get("package_fid").toString().isEmpty())){
    					type = device.get("type").toString();
    				}
    				if(!type.isEmpty()){
	    				Map<String,Object> newPackage = this.redisCache.getCloudLatestPackageByType(type,this.getJedisResourse());
	    				if(newPackage == null){
	    					newPackage = this.deviceDB.getCloudLatestPackageByType(type,jdbcUtils);
	    					this.redisCache.setCloudLatestPackageByType(type,newPackage,this.getJedisResourse());
	    				}
	    				if(newPackage != null && newPackage.get("version") != null && newPackage.get("fid") != null){
	    					if(!addData.isEmpty()){
	    						addData.put("newVersion",newPackage.get("version").toString());
	    						addData.put("package_fid", newPackage.get("fid").toString());
	    					}
	    					if(!updateData.isEmpty() || upgradeFlag){
	    						updateData.put("newVersion",newPackage.get("version").toString());
	    						updateData.put("package_fid",newPackage.get("fid").toString());
	    					}
	    				}
    				}
    			}
    			if(!addData.isEmpty()){
    				rs1 = this.deviceDB.addDevice(addData,jdbcUtils);
    			}
    			if(!updateData.isEmpty()){
    				rs2 = this.deviceDB.updateDevice(mac,updateData,jdbcUtils);
    			}
    			if(parent != null && !toUid.equals("0")){
    				rs3 = this.deviceDB.toRelation(toUid,mac,parent,time,jdbcUtils);
    			}
    			if(!rs1 || !rs2 || !rs3){
    	    		jdbcUtils.rollback();
    	    		log.error("sql exec failure:rs1["+rs1+"]rs2["+rs2+"]rs3["+rs3+"]");
    	 			throw new MyException("Handle the system data of the device["+this.mac+"] for mid["+this.mid+"] failure",MyException.DATA_HANDLE_FAILURE);
    			}else{
    				jdbcUtils.commit();
    				//设备信息有变化,删除缓存report
    				this.redisCache.delete(redisConf.DEVICE_REPORT+this.mac,this.getJedisResourse());
    			}
    			jdbcUtils.setAutoCommit(true);
    			jdbcUtils.releaseConn();
    		}
    		
			//更新设备CPU使用率、内存使用率、运行时间
    		Map<String,String> dynamic = new HashMap<String,String>();
    		if(value.getStatus() != null){
    			String status = value.getStatus();
        		dynamic.put("status",status);
    		}
    		
    		if(value.getCpu_use() != null){
    			dynamic.put("cpu_use",value.getCpu_use());
    		}
    		if(value.getMemory_use() != null){
    			dynamic.put("memory_use", value.getMemory_use());
    		}
    		if(value.getRuntime() != null){
    			dynamic.put("runtime", value.getRuntime());
    		}
    		if(value.getAbility() != null){
    			dynamic.put("ability",JSON.toJSONString(value.getAbility()));
    		}
    		if(value.getParent() != null){
    			if(value.getParent().get("mac") != null){
    				dynamic.put("parent",this.getParentMac(value.getParent().get("mac").toString()));
    			}
    			if(value.getParent().get("link") != null){
    				dynamic.put("link",value.getParent().get("link").toString());
    			}
    			if(value.getParent().get("rssi") != null){
    				dynamic.put("rssi",value.getParent().get("rssi").toString());
    			}
    		}
    		if(value.getAlarm() != null){
    			dynamic.put("alarm_dev", value.getAlarm());
    		}
    		this.redisCache.parseDynamic(mac,dynamic, time,this.getJedisResourse());
    		this.redisCache.setDeviceParams(mac,deviceConf.TYPE_INFO_SYSTEM, value.toString(),this.getJedisResourse());
    		if(value.getEth() != null){
    			this.redisCache.handleEth(this.mac,value.getEth(),this.getJedisResourse());
    		}
    		//工作组中设备数量变化统计队列
    		if(addData.get("group_id") != null || updateData.get("group_id") != null){
    			this.redisCache.addGroupQueue("{\"gid\":\""+gid+"\",\"increase\":\"-1\"}",this.getJedisResourse());
    		}
    		log.debug("Handle the system data of the device["+this.mac+"] for mid["+this.mid+"] done");
    	}catch(Exception e){
 			throw new MyException("Handle the system data of the device["+this.mac+"] for mid["+this.mid+"] failure,caused:"+e.getMessage(),MyException.DATA_HANDLE_FAILURE);
    	}
    }
    
    //处理网络数据
    private void handleNetwork(Map<String,Object> device,Network value,Long time) throws MyException{
    	log.debug("Start handle the network data of the device["+this.mac+"] for mid["+this.mid+"]");
    	this.redisCache.setDeviceParams(this.mac,deviceConf.TYPE_INFO_NETWORK,value.toString(),this.getJedisResourse());
    	log.debug("Handle the network data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    //处理无线数据
    private void handleWifi(Map<String,Object> device,Wifi value,Long time) throws MyException{
    	log.debug("Start handle the wifi data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String saveStr = value.toString();
    	String baseWifiStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_WIFI,this.getJedisResourse());
    	if(baseWifiStr != null){
    		List<WifiRadio> wifiRadios = value.getRadios();
    		if(Integer.parseInt(value.getTotal()) != wifiRadios.size()){
    			throw new MyException("The wifi data is error",MyException.YUNLOT_UPINFO_ERROR);
    		}
			Wifi baseWifi = JSON.parseObject(baseWifiStr, new TypeReference<Wifi>(){});
    		List<WifiRadio> baseWifiRadios = baseWifi.getRadios();

    		//radio处理
    		if(wifiRadios.size() > 0){
    			for(int i=0;i<wifiRadios.size();i++){
    				WifiRadio wifiRadio = wifiRadios.get(i);
    				for(int j=0;j<baseWifiRadios.size();j++){
    					WifiRadio baseWifiRadio = baseWifiRadios.get(j);
    					if(baseWifiRadio.getRadioid().equals(wifiRadio.getRadioid())){//找到对应的radio
    						String[] radioProperty = {"Country_code","Total","Radio_type","Channel","Channel_config","Power","Phymode","Coveragethreshold","User_isolate","Frag_threshold","Rts_threshold","Beacon_interval","Shortgi","Max_sta","Dfs","Distance"};
    						for(String x:radioProperty){
    							try{
	    							Object tmp = wifiRadio.getClass().getMethod("get"+x).invoke(wifiRadio);
	    							if(tmp != null){
	    								baseWifiRadios.get(j).getClass().getMethod("set"+x,String.class).invoke(baseWifiRadios.get(j),tmp.toString());
	    							}
    							}catch(Exception e){
    								log.error(e.getMessage());
    							}
    						}
    						if(wifiRadio.getSupport() != null){
    							baseWifiRadios.get(j).setSupport(wifiRadio.getSupport());
    						}
    						
    						if(wifiRadio.getVap() != null){//需要修改vap
    							for(int m=0;m<wifiRadio.getVap().size();m++){
    								WifiRadioVap wifiRadioVap = wifiRadio.getVap().get(m);
    								for(int n=0;n<baseWifiRadio.getVap().size();n++){
    									WifiRadioVap baseWifiRadioVap = baseWifiRadio.getVap().get(n);
    									if(baseWifiRadioVap.getId().equals(wifiRadioVap.getId())){//找到vap
    										String[] vapProperty = {"Id","Enable","Vlan_id","Bssid","Ssid","Ssid_hide","Encode","Password"};
    										for(String y:vapProperty){
    											try{
    				    							Object tmp = wifiRadioVap.getClass().getMethod("get"+y).invoke(wifiRadioVap);
    				    							if(tmp != null){
    				    								baseWifiRadios.get(j).getVap().get(n).getClass().getMethod("set"+y,String.class).invoke(baseWifiRadios.get(j).getVap().get(n),tmp.toString());
    				    							}
    			    							}catch(Exception e){
    			    								log.error(e.getMessage());
    			    							}
    										}
    										//用户列表处理
    										if(wifiRadioVap.getUsers().getList() != null){//全量上报，直接替换
    											baseWifiRadios.get(j).getVap().get(n).setUsers(wifiRadioVap.getUsers());
    										}else{
    											if(wifiRadioVap.getUsers().getChange() != null){//增加或修改
    												for(int p=0;p<wifiRadioVap.getUsers().getChange().size();p++){
    													Map<String,String> wifiRadioVapUser = wifiRadioVap.getUsers().getChange().get(p);
    													int index = Helper.findListMap(baseWifiRadioVap.getUsers().getList(),wifiRadioVapUser,"mac");
    													if(index == -1){//没找到
    														baseWifiRadios.get(j).getVap().get(n).getUsers().getList().add(wifiRadioVapUser);
    													}else{
    														baseWifiRadios.get(j).getVap().get(n).getUsers().getList().get(index).putAll(wifiRadioVapUser);
    													}
    												}
    											}
    											if(wifiRadioVap.getUsers().getDelete() != null){//删除
    												for(int r=0;r<wifiRadioVap.getUsers().getDelete().size();r++){
    													String wifiRadioVapUser = wifiRadioVap.getUsers().getDelete().get(r);
    													int dindex = Helper.findListMap(baseWifiRadioVap.getUsers().getList(),wifiRadioVapUser,"mac");
    													if(dindex != -1){
    														baseWifiRadios.get(j).getVap().get(n).getUsers().getList().remove(dindex);
    													}
    												}
    											}
    											baseWifiRadios.get(j).getVap().get(n).getUsers().setTotal(String.valueOf(baseWifiRadios.get(j).getVap().get(n).getUsers().getList().size()));
    										}
    										//中继client处理
    										if(wifiRadio.getClient() != null){
    											baseWifiRadios.get(j).setClient(wifiRadio.getClient());
    											//其他中继信息清空
    											for(int q=0;q<baseWifiRadios.size();q++){
    												if(q != j){
    													baseWifiRadios.get(q).setClient(null);
    												}
    											}
    										}
    									}
    								}
    							}
    						}
    					}
    				}
    			}
    			baseWifi.setTotal(String.valueOf(baseWifiRadios.size()));
    			baseWifi.setRadios(baseWifiRadios);
    		}
    		//定时重启处理
    		if(value.getTimer() != null){
    			baseWifi.setTimer(value.getTimer());
    		}
    		saveStr = baseWifi.toString();
    	}
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_WIFI, saveStr,this.getJedisResourse());
    	log.debug("Handle the wifi data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    //处理用户数据
    private void handleUser(Map<String,Object> device,User value,Long time,String now) throws MyException{
    	log.debug("Start handle the user data of the device["+this.mac+"] for mid["+this.mid+"]");
    	User newUsers = new User();
    	List<Map<String,String>> newUsersList = new ArrayList<Map<String,String>>();
    	List<Map<String,String>> oldUsersList = new ArrayList<Map<String,String>>();
    	String oldUsersStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_USER,this.getJedisResourse());
    	if(oldUsersStr != null){
	    	User oldUsers = JSON.parseObject(oldUsersStr, new TypeReference<User>(){});
	    	oldUsersList = oldUsers.getList();
    	}
 	
    	String total = value.getTotal();
    	List<Map<String,String>> list = value.getList();
    	
    	if(total != null && list != null){//全量式上报
    		if(list.size() > publicConf.up_number){
    			throw new MyException("Too many users",MyException.YUNLOT_UPINFO_ERROR);
    		}
    		if(value.getIndex().equals("0")){
    			newUsersList = list;
    		}else{
    			if(oldUsersList != null)
    				newUsersList.addAll(oldUsersList);
    			newUsersList.addAll(list);
    		}
    		//如果是最后一页，处理完数据删除旧数据缓存
    		if(newUsersList.size() >= Integer.parseInt(total)){
    			this.redisCache.delete(redisConf.OLDUSERS + this.mac,this.getJedisResourse());
    		}
    	}else{//增量上报
    		if(value.getChange() != null){
    			newUsersList = Helper.mergeList(oldUsersList,value.getChange(),"mac");
			}
			if(value.getDelete() != null){
				newUsersList = Helper.deletesList(oldUsersList,value.getDelete(),"mac");
			}
    	}
    	newUsers.setTotal(String.valueOf(newUsersList.size()));
    	newUsers.setList(newUsersList);
    	//写参数
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_USER, newUsers.toString(), this.getJedisResourse());
    	//写用户统计表
    	if(device != null){
    		if(!newUsersList.isEmpty()){
    			this.redisCache.addClientNums(this.getJedisResourse(),"{\"mac\":\""+this.mac+"\",\"uid\":\""+device.get("user_id").toString()+"\",\"onlines\":\""+newUsers.getTotal()+"\",\"created_at\":\""+now+"\"}");
    		}
    	}
    	log.debug("Handle the user data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    //处理定时重启数据
    private void handleTimereboot(Map<String,Object> device,TimeReboot value,Long time) throws MyException{
    	log.debug("Start handle the time_reboot data of the device["+this.mac+"] for mid["+this.mid+"]");
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_TIME_REBOOT, value.toString(),this.getJedisResourse());
    	log.debug("Handle the time_reboot data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    //处理子设备数据
    private void handleChild(Map<String,Object> pdevice,String toUid,String gid,Child value,Long time,String now) throws MyException{
    	log.debug("Start handle the child data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String total = value.getTotal();
    	List<Ichild> list = value.getList();
    	if(pdevice.get("group_id") != null && !pdevice.get("group_id").toString().equals("0")){
    		gid = pdevice.get("group_id").toString();
    	}
    	
    	if(total != null && list != null){//全量式上报
    		log.debug("Start handle all childs of the device[" + this.mac + "] for mid["+this.mid+"]");
    		if(list.size() > publicConf.up_number){
    			throw new MyException("Too many users",MyException.YUNLOT_UPINFO_ERROR);
    		}
    		
    		int handled = this.redisCache.getHandledChilds(this.mac,this.getJedisResourse());//已处理数
    		this.handleAllChildData(list,toUid,gid,time,pdevice,now);
    		this.redisCache.setHandledChilds(this.mac,handled+list.size(),600,this.getJedisResourse());
    		if(handled+list.size() >= Integer.parseInt(total)){//最后一页处理完成，删除旧数据缓存
    			String childMacsStr = this.redisCache.get(redisConf.OLDCHILDSLIST + this.mac,this.getJedisResourse());
    			if(childMacsStr != null){
    				List<String> childMacs = JSON.parseObject(childMacsStr, new TypeReference<List<String>>(){});
    				String[] keys = new String[childMacs.size()+1];
    				for(int i=0;i<childMacs.size();i++){
    					keys[i] = redisConf.OLDCHILDS + childMacs.get(i);
    				}
    				keys[childMacs.size()] = redisConf.OLDCHILDSLIST + this.mac;
    				this.redisCache.delete(keys,this.getJedisResourse());
    				this.redisCache.delHandledChilds(this.mac,this.getJedisResourse());
    			}
    		}
    		log.debug("Handle all childs of the device[" + this.mac + "] for mid["+this.mid+"] done");
    	}else{
    		log.debug("Start handle increment childs of the device[" + this.mac + "] for mid["+this.mid+"]");
    		if(value.getChange() != null){
    			this.handleChildData(value.getChange(), toUid, gid, time, pdevice,now);
    		}
    		List<String> childDeletes = value.getDelete();
    		if(childDeletes != null){
    			List<String> macs = new ArrayList<String>();
    			String[] keys = new String[childDeletes.size()];
    			for(int i=0;i<childDeletes.size();i++){
    				macs.add(childDeletes.get(i));
    				keys[i] = redisConf.DEVICE_PARAMS+childDeletes.get(i);
    			}

    			if(!(this.deviceDB.deleteChildsByMacsPool(macs))){
    				log.error("Delete child device failure");
    	 			throw new MyException("Handle the all childs of the device["+this.mac+"] for mid["+this.mid+"] failure",MyException.DATA_HANDLE_FAILURE);
    			}
    			//删除设备参数
    			this.redisCache.delete(keys,this.getJedisResourse());
    		}
    		log.debug("Handle increment childs of the device["+this.mac+"] for mid["+this.mid+"] done");
    	}
    }
    
    //处理命令结果
    private void handleCommResult(Map<String,Object> device,String toUid,CommResult value,Long time) throws MyException{
    	log.debug("Start handle the comm_result data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String cid = value.getCommid();
    	String status = value.getStatus();
    	String upStatus = "";
    	if(status.equals("0")){
			upStatus = "3";
		}else if(status.equals("1")){
			upStatus = "4";
		}
    	if(!upStatus.isEmpty()){
    		Map<String,Object> command = this.deviceDB.getCommandByCidPool(cid);
    		Boolean rs1 = true;
    		if(Helper.hexdec(cid.substring(4,8)).toString().equals(deviceConf.TYPE_INFO_BIND) && status.equals("0")){//绑定失败清除绑定信息及缓存信息
    			String[] cacheKeys = new String[3];
				cacheKeys[0] = redisConf.REGISTER + this.mac;
				cacheKeys[1] = redisConf.DEVICE_REPORT + this.mac;
				cacheKeys[2] = redisConf.DEVICE_DYNAMIC + this.mac;
				this.redisCache.delete(cacheKeys,this.getJedisResourse());
				log.debug("bind failure hand device["+this.mac+"] status to offline");
    		}
    		if(command.isEmpty()){
    			log.debug("The comm_result data of the device["+this.mac+"] for mid["+this.mid+"] ignore");
    		}else{
	    		rs1 = this.deviceDB.saveCommandStatusPool(cid,this.mac,upStatus,time);
	    		if(rs1){
	    			//清除注册信息
					this.redisCache.delete(redisConf.REGISTER+this.mac,this.getJedisResourse());
					if(toUid.equals("0")){
						toUid = command.get("user_id").toString();
					}
					//清除通知缓存
					List<Map<String, Object>> childs = this.deviceDB.getDeviceByUidPool(toUid);
					if(childs.size() > 0){
						String[] keys = new String[childs.size()+1];
						for(int i=0;i<childs.size();i++){
							keys[i] = redisConf.NOTICE_STATICS + childs.get(i).get("id").toString();
						}
						keys[childs.size()] = redisConf.NOTICE_STATICS + toUid;
						this.redisCache.delete(keys,this.getJedisResourse());
					}
	    		}else{
	    			log.error("sql exec failure");
		 			throw new MyException("Handle the comm_result data of the device["+this.mac+"] for mid["+this.mid+"] failure",MyException.DATA_HANDLE_FAILURE);
	    		}
    		}
    	}
    	log.debug("Handle the comm_result data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    //处理升级结果
    @SuppressWarnings("unchecked")
	private void handleUpgrade(Map<String,Object> device,Upgrade value,Long time) throws MyException{
    	log.debug("Start handle the upgrade data of the device["+this.mac+"] for mid["+this.mid+"]");
		String orderID = value.getOrderid();
 		Map<String,String> updates = new HashMap<String,String>();
 		Object status = value.getStatus();
 		if(status instanceof String){//设备本身处理
 			updates.put(this.mac, status.toString());
 		}else{//子设备处理
 			for(Map.Entry<String,String> entry : ((Map<String, String>) status).entrySet()){
 				updates.put(entry.getKey(), entry.getValue());
 			}
 		}
 		//更新当前设备升级状态
 		if(!updates.isEmpty()){
 			this.redisCache.updateOrderDeviceStatus(orderID,updates,this.getJedisResourse());
 		}
    	log.debug("Handle the upgrade data of the device["+this.mac+"] for mid["+this.mid+"] success");
    }
    
    //处理ask数据
    private void handleAsk(Map<String,Object> device,Ask value,Long time)throws MyException{
    	log.debug("Start handle the ask data of the device["+this.mac+"] for mid["+this.mid+"]");
    	Boolean rs = true;	
    	String type = value.getType();
    	List<String> childMacs = value.getChild();
    	switch(type){
    		case "user":
    			if(childMacs != null && !childMacs.isEmpty()){
    				rs = this.handleAskChildUser(device,childMacs);
    			}else{
    				rs = this.handleAskUser(device);
    			}
    			break;
    		case "child":
    			rs = this.handAskChild(device);
    			break;
    		default:
    			break;
    	}
    	String allow = rs ? "1" : "0";
    	String commID = Helper.getCommID(yunlotConf.LOTTYPE_DOWN,deviceConf.TYPE_INFO_REPLY,time);
    	String message = "";
    	if(childMacs != null && !childMacs.isEmpty()){
    		message = "{\"header\":{\"protocol\":\""+yunlotConf.VERSION+"\",\"type\":\""+yunlotConf.LOTTYPE_DOWN+"\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(6)+time+"\"},\"body\":{\"comm_id\":\""+commID+"\",\"command\":{\"type\":\"reply\",\"child\":"+JSON.toJSONString(childMacs)+",\"up\":\""+type+"\",\"allow\":\""+allow+"\"}},\"now\":\""+time+"\"}";
    	}else{
    		message = "{\"header\":{\"protocol\":\""+yunlotConf.VERSION+"\",\"type\":\""+yunlotConf.LOTTYPE_DOWN+"\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(6)+time+"\"},\"body\":{\"comm_id\":\""+commID+"\",\"command\":{\"type\":\"reply\",\"up\":\""+type+"\",\"allow\":\""+allow+"\"}},\"now\":\""+time+"\"}";
    	}	
    	
    	Helper.sendMqtt(Helper.getTopic(prtid,cltid), message);
		log.debug("Handle the ask data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    //处理告警数据
    private void handleAlarm(Map<String,Object> device,List<Alarm> value,Long time){
    	log.debug("Start handle the alarm data of the device["+this.mac+"] for mid["+this.mid+"]");
    	if(!value.isEmpty()){
    		String lang = "zh-cn";
    		List<Map<String,Object>> alarmSets = new ArrayList<Map<String,Object>>();
    		Map<String,Object> noticeSets = new HashMap<String,Object>();
    		List<String> saveDatas = new ArrayList<String>();
    		List<Map<String,String>> wechatNotices = new ArrayList<Map<String,String>>();
    		List<Map<String,String>> emailNotices = new ArrayList<Map<String,String>>();
    		Map<String,String> dynamic = new HashMap<String,String>();
    		
    		//dynamic.put("alarm_dev","1");
    		alarmSets = this.redisCache.getAlarmSet(this.mac,this.getJedisResourse());
    		if(alarmSets.isEmpty()){
    			alarmSets = this.deviceDB.getAlarmSetPool(this.mac);
    			if(!alarmSets.isEmpty()){
    				this.redisCache.setDevAlarm(this.mac, alarmSets,this.getJedisResourse());
    			}
    		}
    		noticeSets = this.redisCache.getAlarmNoticeSet(this.mac,this.getJedisResourse());
    		if(noticeSets.isEmpty()){
    			noticeSets = this.deviceDB.getAlarmNoticeSetPool(this.mac);
    			if(!noticeSets.isEmpty()){
    				this.redisCache.setDevAlarmNotice(this.mac,noticeSets,this.getJedisResourse());
    			}
    		}

    		if(!alarmSets.isEmpty() && !noticeSets.isEmpty()){
    			lang = noticeSets.get("lang").toString();
    			for(int i=0;i<value.size();i++){
    				Alarm dataValue = value.get(i);
    				String type = dataValue.getType();
    				for(int j=0;j<alarmSets.size();j++){
    					Map<String,Object> alarmSet = alarmSets.get(j);	
    					if(alarmSet.get("type").toString().equals(type) && alarmSet.get("enable").toString().equals("1")){
    						if(this.isAlarm(this.mac,type,Long.parseLong(alarmSet.get("interval").toString()),time)){
	    						saveDatas.add(dataValue.toString());
	    						Map<String,String> notice = new HashMap<String,String>();
	    						notice.put("type", type);
    							notice.put("value", dataValue.getValue());
	    						if(alarmSet.get("isemail").toString().equals("1")){
	    							emailNotices.add(notice);
	    						}
	    						if(alarmSet.get("iswechat").toString().equals("1")){
	    							wechatNotices.add(notice);
	    						}
    						}
    					}
    				}
					if(type.equals("5")){//cpu使用率
						dynamic.put("cpu_use",dataValue.getValue());
					}else if(type.equals("6")){//内存使用率
						dynamic.put("memory_use", dataValue.getValue());
					}else if(type.equals("8")){//链路质量
						dynamic.put("link", dataValue.getValue());
					}else if(type.equals("9")){//信号强度
						dynamic.put("rssi", dataValue.getValue());
					}else{
						
					}
    			}
    		}else{
    			for(int i=0;i<value.size();i++){
    				Alarm dataValue = value.get(i);
    				String type = dataValue.getType();
    				if(type.equals("5")){//cpu使用率
						dynamic.put("cpu_use",dataValue.getValue());
					}else if(type.equals("6")){//内存使用率
						dynamic.put("memory_use", dataValue.getValue());
					}else if(type.equals("8")){//链路质量
						dynamic.put("link", dataValue.getValue());
					}else if(type.equals("9")){//信号强度
						dynamic.put("rssi", dataValue.getValue());
					}else{
						
					}
    			}
    		}
    		if(!saveDatas.isEmpty()){
    			if(this.deviceDB.addDeviceAlarmPool(this.mac,JSON.toJSONString(saveDatas),time)){
    				if(!wechatNotices.isEmpty()){//发微信消息
    					SendAlarmByWechatThread wechatThr = new SendAlarmByWechatThread(device,noticeSets.get("user"),wechatNotices,lang,time);
    					wechatThr.start();
    				}
    				if(!emailNotices.isEmpty()){//发送邮件通知
    					SendAlarmByEmailThread emailThr = new SendAlarmByEmailThread(device,noticeSets.get("user"),emailNotices,lang,time);
    					emailThr.start();
    				}
    			}else{
    				log.error("Handle the device["+this.mac+"] for mid["+this.mid+"] alarm data failure"); 
    			}
    		}
    		if(!dynamic.isEmpty()){
    			String deviceSystem = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_SYSTEM, this.getJedisResourse());
    			if(!(deviceSystem == null)){
    				JSONObject deviceSystemJson = JSON.parseObject(deviceSystem);
    				for(Map.Entry<String,String> entry : dynamic.entrySet()){
    	   		 		String jkey = entry.getKey();
    	   		 		String jvalue = entry.getValue();
    	   		 		deviceSystemJson.put(jkey, jvalue);
    				}
    				this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_SYSTEM, deviceSystemJson.toJSONString(), this.getJedisResourse());
    				this.redisCache.parseDynamic(this.mac, dynamic, time, this.getJedisResourse());
    			}
    		}
    	}
    	log.debug("Handle the alarm data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private boolean isAlarm(String mac,String type,Long interval,Long time){
		Boolean rs = false;
		Long alarmTime;
		String alarmTimeStr = this.redisCache.getDeviceAlarmTime(mac,type,this.getJedisResourse());
		if(alarmTimeStr == null){
			alarmTime = time;
			rs = true;
		}else{
			alarmTime = Long.parseLong(alarmTimeStr);
			if(time - alarmTime > interval){//告警
				rs = true;
			}
		}
		this.redisCache.setDeviceAlarmTime(mac,type,String.valueOf(alarmTime),this.getJedisResourse());
		return rs;
	}
    
    private Boolean handleAskChildUser(Map<String,Object> device,List<String> macs){
    	Boolean rs = true;
    	if(macs != null && !macs.isEmpty()){
    		Pipeline pipelined = this.getJedisResourse().pipelined();
    		for(String mac:macs){
    			pipelined.hget(redisConf.DEVICE_PARAMS+mac,deviceConf.TYPE_INFO_USER);
    		}
    		List<Object> results = pipelined.syncAndReturnAll();
    		
    		for(int i=0;i<macs.size();i++){
    			log.debug("Clear the user of the device["+macs.get(i)+"]");
    			Object result = results.get(i);
    			if(result != null && result.toString() != ""){
    				pipelined.set(redisConf.OLDUSERS+mac,result.toString());
    				pipelined.hdel(redisConf.DEVICE_PARAMS+mac,deviceConf.TYPE_INFO_USER);
    			}
    			log.debug("Clear the user of the device["+macs.get(i)+"] done");
    		}
    		pipelined.sync();
    	}
    	return rs;
    }
    
    private Boolean handleAskUser(Map<String,Object> device){
    	Boolean rs = true;
    	String mac = device.get("dev_mac").toString();
    	log.debug("Clear the user of the device["+mac+"]");
    	String deviceUsers = this.redisCache.getDeviceParams(mac, deviceConf.TYPE_INFO_USER,this.getJedisResourse());
    	//删除数据前缓存旧数据，以解决云平台用户列表出现空问题
    	if(deviceUsers != null && deviceUsers != ""){
    		this.redisCache.set(redisConf.OLDUSERS+mac,deviceUsers,this.getJedisResourse());
    	}
    	this.redisCache.delDeviceParamsField(mac,deviceConf.TYPE_INFO_USER,this.getJedisResourse());
    	log.debug("Clear the user of the device["+mac+"] done");
    	return rs;
    }
    
    private Boolean handAskChild(Map<String,Object> device){
    	Boolean rs = true;
    	String mac = device.get("dev_mac").toString();
    	log.debug("Clear the child of the device["+mac+"]");
    	List<Map<String, Object>> childs = this.deviceDB.getChildsPool(mac,true,this.getJedisResourse());
    	if(!childs.isEmpty()){
    		//删除前缓存旧数据，以解决云平台ap列表空问题
    		List<String> macs = new ArrayList<String>();
    		List<String> delMacs = new ArrayList<String>();
    		String childMac;
    		Map<String,Object> child = new HashMap<String,Object>();
    		for(int i=0;i<childs.size();i++){
    			child = childs.get(i);
    			childMac = child.get("dev_mac").toString();
    			macs.add(childMac);
    			delMacs.add(redisConf.DEVICE_REPORT+childMac);
    			//delMacs.add(redisConf.DEVICE_DYNAMIC+childMac);
    			delMacs.add(redisConf.DEVICE_PARAMS+childMac);
    			delMacs.add(redisConf.REGISTER+childMac);
    			this.redisCache.set(redisConf.OLDCHILDS+childMac,JSONObject.toJSON(child).toString(),this.getJedisResourse());
    		}
    		this.redisCache.set(redisConf.OLDCHILDSLIST+mac,JSONArray.toJSON(macs).toString(),this.getJedisResourse());
    		//删除数据
    		rs = this.deviceDB.deleteChildsByMacsPool(macs);
    		
    		this.redisCache.delete(delMacs.toArray(new String[]{}),this.getJedisResourse());
    	}
    	return rs;
    }
    
    private void handleAllChildSystem(List<Ichild> list,String toUid,String gid,Long time,Map<String,Object> pdevice,String now) throws Exception{
    	log.debug("Start handle all child system data of the device["+this.mac+"] for mid["+this.mid+"]");
    	Boolean result = true;
    	Connection connection = null;
		PreparedStatement addPstmt = null;
		PreparedStatement relationPstmt = null;
    	try{
    		connection = DruidUtils.getConnection();
    		connection.setAutoCommit(false);
    		addPstmt = connection.prepareStatement(Device.sql_replace_child);
    		relationPstmt = connection.prepareStatement(Device.sql_replace_relation);
    		Boolean systemFlag = false;
    		for(int i=0;i<list.size();i++){
    			Ichild child = list.get(i);
    			lib.yuncorelot.body.System childSystem = child.getSystem();
    			if(childSystem != null){
    				systemFlag = true;
    				
    				String chip = childSystem.getChip() != null ? childSystem.getChip() : "";
    				String weblogin_pwd = childSystem.getWeblogin_pwd() != null ? childSystem.getWeblogin_pwd() : "";
    				String latitude = "0.0000000";
    				String longitude = "0.0000000";
    				String dev_username = childSystem.getDev_username() != null ? childSystem.getDev_username() : "";
    				String prtType = "2";	
    	    		String type = childSystem.getType() != null ? childSystem.getType() : "";
    	    		String version = childSystem.getVersion() != null ? childSystem.getVersion() : "";
    	    		String mode = childSystem.getMode() != null ? childSystem.getMode() : "";
    	    		String mac = child.getMac();
    	    		String netIp = childSystem.getNet_ip() != null ? childSystem.getNet_ip() : "";
    	    		String name = childSystem.getName() != null ? childSystem.getName() : "";
    	    		String devIp = childSystem.getDev_ip() != null ? childSystem.getDev_ip() : "";
    	    		String is_ip_location = (childSystem.getLocation() != null) && (childSystem.getNet_ip() != null) ? "1" : "0";
    	    		String newversion = "";
    	    		String package_fid = "";
    	    		Map<String,Object> newPackage = this.redisCache.getCloudLatestPackageByType(type,this.getJedisResourse());
    				if(newPackage == null){
    					newPackage = this.deviceDB.getCloudLatestPackageByTypePool(type);
    					this.redisCache.setCloudLatestPackageByType(type,newPackage,this.getJedisResourse());
    				}
    				if(newPackage != null && newPackage.get("version") != null && newPackage.get("fid") != null){
    					newversion = newPackage.get("version").toString();
    					package_fid = newPackage.get("fid").toString();
    				}
    				
    				if(childSystem.getLocation() != null){
    					latitude = childSystem.getLocation().get("lat");
    					longitude = childSystem.getLocation().get("lng");
    				}
    				if(prtid != null && publicConf.product.get(prtid) != null){
    					prtType = publicConf.product.get(prtid);
    				}
    				addPstmt.setObject(1,chip);
    				addPstmt.setObject(2,weblogin_pwd);
    				addPstmt.setObject(3,latitude);
    				addPstmt.setObject(4,dev_username);
    				addPstmt.setObject(5,time);
    				addPstmt.setObject(6,pdevice.get("dev_mac"));
    				addPstmt.setObject(7,prtType);
    				addPstmt.setObject(8,type);
    				addPstmt.setObject(9,version);
    				addPstmt.setObject(10,time);
    				addPstmt.setObject(11,mode);
    				addPstmt.setObject(12,mac);
    				addPstmt.setObject(13,netIp);
    				addPstmt.setObject(14,time);
    				addPstmt.setObject(15,toUid);
    				addPstmt.setObject(16,gid);
    				addPstmt.setObject(17,name);
    				addPstmt.setObject(18,devIp);
    				addPstmt.setObject(19,is_ip_location);
    				addPstmt.setObject(20,longitude);
    				addPstmt.setObject(21,newversion);
    				addPstmt.setObject(22,package_fid);
    				addPstmt.addBatch();
    				
    				relationPstmt.setObject(1,toUid);
    				relationPstmt.setObject(2,mac);
    				relationPstmt.setObject(3,pdevice.get("dev_mac"));
    				relationPstmt.setObject(4,time);
    				relationPstmt.setObject(5,time);
    				relationPstmt.addBatch();
    				
    				//更新设备CPU使用率、内存使用率、运行时间
            		Map<String,String> dynamic = new HashMap<String,String>();
            		if(childSystem.getStatus() != null){
            			String status = childSystem.getStatus();
                		dynamic.put("status",status);
            		}
            		
            		if(childSystem.getCpu_use() != null){
            			dynamic.put("cpu_use",childSystem.getCpu_use());
            		}
            		if(childSystem.getMemory_use() != null){
            			dynamic.put("memory_use", childSystem.getMemory_use());
            		}
            		if(childSystem.getRuntime() != null){
            			dynamic.put("runtime", childSystem.getRuntime());
            		}
            		if(childSystem.getAbility() != null){
            			dynamic.put("ability",JSON.toJSONString(childSystem.getAbility()));
            		}
            		if(childSystem.getParent() != null){
            			if(childSystem.getParent().get("mac") != null){
            				dynamic.put("parent",this.getParentMac(childSystem.getParent().get("mac").toString()));
            			}
            			if(childSystem.getParent().get("link") != null){
            				dynamic.put("link",childSystem.getParent().get("link").toString());
            			}
            			if(childSystem.getParent().get("rssi") != null){
            				dynamic.put("rssi",childSystem.getParent().get("rssi").toString());
            			}
            		}
            		if(childSystem.getAlarm() != null){
            			dynamic.put("alarm_dev", childSystem.getAlarm());
            		}
            		this.redisCache.parseDynamic(mac,dynamic, time,this.getJedisResourse());
            		this.redisCache.setDeviceParams(mac,deviceConf.TYPE_INFO_SYSTEM, childSystem.toString(),this.getJedisResourse());
            		if(childSystem.getEth() != null){
            			this.redisCache.handleEth(mac,childSystem.getEth(),this.getJedisResourse());
            		}
            		//工作组中设备数量变化统计队列
            		this.redisCache.addGroupQueue("{\"gid\":\""+gid+"\",\"increase\":\"-1\"}",this.getJedisResourse());
    			}
    		}
    		if(systemFlag){
    			log.debug(addPstmt.toString());
    			log.debug(relationPstmt.toString());
	    		addPstmt.executeBatch();
	    		relationPstmt.executeBatch();
	    		addPstmt.clearBatch();
	    		relationPstmt.clearBatch();
	    		connection.commit();
    		}
    		result = true;
    	}catch(Exception e){
    		connection.rollback();
    		log.error(e.getMessage());
    		result = false;	
    	}finally{
    		connection.setAutoCommit(true);
    		DruidUtils.close(null, addPstmt, connection);
    	}
    	log.debug("Handle all child system data of the device["+this.mac+"] for mid["+this.mid+"] done");
    	/*if(result){
    		Helper.log("Handle all child system data of the device["+this.mac+"] for mid["+this.mid+"] done","info",log);
    	}else{
    		throw new MyException("Handle all child system data of the device["+this.mac+"] for mid["+this.mid+"] failure",MyException.DATA_HANDLE_FAILURE);
    	}*/
    }
    
    private void handleAllChildData(List<Ichild> list,String toUid,String gid,Long time,Map<String,Object> pdevice,String now) throws MyException{
    	try{
    		Ichild child = new Ichild();
    		if(list.size() > 0){
    			this.handleAllChildSystem(list,toUid,gid,time,pdevice,now);
	    		for(int i=0;i<list.size();i++){
	    			child = list.get(i);
	    			this.mac = child.getMac();
	    					
	    			if(child.getNetwork() != null){
	    				this.handleNetwork(null,child.getNetwork(), time);
	    			}
	    			if(child.getWifi() != null){
	    				this.handleWifi(null,child.getWifi(), time);
	    			}
	    			if(child.getUser() != null){
	    				this.handleUser(null,child.getUser(), time,now);
	    			}
	    			if(child.getTime_reboot() != null){
	    				this.handleTimereboot(null,child.getTime_reboot(),time);
	    			}
	    			if(child.getAlarm() != null){
	    				this.handleAlarm(null,child.getAlarm(),time);
	    			}
	    			if(child.getAuth() != null){
		    			this.handleAuth(null,child.getAuth(),time);
		    		}
	    			if(child.getTime_group() != null){
		    			this.handleTimegroup(null,child.getTime_group(),time);
		    		}
	    			if(child.getIp_group() != null){
		    			this.handleIpgroup(null,child.getIp_group(),time);
		    		}
	    			if(child.getRate_limit() != null){
	    				this.handleRateLimit(null,child.getRate_limit(),time);
	    			}
	    			if(child.getFilter_url() != null){
	    				this.handleFilterUrl(null, child.getFilter_url(), time);
	    			}
	    			if(child.getFilter_iport() != null){
	    				this.handleFilterIpport(null, child.getFilter_iport(), time);
	    			}
	    			if(child.getFilter_mac() != null){
	    				this.handleFilterMac(null, child.getFilter_mac(), time);
	    			}
	    			if(child.getIpport_map() != null){
	    				this.handleIpportMap(null,child.getIpport_map(),time);
	    			}
	    			if(child.getClient_config() != null){
		    			this.handleClientConfig(null,child.getClient_config(),time);
		    		}
	    			this.mac = pdevice.get("dev_mac").toString();
	    		}
    		}
    	}catch(Exception e){
    		throw new MyException(e.getMessage(),MyException.DATA_HANDLE_FAILURE);
    	}
    }
    
    private void handleChildData(List<Ichild> list,String toUid,String gid,Long time,Map<String,Object> pdevice,String now){
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
    					device = null;
    				}
    			}
    			if(child.getSystem() != null){
    				this.handleSystem(device, toUid, gid,child.getSystem(), time,pdevice.get("dev_mac").toString());
    			}
    			if(child.getNetwork() != null){
    				this.handleNetwork(device,child.getNetwork(), time);
    			}
    			if(child.getWifi() != null){
    				this.handleWifi(device,child.getWifi(), time);
    			}
    			if(child.getUser() != null){
    				this.handleUser(device,child.getUser(), time,now);
    			}
    			if(child.getTime_reboot() != null){
    				this.handleTimereboot(device,child.getTime_reboot(),time);
    			}
    			if(child.getAlarm() != null){
    				this.handleAlarm(device,child.getAlarm(),time);
    			}
    			if(child.getAuth() != null){
	    			this.handleAuth(device,child.getAuth(),time);
	    		}
    			if(child.getTime_group() != null){
	    			this.handleTimegroup(device,child.getTime_group(),time);
	    		}
    			if(child.getIp_group() != null){
	    			this.handleIpgroup(device,child.getIp_group(),time);
	    		}
    			if(child.getRate_limit() != null){
    				this.handleRateLimit(device,child.getRate_limit(),time);
    			}
    			if(child.getFilter_url() != null){
    				this.handleFilterUrl(device, child.getFilter_url(), time);
    			}
    			if(child.getFilter_iport() != null){
    				this.handleFilterIpport(device, child.getFilter_iport(), time);
    			}
    			if(child.getFilter_mac() != null){
    				this.handleFilterMac(device, child.getFilter_mac(), time);
    			}
    			if(child.getIpport_map() != null){
    				this.handleIpportMap(device,child.getIpport_map(),time);
    			}
    			if(child.getClient_config() != null){
	    			this.handleClientConfig(device,child.getClient_config(),time);
	    		}
    			this.mac = pdevice.get("dev_mac").toString();
    		}
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    }
    
    private void handleAuth(Map<String,Object> device,Auth value,Long time) throws MyException{
    	log.debug("Start handle the auth result data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String radioid = value.getRadioid();
    	String vapid = value.getVapid();
    	String client_mac = value.getClient_mac();
    	String client_ip = value.getClient_ip();
    	String status = value.getStatus();
    	String token = value.getToken();
    	if(radioid == null && vapid == null && client_mac == null && client_ip == null && status == null && token == null){//设备重置了
    		//清除认证配置
    		this.deviceDB.clearDeviceAuthPool(this.mac);
    		//清除认证缓存
    		this.redisCache.clearDeviceAuth(this.mac,this.dev_gid, this.getJedisResourse());
    	}else{
    		if(client_mac == null){
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
		
	    	if(status.equals("0") || status.equals("1")){//告知放行
	    		log.debug("Handle allow result");
	    		this.handleAllowResult(token,this.mac,client_mac,radioid,vapid,status,time);
	    	}
    	}
    	log.debug("Handle the auth result data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleTimegroup(Map<String,Object> device,TimeGroup value,Long time) throws MyException{
    	log.debug("Start handle the time_group data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String max = value.getMax();
    	TimeGroup saveData = new TimeGroup();
    	
    	List<Map<String,String>> oldList = new ArrayList<Map<String,String>>();
    	String oldTimeGroupStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_TIME_GROUP,this.getJedisResourse());
    	String oldMax = "0";
    	if(oldTimeGroupStr != null){
	    	TimeGroup oldTimeGroup = JSON.parseObject(oldTimeGroupStr, new TypeReference<TimeGroup>(){});
	    	oldList = oldTimeGroup.getList();
	    	oldMax = oldTimeGroup.getMax();
    	}else{
    		if(max == null){
    			throw new MyException("The time_group data is error",MyException.YUNLOT_UPINFO_ERROR); 
    		}
    	}
    	
    	if(max != null){
    		saveData.setMax(max);
    	}else{
    		saveData.setMax(oldMax);
    	}
    	saveData.setList(this.parseAllChangeList(value, oldList,"identy"));
    	if(Integer.parseInt(saveData.getMax()) < saveData.getList().size()){
    		throw new MyException("The time_group rules is reached max",MyException.RULES_MAX); 
    	}
    	//写参数
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_TIME_GROUP,saveData.toString(),this.getJedisResourse());
    	log.debug("Handle the time_group data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleIpgroup(Map<String,Object> device,IpGroup value,Long time) throws MyException{
    	log.debug("Start handle the ip_group data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String max = value.getMax();
    	IpGroup saveData = new IpGroup();

    	List<Map<String,String>> oldList = new ArrayList<Map<String,String>>();
    	String oldIpGroupStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_IP_GROUP,this.getJedisResourse());
    	String oldMax = "0";
    	if(oldIpGroupStr != null){
	    	IpGroup oldIpGroup = JSON.parseObject(oldIpGroupStr, new TypeReference<IpGroup>(){});
	    	oldList = oldIpGroup.getList();
	    	oldMax = oldIpGroup.getMax();
    	}else{
    		if(max == null){
    			throw new MyException("The ip_group data is error",MyException.YUNLOT_UPINFO_ERROR); 
    		}
    	}
    	
    	if(max != null){
    		saveData.setMax(max);
    	}else{
    		saveData.setMax(oldMax);
    	}
    	saveData.setList(this.parseAllChangeList(value, oldList,"identy"));
    	if(Integer.parseInt(saveData.getMax()) < saveData.getList().size()){
    		throw new MyException("The ip_group rules is reached max",MyException.RULES_MAX); 
    	}
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_IP_GROUP,saveData.toString(),this.getJedisResourse());
    	log.debug("Handle the ip_group data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleRateLimit(Map<String,Object> device,RateLimit value,Long time) throws MyException{
    	log.debug("Start handle the rate_limit data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String max = value.getMax();
    	String enable = value.getEnable();
    	RateLimit saveData = new RateLimit();

    	List<Map<String,String>> oldList = new ArrayList<Map<String,String>>();
    	String oldRateLimitStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_RATE_LIMIT,this.getJedisResourse());
    	String oldMax = "0";
    	String oldEnable = "0";
    	if(oldRateLimitStr != null){
	    	RateLimit oldRateLimit = JSON.parseObject(oldRateLimitStr, new TypeReference<RateLimit>(){});
	    	oldList = oldRateLimit.getList();
	    	oldMax = oldRateLimit.getMax();
	    	oldEnable = oldRateLimit.getEnable();
    	}else{
    		if(max == null){
    			throw new MyException("The rate_limit data is error",MyException.YUNLOT_UPINFO_ERROR); 
    		}
    		if(enable == null){
    			throw new MyException("The rate_limit data is error",MyException.YUNLOT_UPINFO_ERROR);
    		}
    	}
    	
    	if(max != null){
    		saveData.setMax(max);
    	}else{
    		saveData.setMax(oldMax);
    	}
    	if(enable != null){
    		saveData.setEnable(enable);
    	}else{
    		saveData.setEnable(oldEnable);
    	}
    	saveData.setList(this.parseAllChangeList(value, oldList,"identy"));
    	if(Integer.parseInt(saveData.getMax()) < saveData.getList().size()){
    		throw new MyException("The rate_limit rules is reached max",MyException.RULES_MAX); 
    	}
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_RATE_LIMIT,saveData.toString(),this.getJedisResourse());
    	log.debug("Handle the rate_limit data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleFilterUrl(Map<String,Object> device,FilterUrl value,Long time) throws MyException{
    	log.debug("Start handle the filter_url data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String max = value.getMax();
    	String enable = value.getEnable();
    	FilterUrl saveData = new FilterUrl();

    	List<Map<String,String>> oldList = new ArrayList<Map<String,String>>();
    	String oldFilterUrlStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_FILTER_URL,this.getJedisResourse());
    	String oldMax = "0";
    	String oldEnable = "0";
    	if(oldFilterUrlStr != null){
	    	FilterUrl oldFilterUrl = JSON.parseObject(oldFilterUrlStr, new TypeReference<FilterUrl>(){});
	    	oldList = oldFilterUrl.getList();
	    	oldMax = oldFilterUrl.getMax();
	    	oldEnable = oldFilterUrl.getEnable();
    	}else{
    		if(max == null){
    			throw new MyException("The filter_url data is error",MyException.YUNLOT_UPINFO_ERROR); 
    		}
    		if(enable == null){
    			throw new MyException("The filter_url data is error",MyException.YUNLOT_UPINFO_ERROR);
    		}
    	}
    	
    	if(max != null){
    		saveData.setMax(max);
    	}else{
    		saveData.setMax(oldMax);
    	}
    	if(enable != null){
    		saveData.setEnable(enable);
    	}else{
    		saveData.setEnable(oldEnable);
    	}
    	saveData.setList(this.parseAllChangeList(value, oldList,"identy"));
    	if(Integer.parseInt(saveData.getMax()) < saveData.getList().size()){
    		throw new MyException("The filter_url rules is reached max",MyException.RULES_MAX); 
    	}
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_FILTER_URL,saveData.toString(),this.getJedisResourse());
    	log.debug("Handle the filter_url data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleFilterIpport(Map<String,Object> device,FilterIpport value,Long time) throws MyException{
    	log.debug("Start handle the filter_ipport data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String max = value.getMax();
    	String enable = value.getEnable();
    	FilterUrl saveData = new FilterUrl();

    	List<Map<String,String>> oldList = new ArrayList<Map<String,String>>();
    	String oldFilterIpportStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_FILTER_IPPORT,this.getJedisResourse());
    	String oldMax = "0";
    	String oldEnable = "0";
    	if(oldFilterIpportStr != null){
	    	FilterIpport oldFilterIpport = JSON.parseObject(oldFilterIpportStr, new TypeReference<FilterIpport>(){});
	    	oldList = oldFilterIpport.getList();
	    	oldMax = oldFilterIpport.getMax();
	    	oldEnable = oldFilterIpport.getEnable();
    	}else{
    		if(max == null){
    			throw new MyException("The filter_ipport data is error",MyException.YUNLOT_UPINFO_ERROR); 
    		}
    		if(enable == null){
    			throw new MyException("The filter_ipport data is error",MyException.YUNLOT_UPINFO_ERROR);
    		}
    	}
    	
    	if(max != null){
    		saveData.setMax(max);
    	}else{
    		saveData.setMax(oldMax);
    	}
    	if(enable != null){
    		saveData.setEnable(enable);
    	}else{
    		saveData.setEnable(oldEnable);
    	}
    	saveData.setList(this.parseAllChangeList(value, oldList,"identy"));
    	if(Integer.parseInt(saveData.getMax()) < saveData.getList().size()){
    		throw new MyException("The filter_ipport rules is reached max",MyException.RULES_MAX); 
    	}
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_FILTER_IPPORT,saveData.toString(),this.getJedisResourse());
    	log.debug("Handle the filter_ipport data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleFilterMac(Map<String,Object> device,FilterMac value,Long time) throws MyException{
    	log.debug("Start handle the filter_mac data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String max = value.getMax();
    	String enable = value.getEnable();
    	FilterMac saveData = new FilterMac();

    	List<Map<String,String>> oldList = new ArrayList<Map<String,String>>();
    	String oldFilterMacStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_FILTER_MAC,this.getJedisResourse());
    	String oldMax = "0";
    	String oldEnable = "0";
    	if(oldFilterMacStr != null){
	    	FilterMac oldFilterMac = JSON.parseObject(oldFilterMacStr, new TypeReference<FilterMac>(){});
	    	oldList = oldFilterMac.getList();
	    	oldMax = oldFilterMac.getMax();
	    	oldEnable = oldFilterMac.getEnable();
    	}else{
    		if(max == null){
    			throw new MyException("The filter_mac data is error",MyException.YUNLOT_UPINFO_ERROR); 
    		}
    		if(enable == null){
    			throw new MyException("The filter_mac data is error",MyException.YUNLOT_UPINFO_ERROR);
    		}
    	}
    	
    	if(max != null){
    		saveData.setMax(max);
    	}else{
    		saveData.setMax(oldMax);
    	}
    	if(enable != null){
    		saveData.setEnable(enable);
    	}else{
    		saveData.setEnable(oldEnable);
    	}
    	saveData.setList(this.parseAllChangeList(value, oldList,"identy"));
    	if(Integer.parseInt(saveData.getMax()) < saveData.getList().size()){
    		throw new MyException("The filter_mac rules is reached max",MyException.RULES_MAX); 
    	}
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_FILTER_MAC,saveData.toString(),this.getJedisResourse());
    	log.debug("Handle the filter_mac data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleIpportMap(Map<String,Object> device,IpportMap value,Long time) throws MyException{
    	log.debug("Start handle the ipport_map data of the device["+this.mac+"] for mid["+this.mid+"]");
    	String max = value.getMax();
    	String enable = value.getEnable();
    	FilterMac saveData = new FilterMac();

    	List<Map<String,String>> oldList = new ArrayList<Map<String,String>>();
    	String oldIpportMapStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_IPPORT_MAP,this.getJedisResourse());
    	String oldMax = "0";
    	String oldEnable = "0";
    	if(oldIpportMapStr != null){
	    	IpportMap oldIpportMap = JSON.parseObject(oldIpportMapStr, new TypeReference<IpportMap>(){});
	    	oldList = oldIpportMap.getList();
	    	oldMax = oldIpportMap.getMax();
	    	oldEnable = oldIpportMap.getEnable();
    	}else{
    		if(max == null){
    			throw new MyException("The ipport_map data is error",MyException.YUNLOT_UPINFO_ERROR); 
    		}
    		if(enable == null){
    			throw new MyException("The ipport_map data is error",MyException.YUNLOT_UPINFO_ERROR);
    		}
    	}
    	
    	if(max != null){
    		saveData.setMax(max);
    	}else{
    		saveData.setMax(oldMax);
    	}
    	if(enable != null){
    		saveData.setEnable(enable);
    	}else{
    		saveData.setEnable(oldEnable);
    	}
    	saveData.setList(this.parseAllChangeList(value, oldList,"identy"));
    	if(Integer.parseInt(saveData.getMax()) < saveData.getList().size()){
    		throw new MyException("The ipport_map rules is reached max",MyException.RULES_MAX); 
    	}
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_IPPORT_MAP,saveData.toString(),this.getJedisResourse());
    	log.debug("Handle the ipport_map data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleDmz(Map<String,Object> device,Dmz value,Long time) throws MyException{
    	log.debug("Start handle the dmz data of the device["+this.mac+"] for mid["+this.mid+"]");
    	Dmz oldDmz = new Dmz();
    	
    	String oldDmzStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_DMZ,this.getJedisResourse());
    	if(oldDmzStr != null){
    		oldDmz = JSON.parseObject(oldDmzStr,new TypeReference<Dmz>(){});
    	}
    	
    	String[] keys = {"Enable","Address"};
    	Class<? extends Dmz> value_T = value.getClass();
    	Class<? extends Dmz> oldDmz_T = oldDmz.getClass();
		for(String key:keys){
			try{
				Object tmp = value_T.getMethod("get"+key).invoke(value);
				if(tmp != null){
					oldDmz_T.getMethod("set"+key,String.class).invoke(oldDmz,tmp.toString());
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
		this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_DMZ,oldDmz.toString(),this.getJedisResourse());
    	log.debug("Handle the dmz data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private void handleClientConfig(Map<String,Object> device,ClientConfig value,Long time) throws MyException{
    	log.debug("Start handle the client_config data of the device["+this.mac+"] for mid["+this.mid+"]");
    	
    	ClientConfig newClientConfig = new ClientConfig();
    	
    	List<Map<String,String>> oldList = new ArrayList<Map<String,String>>();
    	String oldClientConfigStr = this.redisCache.getDeviceParams(this.mac, deviceConf.TYPE_INFO_CLIENT,this.getJedisResourse());
    	if(oldClientConfigStr != null){
	    	ClientConfig oldClientConfig = JSON.parseObject(oldClientConfigStr, new TypeReference<ClientConfig>(){});
	    	oldList = oldClientConfig.getList();
    	}
 	
    	List<Map<String,String>> newList = this.parseAllChangeList(value, oldList,"mac");
    	newClientConfig.setTotal(String.valueOf(newList.size()));
    	newClientConfig.setList(newList);
    	//写参数
    	this.redisCache.setDeviceParams(this.mac, deviceConf.TYPE_INFO_CLIENT, newClientConfig.toString(), this.getJedisResourse());
    	log.debug("Handle the client_config data of the device["+this.mac+"] for mid["+this.mid+"] done");
    }
    
    private List<Map<String,String>> parseAllChangeList(AllChangeUp value,List<Map<String,String>> oldList,String key) throws MyException{
    	List<Map<String,String>> newList = new ArrayList<Map<String,String>>();
    	String total = value.getTotal();
    	List<Map<String,String>> list = value.getList();
    	List<Map<String,String>> change = value.getChange();
    	List<String> delete = value.getDelete();
    	
    	if(list == null && change == null && delete == null){
    		newList = oldList;
    	}else{
    		if(total != null && list != null){//全量式上报
    			if(list.size() > publicConf.up_number){
    				throw new MyException("Too many clients",MyException.YUNLOT_UPINFO_ERROR);
    			}
    			if(value.getIndex().equals("0")){
    				newList = list;
    			}else{
    				if(oldList != null)
    					newList.addAll(oldList);
    				newList.addAll(list);
    			}
    		}else{//增量上报
    			if(value.getChange() != null){
    				newList = Helper.mergeList(oldList,value.getChange(),key);
    			}
    			if(value.getDelete() != null){
    				newList = Helper.deletesList(oldList,value.getDelete(),key);
    			}
    		}
    	}
    	return newList;
    }
    
    private void handleAllowResult(String token,String mac,String client_mac,String radioid,String vapid,String status,Long time){
    	Map<String,Object> deviceAuthInfo = this.redisCache.getDeviceAuthWithPolicy(mac,radioid,vapid,this.getJedisResourse());
    	if(deviceAuthInfo == null){
    		deviceAuthInfo = this.deviceDB.getDeviceAuthWithPolicyPool(mac,radioid,vapid);
    		if(!deviceAuthInfo.isEmpty()){
    			this.redisCache.setDeviceAuthWithPolicy(mac,radioid,vapid,2952000,deviceAuthInfo,this.getJedisResourse());
    		}
    	}

    	if(!deviceAuthInfo.isEmpty() && deviceAuthInfo.get("enable").toString().equals("1")){//开启认证配置了
    		Long expireAt = 0L;
    		String expireIn = deviceAuthInfo.get("expire_in").toString();
    		if(!expireIn.equals("0")){
    			expireAt = time + Long.parseLong(expireIn);
    		}
    		this.redisCache.synsAuthAllow(mac,client_mac,radioid,vapid,status,String.valueOf(expireAt),false,this.getJedisResourse());
    		/*Map<String,List<String>> roam = this.redisCache.getAuthAllowWithRoam(this.dev_gid,this.getJedisResourse());
    		if(roam.containsKey(this.mac)&&status.equals("1")){//开启了同组漫游,并且认证通过了，需要同步同组漫游
    			List<String> authed = roam.get(this.mac);
    			if(!authed.contains(client_mac)){
    				authed.add(client_mac);
    				roam.put(this.mac,authed);
    				this.redisCache.setAuthAllowWithRoam(this.dev_gid,JSON.toJSONString(roam),this.getJedisResourse());
    			}
    		}*/
    		//写认证结果到认证明细
    		if(token != null){
    			this.deviceDB.setAuthLoginInfoPool(token,mac,client_mac,status);
    		}
    	}
    }
    //这个方法区别与Helper中的方法,Helper中的方法相当于以base为基础进行更新
/*    private List<Map<String,String>> mergeList(List<Map<String,String>> bases,List<Map<String,String>> datas,String keyword){
    	Map<String,String> datasObject = null;
    	Map<String,String> basesObject = null;
    	Boolean flag = false;
    	for(int i=0;i<datas.size();i++){
    		datasObject = datas.get(i);
    		for(int j=0;j<bases.size();j++){
    			basesObject = bases.get(j);
    			if(datasObject.get(keyword) != null && datasObject.get(keyword).equals(basesObject.get(keyword))){
    				flag = true;
        			break;
    			}
    		}
    		if(flag){
    			bases.add(bases.indexOf(basesObject),datasObject);
    			flag = false;
    		}else{
    			bases.add(datasObject);
    		}
    	}
    	return bases;
    }*/
    
    private Map<String,String> getDeviceAddData(String mac,String pmac,String toUid,String gid,lib.yuncorelot.body.System systemData,Long time){
    	Map<String,String> result = new HashMap<String,String>();
    	String is_ip_location = (systemData.getLocation() != null) && (systemData.getNet_ip() != null) ? "1" : "0";
    	result.put("user_id",toUid);
    	result.put("join_time",String.valueOf(time));
    	result.put("dev_mac",mac);
		if(systemData.getDev_ip() == null){
			result.put("dev_ip","");
		}else{
			result.put("dev_ip",systemData.getDev_ip());
		}
		if(systemData.getNet_ip() == null){
			result.put("net_ip","");
		}else{
			result.put("net_ip",systemData.getNet_ip());
		}
		if(systemData.getName() == null){
			result.put("name","");
		}else{
			result.put("name",systemData.getName());
		}
		if(systemData.getType() == null){
			result.put("type","");
		}else{
			result.put("type",systemData.getType());
		}
		if(systemData.getMode() == null){
			result.put("mode","");
		}else{
			result.put("mode",systemData.getMode());
		}
		if(systemData.getVersion() == null){
			result.put("version","");
		}else{
			result.put("version",systemData.getVersion());
		}
		result.put("pid",pmac);
		if(systemData.getLocation() == null){
			result.put("latitude","0.0000000");
			result.put("longitude","0.0000000");
		}else{
			if(systemData.getLocation().get("lat") == null){
				result.put("latitude","0.0000000");
			}else{
				result.put("latitude",systemData.getLocation().get("lat").toString());
			}
			if(systemData.getLocation().get("lng") == null){
				result.put("longitude","0.0000000");
			}else{
				result.put("longitude",systemData.getLocation().get("lng").toString());
			}
		}
		if(systemData.getChip() == null){
			result.put("chip","");
		}else{
			result.put("chip",systemData.getChip());
		}
		result.put("group_id",gid);
		result.put("is_ip_location",is_ip_location);
		if(systemData.getDev_username() == null){
			result.put("dev_username","");
		}else{
			result.put("dev_username",systemData.getDev_username());
		}
		if(systemData.getWeblogin_pwd() == null){
			result.put("weblogin_pwd","");
		}else{
			result.put("weblogin_pwd",systemData.getWeblogin_pwd());
		}
		result.put("created_at",String.valueOf(time));
		result.put("updated_at",String.valueOf(time));
		if(!pmac.isEmpty()){
			String prtid = systemData.getPrtid();
			if(prtid == null){
				result.put("prt_type","2");
			}else{
				String prtType = publicConf.product.get(prtid);
				if(prtType != null && !prtType.isEmpty()){
					result.put("prt_type",prtType);
				}else{
					result.put("prt_type","2");
				}
			}
		}
		if(systemData.getRebind() != null){
			result.put("rebind",systemData.getRebind());
		}
		if(systemData.getIs_forceunbind() != null){
			result.put("is_forceunbind", systemData.getIs_forceunbind());
		}
		if(systemData.getLang() != null){
			result.put("lang",systemData.getLang());
		}
		this.redisCache.delete(redisConf.REGISTER+this.mac,this.getJedisResourse());
    	return result;
    }
    
    private Map<String,String> getDeviceUpdateData(Map<String,Object>device,String mac,String pmac,String toUid,String gid,lib.yuncorelot.body.System systemData,Long time){
    	Map<String,String> result = new HashMap<String,String>();
    	Boolean flag = false;
    	if(device.get("user_id") == null || device.get("user_id").toString().equals("0")){
    		result.put("user_id", toUid);
    		flag = true;
    	}
    	if(device.get("bind_status").toString().equals("1") && !device.get("bind").toString().isEmpty()){
    		result.put("join_time", String.valueOf(time));
    		result.put("bind_status","2");
    		flag = true;
    	}
    	if(this.isDifferentNoNull(systemData.getDev_ip(),device.get("dev_ip").toString())){
    		result.put("dev_ip",systemData.getDev_ip());
    	}
    	if(this.isDifferentNoNull(systemData.getNet_ip(), device.get("net_ip").toString())){
    		result.put("net_ip", systemData.getNet_ip());
    	}
    	if(this.isDifferentNoNull(systemData.getName(), device.get("name").toString())){
    		result.put("name", systemData.getName());
    	}
    	if(this.isDifferentNoNull(systemData.getType(), device.get("type").toString())){
    		result.put("type", systemData.getType());
    	}
    	if(this.isDifferentNoNull(systemData.getMode(), device.get("mode").toString())){
    		result.put("mode", systemData.getMode());
    	}
    	if(this.isDifferentNoNull(systemData.getVersion(), device.get("version").toString())){
    		result.put("version", systemData.getVersion());
    	}
    	if(this.isDifferentNoNull(pmac, device.get("pid").toString())){
    		result.put("pid", pmac);
    	}
    	
    	if(systemData.getLocation() != null){
    		if(systemData.getLocation().get("lat") != null){
	    		if(device.get("latitude").toString().isEmpty() || new BigDecimal(systemData.getLocation().get("lat").toString()).compareTo(new BigDecimal(device.get("latitude").toString())) != 0){
	    			result.put("latitude", systemData.getLocation().get("lat").toString());
	    		}
    		}
    		if(systemData.getLocation().get("lng") != null){
    			if(device.get("longitude").toString().isEmpty() || new BigDecimal(systemData.getLocation().get("lng").toString()).compareTo(new BigDecimal(device.get("longitude").toString())) != 0){
	    			result.put("longitude", systemData.getLocation().get("lng").toString());
	    		}
    		}
    	}
    	if(this.isDifferentNoNull(systemData.getChip(), device.get("chip").toString())){
    		result.put("chip", systemData.getChip());
    	}
    	if(device.get("group_id") == null || device.get("group_id").toString().isEmpty() || device.get("group_id").toString().equals("0")){
    		result.put("group_id", gid);
    		flag = true;
    	}
    	if(systemData.getLocation() != null && result.get("net_ip") != null && !device.get("is_ip_location").equals("1")){
    		result.put("is_ip_location", "1");
    	}
    	if(this.isDifferentNoNull(systemData.getDev_username(),device.get("dev_username").toString())){
    		result.put("dev_username",systemData.getDev_username());
    		flag = true;
    	}
    	if(this.isDifferentNoNull(systemData.getWeblogin_pwd(), device.get("weblogin_pwd").toString())){
    		result.put("weblogin_pwd", systemData.getWeblogin_pwd());
    		flag = true;
    	}
    	if(this.isDifferentNoNull(systemData.getRebind(), device.get("rebind").toString())){
    		result.put("rebind",systemData.getRebind());
    	}
    	if(systemData.getIs_forceunbind() != null){
    		if(device.get("is_forceunbind") == null){
    			result.put("is_forceunbind", systemData.getIs_forceunbind());
    		}else if(this.isDifferentNoNull(systemData.getIs_forceunbind(),device.get("is_forceunbind").toString())){
    			result.put("is_forceunbind", systemData.getIs_forceunbind());
    		}
    	}

    	if(device.get("lang") != null && this.isDifferentNoNull(systemData.getLang(),device.get("lang").toString())){
    		result.put("lang",systemData.getLang());
    	}

    	if(!result.isEmpty()){
    		result.put("updated_at",String.valueOf(time));
    	}
    	if(flag){
    		this.redisCache.delete(redisConf.REGISTER+this.mac,this.getJedisResourse());
    	}
    	return result;
    }
    
    private Boolean isDifferentNoNull(String str1,String str2){
    	Boolean rs = false;
    	if(str1 != null){
    		rs = !str1.equals(str2);
    	}
    	return rs;
    }
    
    private String getParentMac(String ethMac){
    	String res = ethMac;
    	if(mac != null || !mac.isEmpty()){
    		String str = this.redisCache.getParentMac(ethMac,this.getJedisResourse());
    		if(str != null && !str.isEmpty()){
    			res = str;
    		}
    	}
    	return res;
    }
     
}
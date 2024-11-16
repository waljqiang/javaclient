package lib;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import conf.deviceConf;
import conf.redisConf;
import conf.yunlotConf;
import redis.clients.jedis.Jedis;
import data.Device;

public class BindTaskThread extends Thread{
	private static final Log log = LogFactory.getLog(BindTaskThread.class);
	private Jedis jedis=null;
	private RedisCache redisCache;
	private String uid;
	private String gid;
	private String mac;
	private String devName;
	private String devUsername;
	private String devPassword;
	private String time;
	private String resource;
	private String bindCode;
	private String bindStatus;
	private String token;
	private Device deviceDB;
	
	public BindTaskThread(){
		this.jedis = helperRedis.getJedis();
		this.redisCache = new RedisCache();
		this.deviceDB = new Device();
	}
	
	@Override
	public void run(){
		log.fatal("Handle binds server started");
		while(true){
			try{
				String data = jedis.rpop(redisConf.DEVICE_BIND_QUEUE_DATA);
				if(data != null && data !=""){
					log.fatal("Begin handle binds["+data+"]");
					this.handleDatas(data);
					log.fatal("End handle bind["+data+"]");
				}else{
					Thread.sleep(3000);
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
	
	private void handleDatas(String str){
		List<Map<String,String>> datas = JSON.parseObject(str,new TypeReference<List<Map<String,String>>>(){});
		if(!datas.isEmpty()){
			this.uid = datas.get(0).get("uid").toString();
			this.gid = datas.get(0).get("gid").toString();
			this.time = datas.get(0).get("time").toString();
			
			final JdbcUtils jdbcUtils = new JdbcUtils();
			jdbcUtils.getConnection();
			try{
				for(Map<String,String> data : datas){
					this.mac = data.get("dev_mac");
					this.devName = data.get("name").toString();
					this.devUsername = data.get("dev_username").toString();
					this.devPassword = data.get("dev_password").toString();
					this.bindStatus = data.get("bind_status").toString();
					this.resource = data.get("resource").toString();
					if(this.devPassword.length() < 30){
						this.handleBind(jdbcUtils);
					}else{
						log.error("Binded failure,caused:The device["+this.mac+"] password is invalid");
					}
				}
				this.deviceDB.handleGroupNums(this.gid,jdbcUtils);
			}catch(Exception e){
				throw e;
			}finally{
				jdbcUtils.releaseConn();
			}
		}
	}
	
	private Boolean handleBind(JdbcUtils jdbcUtils){	
		log.debug("Handling device["+this.mac+"] binded to user["+this.uid+"]");
		Map<String,Object> device = this.deviceDB.getDevice(this.mac);
		if(!device.isEmpty()){
			String pwdEncrypt = device.get("pwd_encrypt").toString();
			if(pwdEncrypt.equals("2")){
				this.devPassword = Helper.encrypt(this.devPassword,"MD5");
			}else if(pwdEncrypt.equals("3")){
				this.devPassword = Helper.encrypt(this.devPassword,"SHA-256");
			}
		}

		this.bindCode = Helper.getBindCode(this.uid,this.mac,this.gid);
		this.token = Helper.generateDevToken(this.devUsername,this.devPassword,this.mac,Long.parseLong(this.time));
		Long timel = Long.parseLong(this.time);
		
		Boolean rs = true;
		Boolean rs1 = true;
		Boolean rs2 = true;
		Boolean rs3 = true;
		Boolean rs4 = true;
		Boolean rs5 = true;
		Boolean rs6 = true;
		jdbcUtils.setAutoCommit(false);
		if(device.isEmpty()){
			//添加设备
			rs1 = this.deviceDB.addDevice(this.getAddDeviceData(),jdbcUtils);
		}else{
			String devUid = device.get("user_id").toString();
			String devRebind = device.get("rebind").toString();
			if(!devRebind.toString().equals("1") && !devUid.isEmpty() && !devUid.equals("0")&&!devUid.equals(this.uid)){
				rs1 = false;
				log.error("Unhandle the device["+this.mac+"],because the device is binded to the user["+devUid+"]");
			}else{
				rs1 = this.deviceDB.updateDevice(this.mac,this.getUpdateDeviceData(),jdbcUtils);
				String prtid = device.get("prtid").toString();
				String cltid = device.get("cltid").toString();
				if(!prtid.isEmpty() && !cltid.isEmpty()){//如果设备已经与云平台通信，则发送绑定命令
					String bindCommand = "{\"header\":{\"protocol\":\""+yunlotConf.VERSION+"\",\"type\":\""
								+yunlotConf.LOTTYPE_DOWN+"\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(6)+this.time+"\"},\"body\":{\"comm_id\":\""
								+Helper.getCommID(yunlotConf.LOTTYPE_DOWN,deviceConf.TYPE_INFO_BIND,timel)
								+"\",\"command\":{\"type\":\"bind\",\"bind\":\""+this.bindCode+"\",\"name\":\""+this.devName+"\",\"token\":\""+this.token+"\"}},\"now\":\""
								+this.time+"\"}";
 					Helper.sendMqtt(Helper.getTopic(prtid,cltid),bindCommand);
				}
				//删除原拓扑图数据
				rs2 = this.deviceDB.deleteTopgraphyByUid(devUid,jdbcUtils);
				rs3 = this.deviceDB.deleteRelationByMac(this.mac,jdbcUtils);
				//删除故障告警相关数据
				rs4 = this.deviceDB.deleteAlarmSet(this.mac,jdbcUtils);
				rs5 = this.deviceDB.deleteAlarmNotice(this.mac,jdbcUtils);
				
				if(!device.get("group_id").toString().isEmpty()){
					this.deviceDB.handleGroupNums(device.get("group_id").toString(),jdbcUtils);
					this.redisCache.deleteTopgraphy(devUid,device.get("group_id").toString(),this.jedis);
				}
			}
		}

		//初始化拓扑图关系
		rs6 = this.deviceDB.toRelation(this.uid,this.mac,"",timel,jdbcUtils);

		if(!rs1 || !rs2 || !rs3 || !rs4 || !rs5 || !rs6){
			jdbcUtils.rollback();
			log.error("Handling device["+this.mac+"] binded to user["+this.uid+"] failure,rs1["+rs1+"]rs2["+rs2+"]rs3["+rs3+"]rs4["+rs4+"]rs5["+rs5+"]rs6["+rs6+"]");
			rs = false;
		}else{
			jdbcUtils.commit();
			//清除缓存
			Map<String,Object> dynamics = new HashMap<String,Object>();
			dynamics.put("status","0");
			this.redisCache.setRegisterInfo(this.mac,1,null,this.jedis);
			this.redisCache.setDeviceReport(this.mac,1,null,this.jedis);
			this.redisCache.setDeviceDynamic(this.mac,dynamics,this.jedis);
			this.redisCache.setDevAlarm(this.mac,null,this.jedis);
			this.redisCache.setDevAlarmNotice(this.mac,null,this.jedis);
			this.redisCache.deleteTopgraphy(this.uid,this.gid,this.jedis);
			this.redisCache.clearDeviceAuthWithPolicy(this.mac,this.jedis);
			log.debug("Handling device["+this.mac+"] binded to user["+this.uid+"] success");
		}
		jdbcUtils.setAutoCommit(true);
		return rs;
	}
	
	private Map<String,String> getAddDeviceData(){
		Map<String,String> data = new HashMap<String,String>();
		data.put("dev_mac",this.mac);
		data.put("user_id",this.uid);
		data.put("name", this.devName);
		data.put("type","");
		data.put("group_id",this.gid);
		data.put("resource",this.resource);
		data.put("bind",this.bindCode);
		data.put("bind_status",this.bindStatus);
		data.put("dev_username",this.devUsername);
		data.put("weblogin_pwd",this.devPassword);
		data.put("created_at",this.time);
		data.put("updated_at",this.time);
		return data;
	}
	
	private Map<String,String> getUpdateDeviceData(){
		Map<String,String> data = new HashMap<String,String>();
		data.put("user_id",this.uid);
		data.put("name", this.devName);
		data.put("group_id",this.gid);
		data.put("resource",this.resource);
		data.put("bind",this.bindCode);
		data.put("bind_status",this.bindStatus);
		data.put("dev_username",this.devUsername);
		data.put("weblogin_pwd",this.devPassword);
		data.put("updated_at",this.time);
		return data;
	}
}
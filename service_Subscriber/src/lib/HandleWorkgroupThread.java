package lib;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import conf.mysqlConf;
import conf.publicConf;
import conf.redisConf;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

//队列数据处理
public class HandleWorkgroupThread extends Thread{
	private static final Log log = LogFactory.getLog(HandleWorkgroupThread.class);
	private Jedis jedis = null;
	private GroupDevicesPush ws;
	public HandleWorkgroupThread(){
		this.jedis=helperRedis.getJedis();
	}
	
	@Override
	public void run() {
		int port = publicConf.ws_port;
		ws = new GroupDevicesPush(new InetSocketAddress(port));
		ws.start();
		
		while(true){
			try{
				List<Map<String,String>> datas = this.getDatas(50);

				if(datas.isEmpty()){
					Thread.sleep(60000);
				}else{
					this.handleData(datas);
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}  	        
	}
	
	private List<Map<String,String>> getDatas(int num){
		List<Map<String,String>> result = new ArrayList<Map<String,String>>();
		
		Pipeline p = this.jedis.pipelined();
		for(int i=0;i<num;i++){
			p.rpop(redisConf.GROUP_DEVICES_QUEUE);
		}
		List<Object> datas = p.syncAndReturnAll();
		for(int j=0;j<datas.size();j++){
			Object data = datas.get(j);
			Map<String,String> groups = new HashMap<String,String>();
			if(data != null){
				Map<String,String> tmp = JSON.parseObject(data.toString(),new TypeReference<Map<String,String>>(){});
				for(Map.Entry<String,String> entry : tmp.entrySet()){
					groups.put(entry.getKey(), entry.getValue());
				}
				result.add(groups);
			}
		}
		return result;
	}
	
	private void handleData(List<Map<String,String>> datas){
		if(!datas.isEmpty()){
			Map<String,Map<String,String>> userIDs = new HashMap<String,Map<String,String>>();
			List<Object> paramsQuery = new ArrayList<Object>();
			JdbcUtils jdbcUtils = new JdbcUtils();
			jdbcUtils.getConnection();
			try{
				jdbcUtils.setAutoCommit(false);
				for(int i=0;i<datas.size();i++){
					Map<String,String> data = datas.get(i);
					String gid = data.get("gid");
					String sql_find = "select count(`id`) as device_nums from "+mysqlConf.prefix+"device where `pid`='' and `user_id` != 0 and `group_id`="+gid;
					Map<String,Object> groupDevices = jdbcUtils.findSimpleResult(sql_find,paramsQuery);
					String sql = "update "+mysqlConf.prefix+"group set `device_nums`="+groupDevices.get("device_nums").toString()+" where `id`="+gid;
					jdbcUtils.updateByPreparedStatement(sql,paramsQuery);
					String sql_user_find = "select `user_id` from "+mysqlConf.prefix+"user_group where `group_id`="+gid;
					List<Map<String,Object>> users = jdbcUtils.findModeResult(sql_user_find, paramsQuery);
					if(!users.isEmpty()){
						for(int j=0;j<users.size();j++){
							String userID = users.get(j).get("user_id").toString();
							Map<String,String> addUserIDs = new HashMap<String,String>();
							if(userIDs.get(userID) == null){
								addUserIDs.put(gid, groupDevices.get("device_nums").toString());
							}else{
								addUserIDs = userIDs.get(userID);
								addUserIDs.put(gid, groupDevices.get("device_nums").toString());
							}
							userIDs.put(userID, addUserIDs);
						}
					}
				}
			}catch(Exception e){
				log.error(e.getMessage());
				jdbcUtils.rollback();
			}
			jdbcUtils.commit();
			jdbcUtils.setAutoCommit(true);
			jdbcUtils.releaseConn();
			//发设备数量更改通知消息
			if(!userIDs.isEmpty()){
				ws.sendToUsers(userIDs);
			}
		}
	}
}
package lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import conf.mysqlConf;
import conf.publicConf;
import conf.redisConf;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

//队列数据处理
public class MessagePushThread implements Runnable {
	private static final Log log = LogFactory.getLog(MessagePushThread.class);
	private MyWebSocket websocket;
	private Jedis jedis;
	
	public MessagePushThread(MyWebSocket websocket,Jedis jedis){
		this.websocket = websocket;
		this.jedis = jedis;
	}
	
	@Override
	public void run() {
		log.fatal("Notice push server started by websocket");
		while(true){
			try{
				Thread.sleep(publicConf.ws_notice_interval * 1000);
				Map<String,String> datas = this.getDatas(50);
				if(!datas.isEmpty()){
					Iterator<Map.Entry<String,String>> entries = datas.entrySet().iterator();
					while(entries.hasNext()){
						Map.Entry<String,String> entry = entries.next();
						String uid = entry.getKey();
						String message = "{\"nums\":\""+entry.getValue()+"\"}";
						this.websocket.sendToUser(uid,"notice", message);
					}
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
	}
	
	private Map<String,String> getDatas(int num){
		Map<String,String> results = new HashMap<String,String>();
		List<String> uids = this.websocket.getUidsForNoticeFromConns();
		
		if(!uids.isEmpty()){
			JdbcUtils jdbcUtils = new JdbcUtils();
			jdbcUtils.getConnection();
			
			Pipeline p = this.jedis.pipelined();
			for(int i=0;i<uids.size();i++){
				p.hget(redisConf.NOTICE_STATICS+uids.get(i),"unreads");
			}
			List<Object> noticesCache = p.syncAndReturnAll();
			List<String> residue = new ArrayList<String>();
			for(int j=0;j<noticesCache.size();j++){
				if(noticesCache.get(j) == null){
					residue.add(uids.get(j));
				}else{
					results.put(uids.get(j),noticesCache.get(j).toString());
				}
			}
			//没有缓存处理
			if(!residue.isEmpty()){
				for(int k=0;k<residue.size();k++){
					String uid = residue.get(k);
					List<Object> paramQuery = new ArrayList<Object>();
					paramQuery.add(uid);
					paramQuery.add(uid);
					String total_sql = "select count(`id`) as nums from "+mysqlConf.prefix+"command where (`status`='3' and `user_id` in(select `id` from "+mysqlConf.prefix+"users where `id`=? or `pid`=?))";
					String read_sql = total_sql + " and exists(select * from "+mysqlConf.prefix+"message_read where "+mysqlConf.prefix+"command.`id`="+mysqlConf.prefix+"message_read.`comm_id`)";
					try{
						Map<String,Object> result1 = jdbcUtils.findSimpleResult(total_sql, paramQuery);
						Map<String,Object> result2 = jdbcUtils.findSimpleResult(read_sql, paramQuery);
						String all = result1.get("nums").toString();
						String reads = result2.get("nums").toString();
						String unreads = String.valueOf(Integer.parseInt(all) - Integer.parseInt(reads));
						Map<String,String> tmp = new HashMap<String,String>();
						tmp.put("all", all);
						tmp.put("reads", reads);
						tmp.put("unreads", unreads);
						this.jedis.hmset(redisConf.NOTICE_STATICS+uid,tmp);
						results.put(uid,unreads);
					}catch(Exception e){
						
					}
				}
			}
			jdbcUtils.releaseConn();
		}
		return results;
	}
	
	@SuppressWarnings("unused")
	private void handleData(List<Map<String,String>> datas){
		if(!datas.isEmpty()){
			for(int i=0;i<datas.size();i++){
				Map<String,String> data = datas.get(i);
				this.websocket.sendToUser(data.get("uid"),"notice","{\"nums\":\""+data.get("nums")+"\"}");
			}
		}
	}

}
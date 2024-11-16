package lib;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import conf.redisConf;
import redis.clients.jedis.Jedis;
import data.Device;

public class OnlineCollectExecutor extends ExcutorTask{
	private static final Log log = LogFactory.getLog(OnlineCollectExecutor.class);
	private SimpleDateFormat dateformat;
	private String name;
	
	public OnlineCollectExecutor(String name){
		this.name = name;
		if(this.name.equals("day")){
			this.dateformat = new SimpleDateFormat("YYYY-MM-dd");
		}else if(this.name.equals("month")){
			this.dateformat = new SimpleDateFormat("YYYY-MM");
		}else if(this.name.equals("year")){
			this.dateformat = new SimpleDateFormat("YYYY");
		}
	}
	@Override
	public void run(){
		log.fatal(this.getName() + " begin");
		Date ldate = new Date();
 		Long time = ldate.getTime()/1000;
 		
 		final JdbcUtils jdbcUtils = new JdbcUtils();
		jdbcUtils.getConnection();
		Jedis jedis=helperRedis.getJedis();
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
    	//List<Object> paramsQuery = new ArrayList<Object>();
		try{
			results = this.getDatas(jdbcUtils,jedis);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		jdbcUtils.releaseConn();
		//存入采样值
		if(!results.isEmpty()){
			String str = "{";
			for(int i=0;i<results.size();i++){
				Map<String,Object>result = results.get(i);
				str += "\""+result.get("type").toString()+"\":\""+result.get("nums").toString()+"\",";
			}
			str = str.substring(0,str.length()-1)+"}";
			Map<String,String> datas = new HashMap<String,String>();
			datas.put(time.toString(), str);
			String key = redisConf.DEVICE_ONLINE_COLLECT+"day:"+this.dateformat.format(ldate);
			jedis.hmset(key,datas);
			jedis.expire(key,90000);
		}
		helperRedis.returnResource(jedis);
		log.fatal(this.getName() + " end");
	}
	
	private List<Map<String,Object>> getDatas(JdbcUtils jdbcUtils,Jedis jedis){
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		List<Object> paramsQuery = new ArrayList<Object>();
		
		/*Date ldate = new Date();
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        paramsQuery.add(calendar.getTime().getTime()/1000);
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        paramsQuery.add(calendar.getTime().getTime()/1000);*/
		
		try{
			List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>();
			datas = jdbcUtils.findModeResult(Device.sql_all_device_get_no_day, paramsQuery);
			if(!datas.isEmpty()){
				for(int i=0;i<datas.size();i++){
					Map<String,Object> data = datas.get(i);
					//String status = jedis.hget(redisConf.DEVICE_DYNAMIC+data.get("dev_mac").toString(),"status");
					String devDynamicStr = jedis.get(redisConf.DEVICE_DYNAMIC + data.get("dev_mac").toString());
					if(devDynamicStr != null){
						Map<String,String> devDynamicObj = JSON.parseObject(devDynamicStr,new TypeReference<Map<String,String>>(){});
						String status = devDynamicObj.get("status");			
						if(status.equals("1")){
							int index = this.findFromList(results,"type",data.get("type").toString());
							if(index >= 0){//已经存在
								Map<String,Object> find = results.get(index);
								find.put("nums",Integer.parseInt(find.get("nums").toString())+1);
								results.remove(index);
								results.add(find);
							}else{
								Map<String,Object> find = new HashMap<String,Object>();
								find.put("type",data.get("type").toString());
								find.put("nums", "1");
								results.add(find);
							}
						}
					}
				}
			}
			
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return results;
	}
	
	private int findFromList(List<Map<String,Object>> list,String key,String keyword){
		int index=-1;
		int i=0;
		if(!list.isEmpty()){
			for(i=0;i<list.size();i++){
				Map<String,Object> tmp = list.get(i);
				if(tmp.get(key).toString().equals(keyword)){
					index = i;
					break;
				}
			}
		}
		return index;
	}
	
	public String getName(){
		return "online-collect-"+this.name;
	}
}
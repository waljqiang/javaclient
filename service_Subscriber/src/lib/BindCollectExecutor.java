package lib;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import conf.redisConf;
import redis.clients.jedis.Jedis;
import data.Device;

public class BindCollectExecutor extends ExcutorTask{
	private static final Log log = LogFactory.getLog(BindCollectExecutor.class);
	private SimpleDateFormat dateformat;
	private String name;
	
	public BindCollectExecutor(String name){
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
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	
    	Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        paramsQuery.add(calendar.getTime().getTime()/1000);
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        paramsQuery.add(calendar.getTime().getTime()/1000);
 	
		try{
			results = jdbcUtils.findModeResult(Device.sql_count_bind, paramsQuery);
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
			Jedis jedis=helperRedis.getJedis();
			String key = redisConf.DEVICE_BIND_COLLECT+"day:"+this.dateformat.format(ldate);
			jedis.hmset(key,datas);
			jedis.expire(key,90000);
			helperRedis.returnResource(jedis);
		}
		log.fatal(this.getName() + " end");
	}
	
	public String getName(){
		return "bind-collect-"+this.name;
	}
}
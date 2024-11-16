package lib;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import conf.redisConf;
import redis.clients.jedis.Jedis;
import data.Device;

public class OnlineStatisticExecutor extends ExcutorTask{
	private static final Log log = LogFactory.getLog(OnlineStatisticExecutor.class);
	private String name;
	
	public OnlineStatisticExecutor(String name){
		this.name = name;
	}
	@Override
	public void run(){
		Date ldate = new Date();
		if(this.name.equals("day")){//按天统计
			log.fatal("statistic onlines-day begin");
			this.handleStatisticDay(ldate);
			log.fatal("statistic onlines-day end");
		}else if(this.name.equals("month")){//按月统计
			Calendar c = Calendar.getInstance();
			c.setTime(ldate);
            int day = c.get(Calendar.DAY_OF_MONTH);
            if(day == 1){
            	log.fatal("statistic onlines-month begin");
            	this.handleStatisticMonth(ldate);
            	log.fatal("statistic onlines-month end");
            }
		}else if(this.name.equals("year")){//按年统计
			Calendar c = Calendar.getInstance();
			c.setTime(ldate);
            int day = c.get(Calendar.DAY_OF_YEAR);
            if(day == 1){
            	log.fatal("statistic onlines-year begin");
            	this.handleStatisticYear(ldate);
            	log.fatal("statistic onlines-year end");
            }
		}else if(this.name.equals("quarter")){//按季度统计
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
			String quarterDate = dateFormat.format(ldate);
			if(quarterDate.equals("01-01") || quarterDate.equals("04-01") || quarterDate.equals("07-01") || quarterDate.equals("10-01")){
				log.fatal("statistic onlines-quarter begin");
				this.handleStatisticQuarter(ldate);
				log.fatal("statistic onlines-quarter end");
			}
		}else if(this.name.equals("week")){//按周统计
			Calendar c = Calendar.getInstance();
			c.setTime(ldate);
            int day = c.get(Calendar.DAY_OF_WEEK);
            if(day == Calendar.MONDAY){
            	log.fatal("statistic binds-week begin");
            	this.handleStatisticWeek(ldate);
            	log.fatal("statistic binds-week end");
            }
		}else{
			
		}
	}
	
	private void handleStatisticDay(Date ldate){
		Map<String,String> datas = new HashMap<String,String>();
		Long time = ldate.getTime()/1000;
		String day = Helper.getDayStr(ldate,-1);//当天统计昨天的
		Jedis jedis=helperRedis.getJedis();	
		datas = jedis.hgetAll(redisConf.DEVICE_ONLINE_COLLECT+"day:"+day);
		log.fatal("online collect data[" + datas.toString()+"]");
		//取采样平均值
		if(datas.size() != 0){
			JdbcUtils jdbcUtils = new JdbcUtils();
			jdbcUtils.getConnection();
			Map<String,List<String>> counts = new HashMap<String,List<String>>();
			Iterator<Map.Entry<String,String>> entries = datas.entrySet().iterator();
			while(entries.hasNext()){
				Map.Entry<String,String> entry = entries.next();
				Map<String,String> data = JSON.parseObject(entry.getValue(),new TypeReference<Map<String,String>>(){});
				Iterator<Map.Entry<String, String>> _entries = data.entrySet().iterator();
				while(_entries.hasNext()){
					Map.Entry<String, String> _entry = _entries.next();
					List<String> tmp = new ArrayList<String>();
					if(counts.get(_entry.getKey()) == null){
						tmp.add(_entry.getValue());
					}else{
						tmp = counts.get(_entry.getKey());
						tmp.add(_entry.getValue());
					}
					counts.put(_entry.getKey(), tmp);
				}
			}

			//记录统计数据
			String sql = Device.sql_count_online_day_add;
			List<Object> paramsQuery = new ArrayList<Object>();
			Iterator<Map.Entry<String,List<String>>> dentries = counts.entrySet().iterator();
			while(dentries.hasNext()){
				Map.Entry<String,List<String>> dentry = dentries.next();
				sql += "(?,?,?,?,?,?),";
				paramsQuery.add(day);
				paramsQuery.add(dentry.getKey());
				paramsQuery.add(Helper.getAverageInt(dentry.getValue()));
				paramsQuery.add(1);
				paramsQuery.add(time);
				paramsQuery.add(time);
			}
			sql = sql.substring(0,sql.length()-1);
			
			try{
				jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
				//统计完成删除redis
				jedis.del(redisConf.DEVICE_ONLINE_COLLECT+"day:"+day);
			}catch(Exception e){
				log.error("handleOnlinesStatisticDay:"+e.getMessage());
			}
			jdbcUtils.releaseConn();
		}
		helperRedis.returnResource(jedis);
	}
	
	private void handleStatisticMonth(Date ldate){
		Long time = ldate.getTime()/1000;
		String preStartMonthStr = Helper.getStartMonthStr(ldate,-1);
		String preEndMonthStr = Helper.getEndMonthStr(ldate,-1);
		String preMonthStr = Helper.getMonthStr(ldate,-1);
		//获取统计数据
		List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(preStartMonthStr);
		paramsQuery.add(preEndMonthStr);
		JdbcUtils jdbcUtils = new JdbcUtils();
		jdbcUtils.getConnection();
		try{
			List<Map<String,Object>> datas = jdbcUtils.findModeResult(Device.sql_count_online_day_get, paramsQuery);
			if(!datas.isEmpty()){//统计数据
				String sql = Device.sql_count_online_month_add;
				List<Object> paramsQuery1 = new ArrayList<Object>();
				for(int i=0;i<datas.size();i++){
					Map<String,Object> data = datas.get(i);
					sql += "(?,?,?,?,?,?),";
					paramsQuery1.add(preMonthStr);
					paramsQuery1.add(data.get("type").toString());
					paramsQuery1.add(data.get("nums").toString());
					paramsQuery1.add(1);
					paramsQuery1.add(time);
					paramsQuery1.add(time);
				}
				sql = sql.substring(0,sql.length()-1);
				jdbcUtils.updateByPreparedStatement(sql, paramsQuery1);
			}
		}catch(Exception e){
			log.error("handleOnlinesStatisticMonth:"+e.getMessage());
		}
		jdbcUtils.releaseConn();
	}
	
	private void handleStatisticYear(Date ldate){
		Long time = ldate.getTime()/1000;
		String preStartYearStr = Helper.getStartYearStr(ldate,-1);
		String preEndYearStr = Helper.getEndYearStr(ldate, -1);
		String preYearStr = Helper.getYearStr(ldate,-1);
		//获取统计数据
		List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(preStartYearStr);
		paramsQuery.add(preEndYearStr);
		JdbcUtils jdbcUtils = new JdbcUtils();
		jdbcUtils.getConnection();
		try{
			List<Map<String,Object>> datas = jdbcUtils.findModeResult(Device.sql_count_online_month_get, paramsQuery);
			if(!datas.isEmpty()){//统计数据
				String sql = Device.sql_count_online_year_add;
				List<Object> paramsQuery1 = new ArrayList<Object>();
				for(int i=0;i<datas.size();i++){
					Map<String,Object> data = datas.get(i);
					sql += "(?,?,?,?,?,?),";
					paramsQuery1.add(preYearStr);
					paramsQuery1.add(data.get("type").toString());
					paramsQuery1.add(data.get("nums").toString());
					paramsQuery1.add(1);
					paramsQuery1.add(time);
					paramsQuery1.add(time);
				}
				sql = sql.substring(0,sql.length()-1);
				jdbcUtils.updateByPreparedStatement(sql, paramsQuery1);
			}
		}catch(Exception e){
			log.error("handleOnlinesStatisticYear:"+e.getMessage());
		}
		jdbcUtils.releaseConn();
	}
	
	private void handleStatisticWeek(Date ldate){
		Long time = ldate.getTime()/1000;
		String preStartWeekStr = Helper.getStartWeekStr(ldate,-1);
		String preEndWeekStr = Helper.getEndWeekStr(ldate,-1);
		String preWeekStr = Helper.getWeekStr(ldate,-1,true);
		//获取统计数据
		List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(preStartWeekStr);
		paramsQuery.add(preEndWeekStr);

		JdbcUtils jdbcUtils = new JdbcUtils();
		jdbcUtils.getConnection();
		try{
			List<Map<String,Object>> datas = jdbcUtils.findModeResult(Device.sql_count_online_day_get, paramsQuery);
			if(!datas.isEmpty()){//统计数据
				String sql = Device.sql_count_online_week_add;
				List<Object> paramsQuery1 = new ArrayList<Object>();
				for(int i=0;i<datas.size();i++){
					Map<String,Object> data = datas.get(i);
					sql += "(?,?,?,?,?,?),";
					paramsQuery1.add(preWeekStr);
					paramsQuery1.add(data.get("type").toString());
					paramsQuery1.add(data.get("nums").toString());
					paramsQuery1.add(1);
					paramsQuery1.add(time);
					paramsQuery1.add(time);
				}
				sql = sql.substring(0,sql.length()-1);
				jdbcUtils.updateByPreparedStatement(sql, paramsQuery1);
			}
		}catch(Exception e){
			log.error("handleOnlinesStatisticWeek:"+e.getMessage());
		}
		jdbcUtils.releaseConn();
	}
	
	private void handleStatisticQuarter(Date ldate){
		Long time = ldate.getTime()/1000;
		String preStartQuarterStr = Helper.getPreStartQuarterStr(ldate);
		String preEndQuarterStr = Helper.getPreEndQuarterStr(ldate);
		String preQuarterStr = Helper.getQuarterStr(ldate,-1,true);
		//获取统计数据
		List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(preStartQuarterStr.substring(0,7));
		paramsQuery.add(preEndQuarterStr.substring(0,7));

		JdbcUtils jdbcUtils = new JdbcUtils();
		jdbcUtils.getConnection();
		try{
			List<Map<String,Object>> datas = jdbcUtils.findModeResult(Device.sql_count_online_month_get, paramsQuery);
			if(!datas.isEmpty()){//统计数据
				String sql = Device.sql_count_online_quarter_add;
				List<Object> paramsQuery1 = new ArrayList<Object>();
				for(int i=0;i<datas.size();i++){
					Map<String,Object> data = datas.get(i);
					sql += "(?,?,?,?,?,?),";
					paramsQuery1.add(preQuarterStr);
					paramsQuery1.add(data.get("type").toString());
					paramsQuery1.add(data.get("nums").toString());
					paramsQuery1.add(1);
					paramsQuery1.add(time);
					paramsQuery1.add(time);
				}
				sql = sql.substring(0,sql.length()-1);
				jdbcUtils.updateByPreparedStatement(sql, paramsQuery1);
			}
		}catch(Exception e){
			log.error("handleOnlinesStatisticQuarter:"+e.getMessage());
		}
		jdbcUtils.releaseConn();
	}
	
	public String getName(){
		return "online-statistic-"+this.name;
	}
}
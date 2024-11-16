package conf;

import java.util.HashMap;
import java.util.Map;

public class publicConf extends config{
    public static final String system_timezone = config.getIni("TIMEZONE","GMT+08");
	
	public static final int ithreadCount = Integer.parseInt(config.getIni("HANDLE_CHILD_THREAD","50"));
	public static final int cache_registerttl = 3600*24*30;//1个月过期
	public static final int up_number = Integer.parseInt(config.getIni("UP_NUMBER","100"));
	public static final int upgradeexpire=Integer.parseInt(config.getIni("UPGRADEEXPIRE","3600"));
	
	public static final int iplocation_interval = Integer.parseInt(config.getIni("IPLOCATION_INTERVAL","600000"));//没有数据休眠10分钟
	public static final String iplocation_china = config.getIni("APP_LANG","zh") == "zh" ? "中国" : "China";
	public static final String iplocation_baiduapi = config.getIni("LOCATION_BAIDU_API","http://api.map.baidu.com/location/ip?ak=4itF2ygdKkIfshFlQggs7DZA&ip=%s&coor=gcj02");//http://lbsyun.baidu.com/index.php?title=webapi/ip-api
	public static final String iplocation_ipapi = config.getIni("LOCATION_IP_API","http://ip-api.com/json/%s?lang=%s&fields=status,message,continent,continentCode,country,countryCode,region,regionName,city,district,zip,lat,lon,timezone,query");//https://ip-api.com/docs

	public static final String location_task_time = config.getIni("LOCATION_TASK_TIME","yyyy-MM-dd 02:00:00");//每天2点开始执行
	public static final long location_interval = Long.parseLong(config.getIni("LOCATION_INTERVAL","86400"));//隔一天执行一次
	public static final int location_task_num = Integer.parseInt(config.getIni("LOCATION_TASK_NUM","10"));
	public static final String location_api = "http://api.tianditu.gov.cn/geocoder?postStr=%s&type=geocode&tk="+config.getIni("TIANDITU_KEY","1d9aed09a8dcd5f657603e3bb5881675");
	public static final String client_statics_task_time = config.getIni("CLIENT_STATICS_TASK_TIME","yyyy-MM-dd 00:00:00");//每天0点开始
	public static final long client_interval = Long.parseLong(config.getIni("CLIENT_INTERVAL","86400"));//隔一天执行一次
	public static final int client_statics_task_num = Integer.parseInt(config.getIni("CLIENT_STATICS_TASK_NUM","100"));
	public static final int client_statics_retry = Integer.parseInt(config.getIni("CLIENT_STATICS_RETRY","3"));
	
	public static final int handle_jedis_num = Integer.parseInt(config.getIni("HANDLE_JEDIS_NUM","10"));
	
	public static final int upinfo_pmax = Integer.parseInt(config.getIni("UPINFO_PMAX","100"));//一分钟内上报最大次数
	public static final int upinfo_lock = Integer.parseInt(config.getIni("UPINFO_LOCK","600"));//锁定时间,单位秒
	
	public static final String cloudnetlot_api_base = config.getIni("CLOUDNETLOT_API_BASE","http://127.0.0.1/cloudnetlot");
	
	public static final int ws_port = Integer.parseInt(config.getIni("WS_PORT","7778"));
	public static final int ws_notice_interval = Integer.parseInt(config.getIni("WS_NOTICE_INTERVAL","120"));
	
	public static final int alarm_recovery_time = Integer.parseInt(config.getIni("ALARM_RECOVERY_TIME","600"));
	
	public static final String binds_collect_task_time = config.getIni("BIND_COLLECT_TASK_TIME","yyyy-MM-dd HH:00:00");
	public static final long binds_collect_interval = Long.parseLong(config.getIni("BIND_COLLECT_INTERVAL","3600"));//一个小时运行一次
	public static final String binds_day_time = config.getIni("BINDS_DAY_TIME","yyyy-MM-dd 00:00:00");//每天00:00:00执行
	public static final long binds_day_interval = Long.parseLong(config.getIni("BINDS_DAY_INTERVAL","86400"));
	public static final String binds_week_time = config.getIni("BINDS_WEEK_TIME","yyyy-MM-dd 00:05:00");//按周,每天00:00:00执行,任务中判断是否周一
	public static final long binds_week_interval = Long.parseLong(config.getIni("BINDS_WEEK_INTERVAL","86400"));
	public static final String binds_month_time = config.getIni("BINDS_MONTH_TIME","yyyy-MM-01 00:05:00");//按月,当月1号00:00:00执行,每天执行，任务中判断是否为月初
	public static final long binds_month_interval = Long.parseLong(config.getIni("BINDS_MONTH_INTERVAL","86400"));
	public static final String binds_quarter_time = config.getIni("BINDS_QUARTER_TIME","yyyy-MM-01 00:10:00");//按季度,当月1号00:00:00执行,每天执行,任务中判断是否为季度首日
	public static final long binds_quarter_interval = Long.parseLong(config.getIni("BINDS_quarter_INTERVAL","86400"));
	public static final String binds_year_time = config.getIni("BINDS_YEAR_TIME","yyyy-01-01 00:10:00");//按年,当年01-01 00:00:00,每天执行,任务中判断是否为年首日
	public static final long binds_year_interval = Long.parseLong(config.getIni("BINDS_YEAR_INTERVAL","86400"));
	
	public static final String onlines_collect_task_time = config.getIni("ONLINE_COLLECT_TASK_TIME","yyyy-MM-dd HH:30:00");
	public static final long onlines_collect_interval = Long.parseLong(config.getIni("ONLINE_COLLECT_INTERVAL","3600"));//一个小时运行一次
	public static final String onlines_day_time = config.getIni("ONLINES_DAY_TIME","yyyy-MM-dd 00:00:00");//每天00:00:00执行
	public static final long onlines_day_interval = Long.parseLong(config.getIni("ONLINES_DAY_INTERVAL","86400"));
	public static final String onlines_week_time = config.getIni("ONLINES_WEEK_TIME","yyyy-MM-dd 00:05:00");//按周,每天00:00:00执行,任务中判断是否周一
	public static final long onlines_week_interval = Long.parseLong(config.getIni("ONLINES_WEEK_INTERVAL","86400"));
	public static final String onlines_month_time = config.getIni("ONLINES_MONTH_TIME","yyyy-MM-01 00:05:00");//按月,当月1号00:00:00执行,每天执行，任务中判断是否为月初
	public static final long onlines_month_interval = Long.parseLong(config.getIni("ONLINES_MONTH_INTERVAL","86400"));
	public static final String onlines_quarter_time = config.getIni("ONLINES_QUARTER_TIME","yyyy-MM-01 00:10:00");//按季度,当月1号00:00:00执行,每天执行,任务中判断是否为季度首日
	public static final long onlines_quarter_interval = Long.parseLong(config.getIni("ONLINES_quarter_INTERVAL","86400"));
	public static final String onlines_year_time = config.getIni("ONLINES_YEAR_TIME","yyyy-01-01 00:10:00");//按年,当年01-01 00:00:00,每天执行,任务中判断是否为年首日
	public static final long onlines_year_interval = Long.parseLong(config.getIni("ONLINES_YEAR_INTERVAL","86400"));
	
	public static final Map<String,Integer> log_levels;
	static{
		log_levels = new HashMap<String,Integer>();
		log_levels.put("TRACE",1);
		log_levels.put("DEBUG",2);
		log_levels.put("INFO",3);
		log_levels.put("WARN",4);
		log_levels.put("ERROR",5);
		log_levels.put("FATAL",6);
	}
	
	public static final int log_level = log_levels.get(config.getIni("LOG_LEVEL","DEBUG").toUpperCase());
	public static final Boolean lfatal = log_level <= log_levels.get("FATAL");
	public static final Boolean lerror = log_level <= log_levels.get("ERROR");
	public static final Boolean lwarm = log_level <= log_levels.get("WARN");
	public static final Boolean linfo = log_level <= log_levels.get("INFO");
	public static final Boolean ldebug = log_level <= log_levels.get("DEBUG");
	public static final Boolean ltrace = log_level <= log_levels.get("TRACE");
    
    public static final Map<String,Map<String,String>> defaultPortal;
    static
    {
    	defaultPortal = new HashMap<String,Map<String,String>>();
    	Map<String,String> pc = new HashMap<String,String>();
    	Map<String,String> mobile = new HashMap<String,String>();
    	pc.put("before",publicConf.cloudnetlot_api_base + "/backend/storage/files/portal/default/pc/before.html");
    	pc.put("process",publicConf.cloudnetlot_api_base + "/backend/storage/files/portal/default/pc/process.html");
    	pc.put("after",publicConf.cloudnetlot_api_base + "/backend/storage/files/portal/default/pc/after.html");
    	
    	mobile.put("before",publicConf.cloudnetlot_api_base + "/backend/storage/files/portal/default/mobile/before.html");
    	mobile.put("process",publicConf.cloudnetlot_api_base + "/backend/storage/files/portal/default/mobile/process.html");
    	mobile.put("after",publicConf.cloudnetlot_api_base + "/backend/storage/files/portal/default/mobile/after.html");
    	
    	defaultPortal.put("pc",pc);
    	defaultPortal.put("mobile", mobile);
    }
    
    public static final Map<String,String> product;
    static{
    	product = new HashMap<String,String>();
    	product.put("prtxaejlnqrtxrlqvqabpdmrw","1");
    	product.put("prtdaxkypywtbwvarlgepmqvr","2");
    	product.put("prtxaejlnqrtdezxbyampdmrw","3");
    	product.put("prtvzrlbplxtbroqmryzpygkx","4");
    }
    
    public static final long queue_max = 1000L;
    
}
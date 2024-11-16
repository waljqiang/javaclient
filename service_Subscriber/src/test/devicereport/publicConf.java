package test.devicereport;

import conf.config;

public class publicConf extends config{
	public static final String USER_ACCOUNT = config.getIni("REPORT_USER_ACCOUNT","yuncorelot");
	public static final String USER_PASSWORD = config.getIni("REPORT_USER_PASSWORD","123456");
	public static final String GID = config.getIni("REPORT_GID","cnlwmnaxqkpembgjvmlyonzvrwdz");
	
	public static final String APPID = config.getIni("REPORT_APPID","325986ac102df6261ca5fbfbc2aa3458");
	public static final String SECRET = config.getIni("SECRET","fbcb84MxLNDndnWCWZ08TXIj9ePbBp8lHVp9rBXy");
	
	public static final String PRODUCT_AP_PRTID = config.getIni("REPORT_PRODUCT_AP_PRTID","prtdaxkypywtbwvarlgepmqvr");
	public static final String PRODUCT_AC_PRTID = config.getIni("REPORT_PRODUCT_AC_PRTID","prtxaejlnqrtxrlqvqabpdmrw");
	public static final String PRODUCT_4G_PRTID = config.getIni("REPORT_PRODUCT_4G_PRTID","prtvzrlbplxtbroqmryzpygkx");
	public static final String PRODUCT_CPE_PRTID = config.getIni("REPORT_PRODUCT_CPE_PRTID","prtxaejlnqrtdezxbyampdmrw");
	
	public static final String CLOUDNETLOT_API_BASE = config.getIni("REPORT_CLOUDNETLOT_API_BASE","http://192.168.111.201:9090/cloudnetlot");
	public static final String CLOUDNETLOT_API_GETCLIENT = CLOUDNETLOT_API_BASE + "/backend/getclient";
	public static final String CLOUDNETLOT_API_GETTOKEN = CLOUDNETLOT_API_BASE + "/backend/auth/token";
	public static final String CLOUDNETLOT_API_BIND = CLOUDNETLOT_API_BASE + "/backend/device/bind";
	
	public static final String MQTT_USERNAME = config.getIni("REPORT_MQTT_USERNAME","cloudnetlot");
	public static final String MQTT_PASSWORD = config.getIni("REPORT_MQTT_PASSWORD","admin@cloudnetlot");
	public static final Short MQTT_KEEPALIVE = Short.parseShort(config.getIni("REPORT_MQTT_KEEPALIVE","0")); 
	
	//模拟AC设备数量
	public static final int DEV_AC_NUM = Integer.parseInt(config.getIni("REPORT_DEV_AC_NUM","1"));
	//AC设备型号
	public static final String DEV_AC_TYPE = config.getIni("REPORT_DEV_AC_TYPE","AC80");
	//模拟AC起始MAC
	public static final String DEV_AC_START_MAC = config.getIni("REPORT_DEV_AC_START_MAC","00:00:00:00:00:01");
	//模拟AC子设备数量
	public static final int DEV_AC_CHILD_NUM = Integer.parseInt(config.getIni("REPORT_DEV_AC_CHILD_NUM","1"));
	
	//上报等待准备时间,单位秒
	public static final int DEV_REPORT_WAIT = Integer.parseInt(config.getIni("REPORT_DEV_REPORT_WAIT","0"));
	
	//最小/大开始上报等待时间,开始上报时间为等待准备时间+最小到最大之间的随机值,单位秒
	public static final int DEV_REPORT_WAIT_MIN = Integer.parseInt(config.getIni("REPORT_DEV_REPORT_WAIT_MIN","0"));
	public static final int DEV_REPORT_WAIT_MAX = Integer.parseInt(config.getIni("REPORT_DEV_REPORT_WAIT_MAX","5"));
	
	//上报频率
	public static final int DEV_REPORT_INTERVAL = Integer.parseInt(config.getIni("REPORT_DEV_REPORT_INTERVAL","300"));
}

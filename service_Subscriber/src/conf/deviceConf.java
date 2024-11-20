package conf;

public class deviceConf extends config{
	public static final String TYPE_INFO_ENCODE = "1";
	public static final String TYPE_INFO_SYSTEM = "2";
	public static final String TYPE_INFO_NETWORK = "3";
	public static final String TYPE_INFO_WIFI = "4";
	public static final String TYPE_INFO_USER = "5";
	public static final String TYPE_INFO_TIME_REBOOT = "6";
	public static final String TYPE_INFO_UPGRADE = "7";
	public static final String TYPE_INFO_BIND = "8";
	public static final String TYPE_INFO_UPINFOFAIL = "9";
	public static final String TYPE_INFO_UP = "10";
	public static final String TYPE_INFO_NTP = "11";
	public static final String TYPE_INFO_UNBIND = "12";
	public static final String TYPE_INFO_CHILDS = "13";
	public static final String TYPE_INFO_DELETE = "14";
	public static final String TYPE_INFO_ASK = "15";
	public static final String TYPE_INFO_REPLY = "16";
	public static final String TYPE_INFO_LOG = "17";
	public static final String TYPE_INFO_ALARM = "18";
	public static final String TYPE_INFO_AUTH_REMOTE = "19";
	public static final String TYPE_INFO_AUTH_LOCAL = "20";	
	public static final String TYPE_INFO_TIME_GROUP = "21";
	public static final String TYPE_INFO_IP_GROUP = "22";
	public static final String TYPE_INFO_RATE_LIMIT = "23";
	public static final String TYPE_INFO_FILTER_URL = "24";
	public static final String TYPE_INFO_FILTER_IPPORT = "25";
	public static final String TYPE_INFO_FILTER_MAC = "26";
	public static final String TYPE_INFO_IPPORT_MAP = "27";
	public static final String TYPE_INFO_DMZ = "28";
	public static final String TYPE_INFO_CLIENT = "29";
	
	public static final String STATUS_OFFLINE = "0";
	public static final String STATUS_ONLINE = "1";
	public static final String STATUS_ALARM = "2";
	
	//是否绑定
    public static final Boolean is_rebind = Boolean.parseBoolean(config.getIni("IS_REBIND","false"));
    
    //备注
    public static final  String mark =  config.getIni("MARK","");
    
    //设备型号
    public static final  String dev_type =  config.getIni("DEV_TYPE","AX820");
    
    public static int encrypt = Integer.parseInt(config.getIni("ENCRYPT","1"));
    
    //是否重新绑定
    public static int rebind = Integer.parseInt(config.getIni("REBIND","0"));
    
    
    //起始mac
    public static String mac_start = config.getIni("MAC_START","66:D1:FF:00:00:01");
    
    //绑定设备数量
    public static int dev_nums = Integer.parseInt(config.getIni("DEV_NUMS","1"));
    
    //云平台组id
	public static final String gid =  config.getIni("G_ID","cnlwmnaxqkpembgjvmlyonzvrwdz");
	
	//产品id
	public static final String prtid =  config.getIni("PRT_ID","prtdaxkypywtbwvarlgepmqvr");
	
	//开发者id
    public static final String app_id = config.getIni("APP_ID","325986ac102df6261ca5fbfbc2aa3458");
	
    //开发者秘钥
    public static final String app_secret = config.getIni("APP_SECRET","fbcb84MxLNDndnWCWZ08TXIj9ePbBp8lHVp9rBXy");
	
	
		
		
}
package conf;



public class publicConf extends config{
    public static final String system_timezone = config.getIni("TIMEZONE","GMT+08");
    
    //云平台接口香港配置
	public static final String cloudnetlot_api_base =  config.getIni("CLOUDNETLOT_API_BASE","http://192.168.33.10/cloudnetlotold");
	public static final String cloudnetlot_api_getclient = cloudnetlot_api_base + "/backend/getclient";
	public static final String cloudnetlot_api_gettoken = cloudnetlot_api_base + "/backend/auth/token";
	public static final String cloudnetlot_api_bind = cloudnetlot_api_base + "/backend/device/bind";
	
	//绑定设备使用云平台账号
	public static final String user_account = config.getIni("USER_ACCOUNT","yuncorelot");
	
	//绑定设备使用云平台密码
	public static final String user_password = config.getIni("USER_PASSWORD","123456");
	
	//上报启动时间
	public static final int report_start_time = Integer.parseInt(config.getIni("REPORT_START_TIME","3000"));
	
	//每台上报时间间隔
	public static final int report_interval = Integer.parseInt(config.getIni("REPORT_INTERVAL","300"));
}
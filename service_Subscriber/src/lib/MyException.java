package lib;

public class MyException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String SUCCESS = "10000";//成功
	public static final String TOKEN_INVALID = "600100104";//token无效
	public static final String TOKEN_EXPIRE = "600100105";//token过期
	public static final String MYSQL_EXEC_ERROR = "600102100";//数据库执行错误
	public static final String YUNLOT_UPINFO_ERROR = "600200101";//yunlot协议错误
	public static final String YUNLOT_REPORT_NO_BIND = "600200102";//请上报不带绑定码的基本数据
	public static final String DEV_NO_CONNECT = "600400174";//设备没有连接云平台
	public static final String BINDCODE_ERROR = "600400177";//绑定码错误
	public static final String DEV_BINDED = "600400178";//设备已绑定其他用户
	public static final String IP_LOCATION_FAIL = "600400179";//IP定位失败
	public static final String DEVICE_NO = "600400185";//设备不存在
	public static final String CLOUD_NOT_BIND="600400250";//云平台还未发起绑定
	public static final String PRT_NO = "600500125";//产品不存在
	public static final String PRT_STATUS_NO_ALLOW = "600500126";//产品状态不允许
	public static final String DATA_HANDLE_FAILURE = "600500127";//数据处理失败
	public static final String DEV_LOCKED = "600000104";//设备被锁定
	public static final String PARAMS_REQUIRED="600400232";//缺少必要的参数
	public static final String USER_NO_EXISTS="600400129";//用户不存在
	public static final String HTTP_REQUEST_NO_EXISTS  = "600000100";//请求不存在
	public static final String RULES_MAX = "600400291";//规则达到最大值
	
	private String code;
	
	public MyException(){
		super();
	}
	
	public MyException(String message){
		super(message);
	}
	
	public MyException(String message,String code){
		super(message);
		this.code = code;
	}
	
	public String getCode(){
		return this.code;
	}
}
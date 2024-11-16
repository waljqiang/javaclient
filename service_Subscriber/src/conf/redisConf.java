package conf;

public class redisConf extends config{
	public static final String host = config.getIni("REDIS_HOST","127.0.0.1");
	public static final int port = Integer.parseInt(config.getIni("REDIS_PORT","6379"));
	public static final String password = config.getIni("REDIS_PASSWORD","");
	public static final int timeOut = Integer.parseInt(config.getIni("REDIS_TIMEOUT","10000"));
	public static final String prefix = config.getIni("REDIS_PREFIX","");
	public static int db = Integer.parseInt(config.getIni("REDIS_DB","0"));
	//可用连接实例的最大数目，默认值为8；
    //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
	public static final int DRIVER_Redis_MAX_ACTIVE = Integer.parseInt(config.getIni("DRIVER_REDIS_MAX_ACTIVE","50"));
	//控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
	public static final int DRIVER_Redis_MAX_IDLE = Integer.parseInt(config.getIni("DRIVER_REDIS_MAX_IDLE","10"));
	//等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
	public static final long DRIVER_Redis_MAX_WAIT = Integer.parseInt(config.getIni("DRIVER_REDIS_MAX_WAIT","100"));
	//在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
	public static final boolean DRIVER_Redis_TEST_ON_BORROW = Boolean.parseBoolean(config.getIni("DRIVER_REDIS_TEST_ON_BORROW","true"));
	//设备上报信息队列
	public static final String DEVICE_QUEUE_DATA = redisConf.prefix + "l:device:queue:data";
	//设备认证询问队列
	public static final String DEVICE_AUTH_QUEUE_DATA = redisConf.prefix + "l:device:auth:queue:data";
	//需要地址定位的设备队列
	public static final String DEVICE_QUEUE_LOCATION = redisConf.prefix + "l:device:queue:location";
	//设备客户端统计队列
	public static final String DEVICE_QUEUE_CLIENTS = redisConf.prefix + "l:device:queue:clients";
	//邮件队列
	public static final String EMAIL_QUEUE = redisConf.prefix + "l:email";
	//设备批量绑定队列
	public static final String DEVICE_BIND_QUEUE_DATA = redisConf.prefix + "l:device:bind:queue:data";
	//注册信息
	public static final String REGISTER = redisConf.prefix + "register:";
	public static final String DEVICE_REPORT = redisConf.prefix + "device:report:";
	//设备动态信息
	public static final String DEVICE_DYNAMIC = redisConf.prefix + "device:dynamic:";
	//设备参数信息
	public static final String DEVICE_PARAMS = redisConf.prefix + "h:device:params:";
	//用户数据旧数据缓存
	public static final String OLDUSERS = redisConf.prefix + "device:old:users:";
	//设备升级状态缓存
	public static final String DEVICE_UPGRADE_STATUS = redisConf.prefix + "h:upgrade:status:";
	//设备上报计数
	public static final String DEV_PER_UPS = redisConf.prefix + "device:perups:";
	//设备锁定
	public static final String DEV_LOCKED = redisConf.prefix + "device:locked:";
	//拓扑图
	public static final String TOPGRAPHY = redisConf.prefix + "topgraphy:";
	/**
	 * 子设备旧数据缓存
	 */
	public static final String OLDCHILDSLIST = redisConf.prefix+"device:old:childs:list:";
	public static final String OLDCHILDS = redisConf.prefix+"device:old:childs:";
	//通知消息队列
	public static final String NOTICE_STATICS = redisConf.prefix + "h:user:notice:statics:";
	// 设备告警设置缓存
	public static final String DEVALARMSET = redisConf.prefix+"dev:alarm:set:";
	//设备告警通知缓存
	public static final String DEVALARMNOTICESET = redisConf.prefix+"dev:alarm:notice:set:";
	//设备上下线记录统计
	public static final String DEVICEONOFFCOUNT = redisConf.prefix+"dev:onoff:count:";
	//设备频繁上下线告警标识
	public static final String DEVICEONOFFALARM = redisConf.prefix + "dev:onoff:alarm:";
	//设备告警时间统计
	public static final String DEVICEALARMTIME = redisConf.prefix+"dev:alarm:time:";
	//子设备数据已经处理条数
	public static final String DEVICECHILDHANDLED = redisConf.prefix+"dev:child:handled:";
	
	//工作组设备数变化处理队列
	public static final String GROUP_DEVICES_QUEUE = redisConf.prefix + "l:group:change:queue";
	//通知消息队列
	public static final String NOTICE_QUEUE = redisConf.prefix + "l:notice:queue";
	
	//设备激活数采样key
	public static final String DEVICE_ONLINE_COLLECT = redisConf.prefix+"h:device:online:collect:";
	//设备在线数采样key
	public static final String DEVICE_BIND_COLLECT = redisConf.prefix+"h:device:bind:collect:";
	
	//设备认证及策略缓存
	public static final String DEVICE_AUTH_POLICY = redisConf.prefix+"dev:auth:policy:";
	//是否允许放行缓存key
	public static final String AUTH_ALLOW = redisConf.prefix + "dev:auth:allow:";
	//
	public static final String AUTH_ROAM_ALLOW = redisConf.prefix+"dev:auth:roam:allow:";
	//会员认证之会员信息缓存
	public static final String AUTH_MEMBER = redisConf.prefix+"dev:auth:mem:";
	//异步处理队列
	public static final String ASYN_HANDLE = redisConf.prefix + "l:asyn:handle";
	//在线升级最新升级包缓存
	public static final String CLOUD_LATEST_PACKAGE = redisConf.prefix + "cloud:package:latest:";
	//设备网卡mac到设备的映射
	public static final String MAC_ETH_TO_DEV = redisConf.prefix + "eth:to:mac:";

}
package conf;

import org.fusesource.mqtt.client.QoS;

public class mqttConf extends config{
	
	//mqtt相关配置
	public static final String address = config.getIni("MQ_ADDRESS","127.0.0.1");
	public static final Integer port = Integer.parseInt(config.getIni("MQ_PORT","1883"));
	public static final String username = config.getIni("MQ_USERNAME","");
	public static final String password = config.getIni("MQ_PASSWORD","");
	public static final boolean clean = Boolean.parseBoolean(config.getIni("MQ_CLEAN","false"));
	public static final Short keepalive = Short.parseShort(config.getIni("MQ_KEEP_ALIVE","300"));
	
	
	//设备上行topic
	public static final String TOPIC_DEV_APP = "$queue/+/+/dev2app";
	//设备下行topic
	public static final String TOPIC_APP_DEV = "+/+/app2dev";
	//auth主题
	public static final String TOPIC_DEV_AUTH = "$queue/+/+/auth";
	//设备上线topic
	public static final String TOPIC_DEV_ONLINE = "$SYS/brokers/+/clients/+/connected";
	//设备下线topic
	public static final String TOPIC_DEV_OFFLINE = "$SYS/brokers/+/clients/+/disconnected";
	//qos
	private static final QoS[] qos = new QoS[]{QoS.AT_MOST_ONCE,QoS.AT_LEAST_ONCE,QoS.EXACTLY_ONCE};
	public static final QoS QoS_send = mqttConf.qos[Integer.parseInt(config.getIni("MQ_SEND_QOS","0"))];
	public static final QoS QoS_sub = mqttConf.qos[Integer.parseInt(config.getIni("MQ_SUB_QOS","0"))];
	public static final QoS QoS_auth = mqttConf.qos[Integer.parseInt(config.getIni("MQ_AUTH_QOS","0"))];
	
	
	
	
	
}
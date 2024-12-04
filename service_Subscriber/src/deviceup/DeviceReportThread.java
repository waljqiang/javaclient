package deviceup;
import java.net.URISyntaxException;
import java.util.Random;
import conf.mqttConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
public abstract class DeviceReportThread extends Thread{
	
	private static final Log logger = LogFactory.getLog(DeviceReportThread.class);
	public  CallbackConnection connection;
	public Device device = null;

	
	public String topic_up;
	

	
	public DeviceReportThread(Device device){
		this.device = device;
	
		//建立mqtt连接
		MQTT mqtt = new MQTT();
	 	try {
			mqtt.setHost(mqttConf.address,mqttConf.port);
		} catch (URISyntaxException e) {
			logger.error(e.getMessage());
		}
	 	mqtt.setUserName(mqttConf.username);
	 	mqtt.setPassword(mqttConf.password);
	 	mqtt.setClientId(this.device.getCltid());
	 	mqtt.setCleanSession(mqttConf.clean);
	 	mqtt.setKeepAlive(mqttConf.keepalive);
	 	
	 	//断连相关配置
	    mqtt.setConnectAttemptsMax(mqttConf.set_conncet_attempts_max);
	    mqtt.setReconnectAttemptsMax(mqttConf.set_reconnect_attempts_max);
	    mqtt.setReconnectDelay(mqttConf.set_reconnect_delay);
	    mqtt.setReconnectDelayMax(mqttConf.set_reconnect_delay_max);
	    mqtt.setReconnectBackOffMultiplier(mqttConf.set_reconnect_back_off_multiplier);
	 	
	    this.connection = mqtt.callbackConnection();
	 	
	    String topic = this.device.getPrtid() + "/" + this.device.getCltid() + "/dev2app";
	 	Topic[] topics = {new Topic(org.fusesource.hawtbuf.Buffer.utf8(topic), QoS.AT_MOST_ONCE)};
	 
	 	this.connection.connect(new Callback<Void>() {
	 
	        public void onSuccess(Void v) {         	
	        	connection.subscribe(topics, new Callback<byte[]>() {
	                public void onSuccess(byte[] value) {
	                	//System.out.println(topics);
	                	logger.debug("The device["+device.getMac()+"] with mqtt subscribe "+topic+" success!");
	                }
	
	                public void onFailure(Throwable value) {
	                	connection.disconnect(null);	
	                    logger.error("The device["+device.getMac()+"] with mqtt subscribe failure,caused:"+value.getMessage());      
	                }
	            });
	
	        }
	
	        public void onFailure(Throwable value) {
	        	logger.error("mqtt dev2app conn failure");
	        }
	    });
	 	
	 	this.connection.listener(new Listener() {
			public void onConnected() {
				logger.fatal("The device["+device.getMac()+"] with mqtt connected");
			}
			public void onDisconnected() {
				logger.fatal("The device["+device.getMac()+"] with mqtt disconnected");
			}
	
		public void onPublish(UTF8Buffer topic, org.fusesource.hawtbuf.Buffer payload, Runnable onComplete){
//				String s_topic = topic.toString();
//				String data = new String(payload.toByteArray());
//				logger.debug("mqtt receive data[" + data + "]from topic[" + s_topic + "]");
			
			}
	
			public void onFailure(Throwable value) {
				//conn.disconnect(null);
				logger.error("receive message failure");
			}
		});
	 	
	}
	
	public void run(){
		
	}
	
	public String getMessage(){
		String message = "{\"header\":"+this.getHeader()+",\"body\":"+this.getBody()+",\"now\":\""+this.getNow()+"\"}";
		return message;
	}
	
	private String getHeader(){
		return "{\"protocol\":\"v1.0\",\"type\":\"1\",\"encode\":\"1\",\"mid\":\""+this.getStr(30,"0123456789abcdefghijklmnopqrstuvw")+"\", \"bind\":\""+device.getBind()+"\"}";
	}
	
   public String getNetwork(String mac) {
        return "{\"lan\":[{\"ip\":\"192.168.10.2\",\"subnet\":\"255.255.255.0\",\"mac\":\"" + mac + "\",\"dhcp_enable\":\"0\",\"dhcp_ip_start\":\"192.168.188.2\",\"dhcp_ip_end\":\"192.168.188.236\",\"gateway\":\"192.168.10.1\",\"dns\":[\"114.114.114.114\"]}]}";
	}
	
	public String getWifi(String mac) {
	    return "{\"total\":\"2\",\"radios\":[" + getRadio2G(mac) + "," + getRadio5G(mac) + "],\"timer\":{\"enable\":\"0\",\"start\":\"\",\"end\":\"\"}}";
	}
	
	public String getRadio2G(String mac) {
	    return "{\"radioid\":\"0\",\"country_code\":\"CN\",\"total\":\"4\",\"radio_type\":\"1\",\"channel\":\"2\",\"channel_config\":\"0\",\"power\":\"0\",\"phymode\":\"27\",\"coveragethreshold\":\"-90\",\"user_isolate\":\"1\",\"frag_threshold\":\"2222\",\"rts_threshold\":\"1111\",\"beacon_interval\":\"333\",\"shortgi\":\"1\",\"max_sta\":\"33\",\"support\":{\"country_code\":[{\"code\":\"CN\",\"channel\":[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\",\"11\",\"12\",\"13\"]},{\"code\":\"US\",\"channel\":[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\",\"11\"]},{\"code\":\"AE\",\"channel\":[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\",\"11\",\"12\",\"13\"]},{\"code\":\"IT\",\"channel\":[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\",\"11\",\"12\",\"13\"]},{\"code\":\"IN\",\"channel\":[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\",\"11\",\"12\",\"13\"]},{\"code\":\"BR\",\"channel\":[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\",\"11\",\"12\",\"13\"]}],\"encode\":[\"1\",\"5\",\"6\"],\"phymode\":[\"8\",\"13\",\"26\",\"27\"],\"max_sta\":\"256\"},\"vap\":[{\"id\":\"0\",\"enable\":\"1\",\"vlan_id\":\"0\",\"bassid\":\"" + mac + "\",\"ssid\":\"wifi20\",\"ssid_hide\":\"0\",\"encode\":\"1\",\"password\":\"12345678\",\"users\":{\"total\":\"0\",\"list\":[]}},{\"id\":\"1\",\"enable\":\"1\",\"vlan_id\":\"0\",\"bassid\":\"00:00:00:00:00:00\",\"ssid\":\"wifi2g1\",\"ssid_hide\":\"0\",\"encode\":\"1\",\"password\":\"12345678\",\"users\":{\"total\":\"0\",\"list\":[]}},{\"id\":\"2\",\"enable\":\"1\",\"vlan_id\":\"0\",\"bassid\":\"00:00:00:00:00:00\",\"ssid\":\"wifi2g2\",\"ssid_hide\":\"0\",\"encode\":\"1\",\"password\":\"12345678\",\"users\":{\"total\":\"0\",\"list\":[]}},{\"id\":\"3\",\"enable\":\"1\",\"vlan_id\":\"0\",\"bassid\":\"00:00:00:00:00:00\",\"ssid\":\"wifi2g3\",\"ssid_hide\":\"0\",\"encode\":\"1\",\"password\":\"12345678\",\"users\":{\"total\":\"0\",\"list\":[]}}]}";
	}
	
	public String getRadio5G(String mac) {
	    return "{\"radioid\":\"1\",\"country_code\":\"CN\",\"total\":\"4\",\"radio_type\":\"2\",\"channel\":\"60\",\"channel_config\":\"0\",\"power\":\"1\",\"phymode\":\"23\",\"coveragethreshold\":\"-90\",\"user_isolate\":\"1\",\"frag_threshold\":\"2222\",\"rts_threshold\":\"1111\",\"beacon_interval\":\"333\",\"shortgi\":\"1\",\"max_sta\":\"44\",\"support\":{\"country_code\":[{\"code\":\"CN\",\"channel\":[\"0\",\"36\",\"40\",\"44\",\"48\",\"52\",\"56\",\"60\",\"64\",\"149\",\"153\",\"157\",\"161\",\"165\"]},{\"code\":\"US\",\"channel\":[\"0\",\"36\",\"40\",\"44\",\"48\",\"52\",\"56\",\"60\",\"64\",\"100\",\"104\",\"108\",\"112\",\"116\",\"120\",\"124\",\"128\",\"132\",\"136\",\"140\",\"149\",\"153\",\"157\",\"161\",\"165\"]},{\"code\":\"AE\",\"channel\":[\"0\",\"36\",\"40\",\"44\",\"48\",\"52\",\"56\",\"60\",\"64\",\"100\",\"104\",\"108\",\"112\",\"116\",\"120\",\"124\",\"128\",\"132\",\"136\",\"140\",\"149\",\"153\",\"157\",\"161\",\"165\"]},{\"code\":\"IT\",\"channel\":[\"0\",\"36\",\"40\",\"44\",\"48\",\"52\",\"56\",\"60\",\"64\",\"100\",\"104\",\"108\",\"112\",\"116\",\"120\",\"124\",\"128\",\"132\",\"136\",\"140\"]},{\"code\":\"IN\",\"channel\":[\"0\",\"36\",\"40\",\"44\",\"48\",\"52\",\"56\",\"60\",\"64\",\"149\",\"153\",\"157\",\"161\",\"165\"]},{\"code\":\"BR\",\"channel\":[\"0\",\"36\",\"40\",\"44\",\"48\",\"52\",\"56\",\"60\",\"64\",\"100\",\"104\",\"108\",\"112\",\"116\",\"120\",\"124\",\"128\",\"132\",\"136\",\"140\",\"149\",\"153\",\"157\",\"161\",\"165\"]}],\"encode\":[\"1\",\"5\",\"6\"],\"phymode\":[\"7\",\"14\",\"15\",\"18\",\"19\",\"21\",\"22\",\"23\"]},\"vap\":[{\"id\":\"0\",\"enable\":\"1\",\"vlan_id\":\"0\",\"bassid\":\"" + mac + "\",\"ssid\":\"wifi50\",\"ssid_hide\":\"0\",\"encode\":\"1\",\"password\":\"12345678\",\"users\":{\"total\":\"0\",\"list\":[]}},{\"id\":\"1\",\"enable\":\"1\",\"vlan_id\":\"0\",\"bassid\":\"00:00:00:00:00:00\",\"ssid\":\"wifi5g1\",\"ssid_hide\":\"0\",\"encode\":\"1\",\"password\":\"12345678\",\"users\":{\"total\":\"0\",\"list\":[]}},{\"id\":\"2\",\"enable\":\"1\",\"vlan_id\":\"0\",\"bassid\":\"00:00:00:00:00:00\",\"ssid\":\"wifi5g2\",\"ssid_hide\":\"0\",\"encode\":\"1\",\"password\":\"12345678\",\"users\":{\"total\":\"0\",\"list\":[]}},{\"id\":\"3\",\"enable\":\"1\",\"vlan_id\":\"0\",\"bassid\":\"00:00:00:00:00:00\",\"ssid\":\"wifi5g3\",\"ssid_hide\":\"0\",\"encode\":\"1\",\"password\":\"12345678\",\"users\":{\"total\":\"0\",\"list\":[]}}]}";
	}

	public String getTime_reboot(String mac) { 
	    return "{\"enable\":\"1\",\"time\":\"day1\"}";
	}
	
	public abstract String getBody();
	
	public Long getNow(){
		return System.currentTimeMillis()/1000;
	}
	
	public String getStr(int length,String source){
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for(int i = 0;i<length;i++){
			int number = random.nextInt(source.length());
			sb.append(source.charAt(number));
		}
		return sb.toString();
	}
	
	
	public abstract String getSystem(String mac, String type);
}

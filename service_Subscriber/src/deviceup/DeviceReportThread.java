package deviceup;

import java.net.URISyntaxException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;

import conf.deviceConf;

public abstract class DeviceReportThread extends Thread{
	
	private static final Log logger = LogFactory.getLog(DeviceReportThread.class);
	
	public Device device = null;
	public int waittime = 0;
	
	public String topic_up;
	public String topic_down;
	
	public BlockingConnection mqttConnection;
	
	public DeviceReportThread(Device device,int waittime){
		this.device = device;
		this.waittime = waittime;
		this.topic_up = this.device.getPrtid() + "/" + this.device.getCltid() + "/dev2app";
		this.topic_down = this.device.getPrtid() + "/" + this.device.getCltid() + "/app2dev";
		
		/*//建立mqtt连接
		MQTT mqtt = new MQTT();
		try{
			mqtt.setHost(this.device.getMqtthost(),this.device.getMqttport());
			mqtt.setUserName(deviceupConf.mqtt_username);
			mqtt.setPassword(deviceupConf.mqtt_password);
			mqtt.setClientId(this.device.getCltid());
			mqtt.setCleanSession(false);
			mqtt.setKeepAlive(deviceupConf.mqtt_keepalive);
			this.mqttConnection = mqtt.blockingConnection();
			this.mqttConnection.connect();
		}catch(URISyntaxException e){
			logger.error("mqtt connect failure,cause[" + e.getMessage() + "]");
		}catch(Exception e){
			logger.error("mqtt connect failure,cause[" + e.getMessage() + "]");
		}*/
	}
	
	public void run(){
		
	}
	
	public String getMessage(){
		String message = "{\"header\":"+this.getHeader()+",\"body\":"+this.getBody()+",\"now\":\""+this.getNow()+"\"}";
		return message;
	}
	
	private String getHeader(){
		return "{\"protocol\":\"v1.0\",\"type\":\"1\",\"encode\":\"1\",\"mid\":\""+this.getStr(30,"0123456789abcdefghijklmnopqrstuvw")+"\","
				+ "\"bind\":\""+device.getBind()+"\"}";
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
}

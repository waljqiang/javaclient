package test.devicereport;

import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import static org.fusesource.hawtbuf.Buffer.utf8;

import test.com.Helper;

public abstract class DeviceReportThread extends Thread{
	private static final Log logger = LogFactory.getLog(DeviceReportThread.class);
	
	protected long begin_time = System.currentTimeMillis() / 1000;
	
	public Device device = null;
	public int waittime = 0;
	
	public String topic_up;
	public String topic_down;
	
	public CallbackConnection mqttConnection;
	
	public DeviceReportThread(Device device,int waittime){
		this.device = device;
		this.waittime = waittime;
		this.topic_up = this.device.getPrtid() + "/" + this.device.getCltid() + "/dev2app";
		this.topic_down = this.device.getPrtid() + "/" + this.device.getCltid() + "/app2dev";
		
		//建立mqtt连接
		MQTT mqtt = new MQTT();
		try{
			mqtt.setHost(this.device.getMqtthost(),this.device.getMqttport());
			mqtt.setUserName(publicConf.MQTT_USERNAME);
			mqtt.setPassword(publicConf.MQTT_PASSWORD);
			mqtt.setClientId(this.device.getCltid());
			mqtt.setCleanSession(false);
			mqtt.setKeepAlive(publicConf.MQTT_KEEPALIVE);
			
			mqttConnection = mqtt.callbackConnection();
			Topic[] topic = {new Topic(utf8(this.topic_down), QoS.AT_LEAST_ONCE)};
			
			mqttConnection.connect(new Callback<Void>() {
	            // Once we connect..
	            public void onSuccess(Void v) {
	            	logger.debug("mqtt dev2app conn success");
	                // Subscribe to dev2app           	
	            	mqttConnection.subscribe(topic, new Callback<byte[]>() {
	                    public void onSuccess(byte[] value) {
	                    	logger.debug("mqtt subscribe "+topic_down+" success!");
	                    	logger.fatal("subscribe_message_Thread start success");
	                    }

	                    public void onFailure(Throwable value) {
	                    	mqttConnection.disconnect(null);	
	                    	logger.error("mqtt subscribe "+topic_down+" failure,to disconnect !!!,caused:"+value.getMessage());      
	                    }
	                });

	            }

	            public void onFailure(Throwable value) {
	            	logger.error("mqtt dev2app conn failure");
	            	logger.error("subscribe_Block_Thread start failure");
	            }
	        });
			
			
			mqttConnection.listener(new Listener() {
				public void onConnected() {
					logger.fatal("mqtt listener message connected");
				}
				public void onDisconnected() {
					logger.fatal("mqtt listener message disconnected");
				}

				public void onPublish(UTF8Buffer topic, org.fusesource.hawtbuf.Buffer payload, Runnable onComplete){
					String s_topic = topic.toString();
					String data = new String(payload.toByteArray());
					System.out.println("--------------------------");
				}

				public void onFailure(Throwable value) {
					//conn.disconnect(null);
					logger.error("receive message failure");
				}
			});
		}catch(URISyntaxException e){
			logger.error("mqtt connect failure,cause[" + e.getMessage() + "]");
		}catch(Exception e){
			logger.error("mqtt connect failure,cause[" + e.getMessage() + "]");
		}
	}
	
	public void run(){
		
	}
	
	public String getMessage(){
		String message = "{\"header\":"+this.getHeader()+",\"body\":"+this.getBody()+",\"now\":\""+this.getNow()+"\"}";
		return message;
	}
	
	private String getHeader(){
		return "{\"protocol\":\"v1.0\",\"type\":\"1\",\"encode\":\"1\",\"mid\":\""+Helper.getStr(30,"0123456789abcdefghijklmnopqrstuvw")+"\","
				+ "\"bind\":\""+device.getBind()+"\"}";
	}
	
	public abstract String getBody();
	
	public Long getNow(){
		return System.currentTimeMillis()/1000;
	}

	public Long getRuntime(){
		return this.getNow() - this.begin_time + 10;
	}
	
	public String getNetwork(){
		return "{\"lan\":[{\"ip\":\"192.168.10.2\",\"subnet\":\"255.255.255.0\",\"mac\":\""+this.device.getMac()+"\",\"dhcp_enable\":\"0\",\"dhcp_ip_start\":\"192.168.188.2\","
				+ "\"dhcp_ip_end\":\"192.168.188.236\",\"gateway\":\"192.168.10.1\",\"dns\":[\"114.114.114.114\"]}]}";
	}
	
	public String getTime_reboot(){
		return "{\"enable\":\"1\",\"time\":\"day1\"}";
	}
}

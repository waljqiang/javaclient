package deviceup;

import java.net.URISyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import conf.mqttConf;
import deviceup.Device;

import org.fusesource.mqtt.client.*;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Listener;

@SuppressWarnings("deprecation")
public class DeviceUpThread extends Thread{
	private static final Log logger = LogFactory.getLog(DeviceUpThread.class);
	//private static CallbackConnection mqttConnection;
	
	private Device device;

	public DeviceUpThread(Device device){
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
	 	
	 	String topic_str = this.device.getPrtid() + "/" + this.device.getCltid() + "/app2dev";

	 	final CallbackConnection connection = mqtt.callbackConnection();
	 	Topic[] topics = {new Topic(utf8(topic_str), QoS.AT_MOST_ONCE)};
	 	
	 	connection.connect(new Callback<Void>() {
            // Once we connect..
            public void onSuccess(Void v) {
            	logger.fatal("mqtt connected");
                // Subscribe to dev2app           	
            	connection.subscribe(topics, new Callback<byte[]>() {
                    public void onSuccess(byte[] value) {
                    	logger.debug("mqtt subscribe "+topic_str+" success!");
                    }

                    public void onFailure(Throwable value) {
                    	connection.disconnect(null);	
                        logger.error("mqtt disconnect "+topic_str+" failure,to disconnect !!!,caused:"+value.getMessage());      
                    }
                });

            }

            public void onFailure(Throwable value) {
            	logger.error("mqtt dev2app conn failure");
            }
        });
	 	
	 	connection.listener(new Listener() {
			public void onConnected() {
				logger.fatal("mqtt listener message connected");
			}
			public void onDisconnected() {
				logger.fatal("mqtt listener message disconnected");
			}

			public void onPublish(UTF8Buffer topic, org.fusesource.hawtbuf.Buffer payload, Runnable onComplete){
				String s_topic = topic.toString();
				String data = new String(payload.toByteArray());
				logger.debug("mqtt receive data[" + data + "]from topic[" + topic + "]");
			
			}

			public void onFailure(Throwable value) {
				//conn.disconnect(null);
				logger.error("receive message failure");
			}
		});
	}
	
	private String utf8(String topic_str2) {
		// TODO Auto-generated method stub
		return null;
	}

	public void run(){
		
	}

}

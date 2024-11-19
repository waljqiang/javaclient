package deviceup;

import java.net.URISyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import conf.mqttConf;
import deviceup.Device;

import org.fusesource.mqtt.client.*;

public class DeviceUpThread extends Thread{
	private static final Log logger = LogFactory.getLog(DeviceUpThread.class);
	private FutureConnection  mqttConnection;
	
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
	 	mqtt.setUserName("cloudnetlot");
	 	mqtt.setPassword("admin@cloudnetlot");
	 	mqtt.setClientId(this.device.getCltid());
	 	mqtt.setCleanSession(mqttConf.clean);
	 	mqtt.setKeepAlive(mqttConf.keepalive);

	 	this.mqttConnection = mqtt.futureConnection();
	 	this.mqttConnection.connect();
	 	
	 	Topic[] topics = {new Topic(org.fusesource.hawtbuf.Buffer.utf8(this.device.getPrtid() + "/" + this.device.getCltid() + "/app2dev"), QoS.AT_MOST_ONCE)};
	 	this.mqttConnection.subscribe(topics);
//	 	connection.connect(new Callback<Void>() {
//            // Once we connect..
//            public void onSuccess(Void v) {         	
//            	connection.subscribe(topics, new Callback<byte[]>() {
//            		
//                    public void onSuccess(byte[] value) {
//                    	logger.debug("The device["+device.getMac()+"] with mqtt subscribe "+topic_str+" success!");
//                    }
//
//                    public void onFailure(Throwable value) {
//                    	connection.disconnect(null);	
//                        logger.error("The device["+device.getMac()+"] with mqtt subscribe failure,caused:"+value.getMessage());      
//                    }
//                });
//
//            }
//
//            public void onFailure(Throwable value) {
//            	logger.error("mqtt dev2app conn failure");
//            }
//        });
//	 	
//	 	connection.listener(new Listener() {
//			public void onConnected() {
//				logger.fatal("The device["+device.getMac()+"] with mqtt connected");
//			}
//			public void onDisconnected() {
//				logger.fatal("The device["+device.getMac()+"] with mqtt disconnected");
//			}
//
//			public void onPublish(UTF8Buffer topic, org.fusesource.hawtbuf.Buffer payload, Runnable onComplete){
//				String s_topic = topic.toString();
//				String data = new String(payload.toByteArray());
//				logger.debug("mqtt receive data[" + data + "]from topic[" + s_topic + "]");
//			
//			}
//
//			public void onFailure(Throwable value) {
//				//conn.disconnect(null);
//				logger.error("receive message failure");
//			}
//		});
	}
	
	private String utf8(String topic_str2) {
		// TODO Auto-generated method stub
		return null;
	}

	public void run(){
		try{
			while(true){
				this.mqttConnection.publish(device.getPrtid() + "/" + device.getCltid() + "/dev2app", "Hello".getBytes(), QoS.AT_LEAST_ONCE, false);
				Thread.sleep(1000);
			}
		}catch(Exception e){
			System.out.println(e);
		}
	}

}

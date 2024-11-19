package lib;

import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Topic;
import java.util.List;
import conf.mqttConf;
import java.util.ArrayList;
import static org.fusesource.mqtt.client.QoS.AT_LEAST_ONCE; // Correct QoS import

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.hawtbuf.Buffer;
import deviceup.Device;
import deviceup.DeviceUpBase;

public class MqttClientManager {
	private static final Log logger = LogFactory.getLog(DeviceUpBase.class);
	private MQTT mqtt;
	private List<CallbackConnection> connections = new ArrayList<>();
	
    public CallbackConnection getMqttClient(Device device) throws Exception {
	    logger.debug("link clients is :"+device.getCltid());
    	mqtt = new MQTT();
        mqtt.setHost(mqttConf.address, mqttConf.port);
        mqtt.setClientId(device.getCltid());
        mqtt.setUserName(mqttConf.username);
        mqtt.setPassword(mqttConf.password);
        mqtt.setKeepAlive(mqttConf.keepalive);
        
       // 设置重连延迟时间为 10 毫秒
        mqtt.setReconnectDelay(10);

        // 设置重连最大延迟时间为 30 秒
        mqtt.setReconnectDelayMax(30 * 1000);

        // 设置重连延迟时间的指数退避乘数为 2.0
        mqtt.setReconnectBackOffMultiplier(2.0);

        // 设置最大重连尝试次数为 5 次
        mqtt.setReconnectAttemptsMax(5);

        CallbackConnection connection = mqtt.callbackConnection();
        connection.listener(new org.fusesource.mqtt.client.Listener() {
            
            @Override
            public void onConnected() {
                logger.debug("Connected to MQTT broker");
            }

            @Override
            public void onDisconnected() {
            	 logger.debug("Disconnected from MQTT broker");
            }

            @Override
            public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
            	 logger.debug("Received message on topic: " + topic.toString() + " Message: " + new String(body.toByteArray()));
                ack.run(); // 确认消息接收
            }

            @Override
            public void onFailure(Throwable value) {
            	 logger.debug("Connection failed: " + value.getMessage());
            }
        });
        connection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                logger.debug("Connected to MQTT broker");
                try {
                	//订阅
                    subscribeToTopic(connection, device);
                } catch (Exception e) {
                    logger.error("Exception occurred while subscribing to topic", e);
                }
            }

            @Override
            public void onFailure(Throwable value) {
                logger.error("Failed to connect to MQTT broker: " + value.getMessage(), value);
            }
        });
        SubOnlineThread sub = new SubOnlineThread();
        sub.start();
       // setMqttClient(connection);
        return connection;
    }
    
    // 新增方法来保存连接
    public  void setMqttClient(CallbackConnection connection) {
        if (connection != null) {
            connections.add(connection);
        }
    }
    
    //订阅
	private void subscribeToTopic(CallbackConnection connection, Device device) throws Exception {
	    String topic = device.getPrtid() + "/" + device.getCltid() + "/app2dev";
	    Topic[] topics = { new Topic(topic, AT_LEAST_ONCE) };
	    logger.debug("link prtid is :"+device.getPrtid());
	    logger.debug("link cltid   is :"+device.getCltid());
	    logger.debug("link topic is :"+topic);
	    connection.subscribe(topics, new Callback<byte[]>() {
	        @Override
	        public void onSuccess(byte[] qoses) {
	            logger.debug("Subscribed to topic");
	        }
	
	        @Override
	        public void onFailure(Throwable value) {
	            logger.error("Failed to subscribe to topic: " + value.getMessage(), value);
	        }
	    });
	   
	}
    
}
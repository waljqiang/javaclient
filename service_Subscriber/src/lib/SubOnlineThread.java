package lib;

import static org.fusesource.hawtbuf.Buffer.utf8;


import java.net.URISyntaxException;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import conf.mqttConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubOnlineThread  extends Thread  {
	private static final Log logger = LogFactory.getLog(SubOnlineThread.class);	
	private String topic_str;
	private QoS qos;

	public SubOnlineThread(){
		this.topic_str=mqttConf.TOPIC_DEV_ONLINE;
		this.qos=mqttConf.QoS_send;
	}

	public static void  pub_Block(String[] top_message,final QoS qos, final boolean retain ) throws Exception 
	{
		UTF8Buffer clintid=new UTF8Buffer("cloudnetdaemon"+Helper.getStr(12));  
	 	MQTT mqtt = new MQTT(clintid);
		 
	     BlockingConnection connection = mqtt.blockingConnection();
	     connection.connect();	      
	        
	     for(int i=0;i/2<top_message.length/2;i=i+2)
	     {
	        connection.publish(top_message[i], top_message[i+1].getBytes(), qos,retain);
	        
	     }
	 	 Thread.sleep(223000);  	        
	     connection.disconnect();
	  }
	
	public void run (){
		logger.fatal("subscribe_online_Thread start...");
	 	MQTT mqtt = new MQTT();
	 	try {
			mqtt.setHost(mqttConf.address,mqttConf.port);
		} catch (URISyntaxException e) {
			logger.error(e.getMessage());
		}
	 	mqtt.setUserName(mqttConf.username);
	 	mqtt.setPassword(mqttConf.password);
	 	mqtt.setClientId("cloudnetlot_"+topic_str);
	 	mqtt.setCleanSession(mqttConf.clean);
	 	mqtt.setKeepAlive(mqttConf.keepalive);
	 	
	 	final CallbackConnection connection = mqtt.callbackConnection();
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
	 	Topic[] topic = {new Topic(utf8(topic_str), qos)};
	 	connection.connect(new Callback<Void>() {
            // Once we connect..
            public void onSuccess(Void v) {
            	logger.debug("mqtt online conn success");
                // Subscribe to dev2app           	
                connection.subscribe(topic, new Callback<byte[]>() {
                    public void onSuccess(byte[] value) {
                       
                    	logger.debug("mqtt subscribe "+topic_str+" success!");
                    	logger.fatal("subscribe_online_Thread start success");
                    }

                    public void onFailure(Throwable value) {
                        connection.disconnect(null);	
                        logger.error("mqtt subscribe "+topic_str+" failure,to disconnect !!!,caused:"+value.getMessage());      
                    }
                });

            }

            public void onFailure(Throwable value) {
            	logger.error("mqtt online conn failure");
            	logger.error("subscribe_online_Thread start failure");
            }
        });
	 	//ListenerOnlineThread listenMessageThr=new ListenerOnlineThread(connection);
	 	//listenMessageThr.setName("listenOnlineThr");
    	//listenMessageThr.start();
	}
}

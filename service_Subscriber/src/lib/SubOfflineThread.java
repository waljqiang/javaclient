package lib;

import static org.fusesource.hawtbuf.Buffer.utf8;


import java.net.URISyntaxException;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import conf.mqttConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubOfflineThread  extends Thread  {
	private static final Log log = LogFactory.getLog(SubOfflineThread.class);	
	private String topic_str;
	private QoS qos;

	public SubOfflineThread(){
		this.topic_str=mqttConf.TOPIC_DEV_OFFLINE;
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
		log.fatal("subscribe_Block_Thread start...");
	 	MQTT mqtt = new MQTT();
	 	try {
			mqtt.setHost(mqttConf.address,mqttConf.port);
		} catch (URISyntaxException e) {
			log.error(e.getMessage());
		}
	 	mqtt.setUserName(mqttConf.username);
	 	mqtt.setPassword(mqttConf.password);
	 	mqtt.setClientId("cloudnetlot_"+topic_str);
	 	mqtt.setCleanSession(mqttConf.clean);
	 	mqtt.setKeepAlive(mqttConf.keepalive);
	 	
	 	final CallbackConnection connection = mqtt.callbackConnection();
	 	Topic[] topic = {new Topic(utf8(topic_str), qos)};
	 	connection.connect(new Callback<Void>() {
            // Once we connect..
            public void onSuccess(Void v) {
            	log.debug("mqtt offline conn success");
                // Subscribe to dev2app           	
                connection.subscribe(topic, new Callback<byte[]>() {
                    public void onSuccess(byte[] value) {
                        // Once subscribed, publish a message on the same topic.
                        //connection.publish("foo", "Hello".getBytes(), QoS.AT_LEAST_ONCE, false, null);
                    	log.debug("mqtt subscribe "+topic_str+" success!");
                    	log.fatal("subscribe_Block_Thread start success");
                    }

                    public void onFailure(Throwable value) {
                        connection.disconnect(null);	
                        log.error("mqtt subscribe "+topic_str+" failure,to disconnect !!!,caused:"+value.getMessage());      
                    }
                });

            }

            public void onFailure(Throwable value) {
                log.error("mqtt offline conn failure");
                log.error("subscribe_Block_Thread start failure");
            }
        });
	 	ListenerOfflineThread listenMessageThr=new ListenerOfflineThread(connection);
	 	listenMessageThr.setName("listenOfflineThr");
    	listenMessageThr.start();
	}
}

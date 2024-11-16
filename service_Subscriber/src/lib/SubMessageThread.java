package lib;

import static org.fusesource.hawtbuf.Buffer.utf8;


import java.net.URISyntaxException;
import org.fusesource.mqtt.client.*;
import conf.mqttConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubMessageThread  extends Thread  {
	private static final Log log = LogFactory.getLog(SubMessageThread.class);	
	private String topic_str;
	private String topic_auth;
	private QoS qos;
	private QoS qos_auth;

	public SubMessageThread(){
		this.topic_str=mqttConf.TOPIC_DEV_APP;
		this.qos=mqttConf.QoS_sub;
		this.topic_auth = mqttConf.TOPIC_DEV_AUTH;
		this.qos_auth = mqttConf.QoS_auth;
	 }
	
	public void run (){
		log.fatal("subscribe_message_Thread start...");
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
	 	Topic[] topic = {new Topic(utf8(topic_str), qos),new Topic(utf8(topic_auth),qos_auth)};
	 	connection.connect(new Callback<Void>() {
            // Once we connect..
            public void onSuccess(Void v) {
            	log.debug("mqtt dev2app conn success");
                // Subscribe to dev2app           	
                connection.subscribe(topic, new Callback<byte[]>() {
                    public void onSuccess(byte[] value) {
                        // Once subscribed, publish a message on the same topic.
                        //connection.publish("foo", "Hello".getBytes(), QoS.AT_LEAST_ONCE, false, null);
                    	log.debug("mqtt subscribe "+topic_str+" success!");
                    	log.debug("mqtt subscribe "+topic_auth+" success!");
                    	log.fatal("subscribe_message_Thread start success");
                    }

                    public void onFailure(Throwable value) {
                        connection.disconnect(null);	
                        log.error("mqtt subscribe "+topic_str+" failure,to disconnect !!!,caused:"+value.getMessage());      
                    }
                });

            }

            public void onFailure(Throwable value) {
                log.error("mqtt dev2app conn failure");
                log.error("subscribe_Block_Thread start failure");
            }
        });
    	ListenerMessageThread listenMessageThr=new ListenerMessageThread(connection);
    	listenMessageThr.setName("listenMessageThr");
    	listenMessageThr.start();
	}
}

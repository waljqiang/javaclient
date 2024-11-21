package lib;
import java.util.List;
import java.util.ArrayList;
import com.alibaba.fastjson.JSONObject;
import  conf.deviceConf;
import java.util.Arrays;
import conf.mqttConf;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.security.MessageDigest;
import java.net.URISyntaxException;
import java.util.Random;
import deviceup.Device;
public class Helper{
	private static final Log log = LogFactory.getLog(Helper.class);
	private static MQTT mqtt;
	private static FutureConnection mqttConnection;
	public static String setMac(String mac){
		return mac.substring(0,2) + ":" + mac.substring(2,4) + ":" + mac.substring(4,6) + ":" + mac.substring(6,8) + ":" + mac.substring(8,10) + ":" + mac.substring(10,12);
	}
	
	//生成随机字符串
	public static String getStr(){
		int length = 6;
		String source = "0123456789";
		return Helper.getStr(length,source);
	}
	
	public static String getStr(int length){
		String source = "0123456789";
		return Helper.getStr(length,source);
	}
	
	public static String getStr(int length,String source){
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for(int i = 0;i<length;i++){
			int number = random.nextInt(source.length());
			sb.append(source.charAt(number));
		}
		return sb.toString();
	}
	public static String parseMac(String mac){
		return mac.replace(":","");
	}
	
	public static Long hexdec(String str){
		long sum=0,tmp=0;
		for(int i=0;i<str.length();i++){
			char c = str.charAt(i);
			if(c>='0'&&c<='9'){
				tmp = c-'0';
			}else if(c>='A'&&c<='F'){
				tmp=c-'A'+10;
			}else{
				break;
			}
			sum=sum*16+tmp;
		}
		return sum; 
	}
  //生成协议信息类型
	public static String getCommType(String type){
		return Helper.padLeft(Integer.toHexString(Integer.parseInt(type)),4,'0').toUpperCase();
	}
	
	public static String getCommID(String lotType,String commType,long time){
		return Helper.getCommType(lotType) + Helper.getCommType(commType) + time + Helper.getStr(4);
	}
	//获取topic
	public static String getTopic(String prtid,String cltid){
		return String.format(mqttConf.TOPIC_APP_DEV.replace("+","%s"),prtid,cltid);
	}
	
	public static String padLeft(String str,int len,char ch){
		int diff = len - str.length();
		if(diff <= 0){
			return str;
		}
		int charLength = len - str.length();
		char[] charr = new char[charLength];
		for(int i = 0;i < charLength;i++){
			charr[i] = ch;
		}
		return new String(charr) + str;
	}
	

	
	public static String jsonGet(JSONObject jsonObject,String key,String defaultValue){
    	String res;
    	String[] keys = key.split("\\.");
    	if(keys.length > 1){
    		JSONObject tmp = jsonObject.getJSONObject(keys[0]);
    		if(tmp != null){
	    		for(int i=1;i<keys.length-1;i++){
	    			tmp = tmp.getJSONObject(keys[i]);
	    			if(tmp == null){
	    				break;
	    			}
	    		}
    		}
    		res = tmp == null ? defaultValue : tmp.getString(keys[keys.length-1]);
    	}else{
    		res = jsonObject.getString(keys[0]);
    	}
    	return res == null || res.isEmpty() ? defaultValue : res;
    }
	

	
	

	
	
	//生成mac
	 public static List<String> generateMacs(int nums) {
	        List<String> macs = new ArrayList<>();
	        String mac = deviceConf.mac_start;
	        for (int i = 0; i < nums; i++) {
	            macs.add(mac);
	            mac = getNextMac(mac, 1);
	        }
	        return macs;
	    }
	
	//生成下一个mac
	public static String getNextMac(String mac, int step) {
		// 移除MAC地址中的冒号
	    String mactrim = mac.replace(":", "");
	    // 将十六进制字符串转换为十进制长整数
	    long macdec = Long.parseLong(mactrim, 16);
	    // 计算下一个MAC地址的十进制值
	    long macdecnext = macdec + step;
	    // 将十进制整数转换回十二位的十六进制字符串
	    String mactrimnext = String.format("%012X", macdecnext);
	    // 将十六进制字符串格式化为MAC地址格式，每两个字符插入一个冒号
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < 12; i += 2) {
	        if (i > 0) {
	            sb.append(":");
	        }
	        sb.append(mactrimnext.substring(i, i + 2));
	    }
	    return sb.toString();
    }
	
	
	public static void sendMqtt(String topic,String message){
		Helper.sendMqtt(topic, message,QoS.AT_LEAST_ONCE);
	}
	
	public static void sendMqtt(String topic,String message,QoS qos){
		Helper.sendMqtt(topic, message, qos,false);
	}
	
	public static void getMqttClient(Device device)  {
		try{
			Helper.mqtt = new MQTT();
			Helper.mqtt.setHost(mqttConf.address,mqttConf.port);
			Helper.mqtt.setClientId(device.getCltid());
			Helper.mqtt.setUserName(mqttConf.username);
			Helper.mqtt.setPassword(mqttConf.password);
	
		    // 设置连接选项
			Helper.mqtt.setCleanSession(true);
			Helper.mqtt.setKeepAlive((short) 60);
	
		    Helper.mqttConnection = Helper.mqtt.futureConnection();
		 	Helper.mqttConnection.connect();
		 	
		}catch(URISyntaxException e){
			
			//log.error("mqtt send message[" + message + "] to topic[" + topic + "] failure,cause:"+e.getMessage());
		}
	}
	public static void sendMqtt(String topic,String message,QoS qos,boolean retain){
	
		if(Helper.mqtt == null){
			Helper.mqtt = new MQTT();
			try{
				Helper.mqtt.setHost(mqttConf.address,mqttConf.port);
				Helper.mqtt.setUserName(mqttConf.username);
			 	Helper.mqtt.setPassword(mqttConf.password);
			 	Helper.mqtt.setClientId("service_"+Helper.getStr(10));
			 	Helper.mqtt.setCleanSession(mqttConf.clean);
			 	Helper.mqtt.setKeepAlive(mqttConf.keepalive);
			 	Helper.mqttConnection = Helper.mqtt.futureConnection();
			 	Helper.mqttConnection.connect();
			 	/*Future<Void> f1 = Helper.mqttConnection.connect();
			 	try {
					f1.await();
				} catch (Exception e) {
					Helper.log("f1 mqtt send message[" + message + "] to topic[" + topic + "] failure,cause:"+e.getMessage(),"info",log);
				}*/
			}catch(URISyntaxException e){
				log.error("mqtt send message[" + message + "] to topic[" + topic + "] failure,cause:"+e.getMessage());
			}
		}
		/*if(Helper.mqttConnection == null || !Helper.mqttConnection.isConnected()){
		 	Helper.mqttConnection = Helper.mqtt.futureConnection();    
		 	Helper.mqttConnection.connect();
		}*/
		try{
			Helper.mqttConnection.publish(topic, message.getBytes(),qos,retain);
			log.debug("mqtt send message[" + message + "] to topic[" + topic + "] success");
		}catch(Exception e){
			log.error("mqtt send message[" + message + "] to topic[" + topic + "] failure,cause:"+e.getMessage());
		}
	}
	
	//生成设备验证token
		public static String generateDevToken(String username,String password,String mac,Long time){
			try{
				String[] array = new String[] {username,password,mac,time.toString()};
				StringBuffer sb = new StringBuffer();
				//排序
				Arrays.sort(array);
				for(int i=0;i < 4;i++){
					sb.append(array[i]);
				}
				String str = sb.toString();
				//SHA1签名
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(str.getBytes());
				byte[] digest = md.digest();
				StringBuffer hexstr = new StringBuffer();
				String shaHex = "";
				for(int i=0;i < digest.length;i++){
					shaHex = Integer.toHexString(digest[i] & 0xFF);
					if(shaHex.length() < 2){
						hexstr.append(0);
					}
					hexstr.append(shaHex);
				}
				return hexstr.toString();
			}catch(Exception e){
				return "";
			}
		}
}
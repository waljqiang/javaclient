package lib;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Exception;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.security.MessageDigest;

import conf.hashidsConf;
import conf.mqttConf;

import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import com.alibaba.fastjson.JSONObject;

public class Helper{
	private static final Log log = LogFactory.getLog(Helper.class);
	public static Hashids hashPrtid = new Hashids(hashidsConf.hashPrtid.get("salt"),Integer.parseInt(hashidsConf.hashPrtid.get("length")),hashidsConf.hashPrtid.get("alphabet"),hashidsConf.hashPrtid.get("header"),true);
	public static Hashids hashCltid = new Hashids(hashidsConf.hashCltid.get("salt"),Integer.parseInt(hashidsConf.hashCltid.get("length")),hashidsConf.hashCltid.get("alphabet"),hashidsConf.hashCltid.get("header"),true);
	public static Hashids hashid = new Hashids(hashidsConf.hashid.get("salt"),Integer.parseInt(hashidsConf.hashid.get("length")),hashidsConf.hashid.get("alphabet"),hashidsConf.hashid.get("header"),true);
	private static MQTT mqtt;
	private static FutureConnection mqttConnection;
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
	
	//时区时间
	public static String convUnixToZoneGm(String time,String timeZone,String isSummerTime){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Date ldate = new Date(Long.valueOf(time + "000"));
		Calendar cal = Calendar.getInstance();
		cal.setTime(ldate);
		
		String[] arr = timeZone.split(":");
		String symbol = arr[0].substring(0,1);
		int minute = Integer.parseInt(symbol + ((Integer.parseInt(arr[0].substring(1)) * 60 + Integer.parseInt(arr[1])))) + Integer.parseInt(isSummerTime) * 60;
		cal.add(Calendar.MINUTE,minute);
		Date ndate = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(ndate);
	}
	
	public static String convDateToZoneGm(String date,String timeZone,String isSummerTime){
		String res = "";
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String str = Long.toString(sdf.parse(date).getTime()/1000);
            res = Helper.convUnixToZoneGm(str,timeZone,isSummerTime);
        } catch (Exception e) {  
            //log.error("convDateToZoneGm error");
            log.error("convDateToZoneGm error");
        }  
		return res;
	}
	
	public static String getTimeEndHour(String time){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Date ldate = new Date(Long.valueOf(time + "000"));
		Calendar cal = Calendar.getInstance();
		cal.setTime(ldate);
		cal.add(Calendar.MINUTE,60);
		cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( cal.getTime());
	}
	
	//获取天
	public static String getDayStr(Date ldate,int n){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.add(Calendar.DATE,n);
        return sdf.format(calendar.getTime());
	}
	
	//获取月开始
	public static String getStartMonthStr(Date ldate,int n){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.add(Calendar.MONTH,n);
        calendar.set(Calendar.DATE, 1);
        return sdf.format(calendar.getTime());
	}
	
	//获取月结束
	public static String getEndMonthStr(Date ldate,int n){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.add(Calendar.MONTH,n);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return sdf.format(calendar.getTime());
	}
	//获取月
	public static String getMonthStr(Date ldate,int n){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.add(Calendar.MONTH,n);
        return sdf.format(calendar.getTime());
	}
	
	//获取前年
	public static String getYearStr(Date ldate,int n){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.add(Calendar.YEAR,n);
        return sdf.format(calendar.getTime());
	}
	
	//获取年首月
	public static String getStartYearStr(Date ldate,int n){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.add(Calendar.YEAR,n);
        calendar.set(Calendar.MONTH,0);
        return sdf.format(calendar.getTime());
	}
	
	//获取年尾月
	public static String getEndYearStr(Date ldate,int n){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.add(Calendar.YEAR,n);
        calendar.set(Calendar.MONTH,11);
        return sdf.format(calendar.getTime());
	}
	
	//获取季度序号
	public static String getQuarterStr(Date ldate,int n,Boolean withYear){
		String result;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.add(Calendar.MONTH, n);
        int month = calendar.get(Calendar.MONTH)+1;
        if(month <= 3){
        	result = "Q1";
        }else if(month <= 6){
        	result = "Q2";
        }else if(month <= 9){
        	result = "Q3";
        }else{
        	result = "Q4";
        }
        if(withYear)
        	return calendar.get(Calendar.YEAR) + "-" + result;
        else
        	return result;
	}
	
	//获取上一个季度开始
	public static String getPreStartQuarterStr(Date ldate){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.set(Calendar.MONTH, ((int) calendar.get(Calendar.MONTH) / 3 - 1) * 3);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return sdf.format(calendar.getTime());
	}
	
	//获取上一个季度最后一天
	public static String getPreEndQuarterStr(Date ldate){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ldate);
        calendar.set(Calendar.MONTH, ((int) calendar.get(Calendar.MONTH) / 3 - 1) * 3 + 2);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        return sdf.format(calendar.getTime());
	}
	
	//获取上下某周的开始
	public static String getStartWeekStr(Date ldate,int n){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(ldate);
		int curMonth = cal.get(Calendar.MONTH);
		int week = cal.get(Calendar.DAY_OF_WEEK)-1;
		if(week == 0)
			week = 7;
		cal.add(Calendar.DAY_OF_YEAR, -week+1);
		if(curMonth == 0 && cal.get(Calendar.MONTH) != 0){
			cal.add(Calendar.YEAR, 1);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DATE, 1);
		}

		if(n < 0){
			n = -n;
			while(n > 0){
				cal.add(Calendar.DAY_OF_YEAR, -1);
				curMonth = cal.get(Calendar.MONTH);
				week = cal.get(Calendar.DAY_OF_WEEK)-1;
				if(week == 0)
					week = 7;
				cal.add(Calendar.DAY_OF_YEAR,-week+1);
				if(curMonth == 0 && cal.get(Calendar.MONTH) != 0){
					cal.add(Calendar.YEAR, 1);
					cal.set(Calendar.MONTH, 0);
					cal.set(Calendar.DATE, 1);
				}
				n--;
			}
		}else if(n > 0){
			while(n > 0){
				curMonth = cal.get(Calendar.MONTH);
				week = cal.get(Calendar.DAY_OF_WEEK)-1;
				if(week == 0)
					week = 7;
				cal.add(Calendar.DAY_OF_YEAR,7-week);
				if(curMonth == 11){
					cal.add(Calendar.DAY_OF_YEAR, 1);
					if(cal.get(Calendar.MONTH) == 0){
						cal.add(Calendar.YEAR, -1);
						cal.set(Calendar.MONTH,11);
						cal.set(Calendar.DATE, 31);
					}
				}
				cal.add(Calendar.DAY_OF_YEAR,1);
				n--;
			}
		}
		return sdf.format(cal.getTime());
	}
	//获取上下某周的结束
	public static String getEndWeekStr(Date ldate,int n){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(ldate);
		int curMonth = cal.get(Calendar.MONTH);
		int week = cal.get(Calendar.DAY_OF_WEEK)-1;
		if(week == 0)
			week = 7;
		cal.add(Calendar.DAY_OF_YEAR, 7-week);
		if(curMonth == 11 && cal.get(Calendar.MONTH) != 11){
			cal.add(Calendar.YEAR, -1);
			cal.set(Calendar.MONTH, 11);
			cal.set(Calendar.DATE, 31);
		}
		if(n < 0){
			n = -n;
			while(n > 0){
				curMonth = cal.get(Calendar.MONTH);
				week = cal.get(Calendar.DAY_OF_WEEK)-1;
				if(week == 0)
					week = 7;
				cal.add(Calendar.DAY_OF_YEAR,-week);	
				if(curMonth == 0){
					if(cal.get(Calendar.MONTH) == 11){
						cal.add(Calendar.DAY_OF_YEAR,-1);
						cal.set(Calendar.MONTH, 11);
						cal.set(Calendar.DATE, 31);
					}
				}
				n--;
			}
		}else if(n > 0){
			while(n > 0){
				curMonth = cal.get(Calendar.MONTH);
				cal.add(Calendar.DAY_OF_YEAR, 1);
				if(curMonth == 11){
					if(cal.get(Calendar.MONTH) == 0){
						week = cal.get(Calendar.DAY_OF_WEEK) - 1;
						if(week == 0)
							week = 7;
						cal.add(Calendar.DAY_OF_YEAR,7-week);
					}else{
						cal.add(Calendar.DAY_OF_YEAR, 6);
						if(cal.get(Calendar.MONTH) == 0){
							cal.add(Calendar.YEAR, -1);
							cal.set(Calendar.MONTH,11);
							cal.set(Calendar.DATE, 31);
						}
					}
				}else{
					week = cal.get(Calendar.DAY_OF_WEEK) - 1;
					if(week == 0)
						week = 7;
					cal.add(Calendar.DAY_OF_YEAR,7-week);
				}
				n--;
			}
		}
		return sdf.format(cal.getTime());
	}
	
	//获取一年中第几周
	public static String getWeekStr(Date ldate,int n,Boolean withYear){
		String str;
		Calendar cal = Calendar.getInstance();
		//设置星期一为一周开始的第一天
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(ldate);
		cal.add(Calendar.DAY_OF_YEAR,7*n);
		if(withYear)
			str = cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.WEEK_OF_YEAR);
		else
			str = String.valueOf(cal.get(Calendar.WEEK_OF_YEAR));
		return str;
	}
	
	//生成协议信息类型
	public static String getCommType(String type){
		return Helper.padLeft(Integer.toHexString(Integer.parseInt(type)),4,'0').toUpperCase();
	}
	//生成命令ID
	public static String getCommID(String lotType,String commType){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Date ldate = new Date();
		Long time = ldate.getTime()/1000;
		return Helper.getCommID(lotType, commType,time);
	}
	public static String getCommID(String lotType,String commType,long time){
		return Helper.getCommType(lotType) + Helper.getCommType(commType) + time + Helper.getStr(4);
	}
	//获取topic
	public static String getTopic(String prtid,String cltid){
		return String.format(mqttConf.TOPIC_APP_DEV.replace("+","%s"),prtid,cltid);
	}
	//生成产品ID
	public static String generatePrtid(String userID){
		Long time = new Date().getTime() / 1000;
		return Helper.hashPrtid.encode(Long.parseLong(userID),time);
	}
	//解析用户ID
	public static String decodeUid(String uid){
		String res = "";
		if(Boolean.parseBoolean(hashidsConf.hashid.get("enable"))){
			long[] uidArr = Helper.hashid.decode(uid);
			if(uidArr.length == 1){
				res = String.valueOf(uidArr[0]);
			}
		}else{
			res = uid;
		}	
		return res;
	}
	//解析产品ID,返回值为null时解析失败
	public static String[] decodePrtid(String prtid){
		String res[] = new String[1];
		long[] prtIDArr = Helper.hashPrtid.decode(prtid);
		if(prtIDArr.length == 2){
			res[0] = String.valueOf(prtIDArr[0]);// 
		}
		return res;
	}
	//生成客户端ID
	public static String generateCltid(String userID,String productID,String mac){
		Long time = new Date().getTime() / 1000;
		mac = "1" + Helper.parseMac(mac);
		long macdec = Helper.hexdec(mac);
		return Helper.hashCltid.encode(Long.parseLong(userID),Long.parseLong(productID),macdec,time);
	}
	
	public static String generateCltid(String userID,String productID,String mac,long time){
		mac = "1" + Helper.parseMac(mac);
		long macdec = Helper.hexdec(mac);
		return Helper.hashCltid.encode(Long.parseLong(userID),Long.parseLong(productID),macdec,time);
	}
	//解析客户端ID,返回值为null时解析失败
	public static String[] decodeCltid(String cltid){
		String res[] = new String[3];
		long[] cltIDArr = Helper.hashCltid.decode(cltid);
		if(cltIDArr.length == 4){
			res[0] = String.valueOf(cltIDArr[0]);//用户ID
			res[1] = String.valueOf(cltIDArr[1]);//产品ID
			res[2] = Helper.setMac(Helper.dechex(cltIDArr[2]).substring(1));//设备MAC
		}
		return res;
	}
	//生成绑定码
	public static String getBindCode(String userID,String mac,String gid){
		StringBuffer result = new StringBuffer();
		Long time = new Date().getTime() / 1000;
		mac = Helper.parseMac(mac);
		String str = (userID + "#" + mac + "#" + gid + "#" + time).toLowerCase();
		int keyLen = mac.length();
		int strLen = str.length();
		for(int i=0;i < strLen;i++){
			int k = i % keyLen;
			result.append((char)(str.charAt(i) ^ mac.charAt(k)));
		}
		return new String(org.apache.commons.codec.binary.Base64.encodeBase64(result.toString().getBytes()));
	}
	//解析绑定码
	public static String[] parseBindCode(String bindCode,String key){
		String res[] = new String[3];
		StringBuffer result = new StringBuffer();
		String bind = new String(org.apache.commons.codec.binary.Base64.decodeBase64(bindCode));
		int keyLen = key.length();
		for(int i=0;i < bind.length();i++){
			int k = i % keyLen;
			result.append((char)(bind.charAt(i)^key.charAt(k)));
		}
		String[] arr = result.toString().split("#");
		if(arr.length == 4){
			res[0] = arr[0];//userID
			res[1] = Helper.setMac(arr[1]).toUpperCase();//mac
			res[2] = arr[2];//gid
		}
		return res;
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
	
	public static String setMac(String mac){
		return mac.substring(0,2) + ":" + mac.substring(2,4) + ":" + mac.substring(4,6) + ":" + mac.substring(6,8) + ":" + mac.substring(8,10) + ":" + mac.substring(10,12);
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
	
	private static String dechex(long num) {
        return Long.toHexString(num).toUpperCase();
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
	
	public static void sendMqtt(String topic,String message){
		Helper.sendMqtt(topic, message,QoS.AT_LEAST_ONCE);
	}
	
	public static void sendMqtt(String topic,String message,QoS qos){
		Helper.sendMqtt(topic, message, qos,false);
	}
	
	public static void sendMqtt(String topic,String message,QoS qos,boolean retain){
		/*if(Helper.mqtt == null){
			Helper.mqtt = new MQTT();
		 	try {
				Helper.mqtt.setHost(mqttConf.address,mqttConf.port);
				Helper.mqtt.setUserName(mqttConf.username);
			 	Helper.mqtt.setPassword(mqttConf.password);
			 	//Helper.mqtt.setClientId("service_Subsriber_http_1"+topic);
			 	Helper.mqtt.setCleanSession(mqttConf.clean);
			 	Helper.mqtt.setKeepAlive(mqttConf.keepalive);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				Helper.log("mqtt send message[" + message + "] to topic[" + topic + "] failure,cause:connect failure","info",log);
			}
		}
		Helper.mqtt.setClientId("service_"+Helper.getStr(10));
		CallbackConnection connection = Helper.mqtt.callbackConnection();
	 	connection.connect(new Callback<Void>() {
            public void onSuccess(Void v) {   	
            	connection.publish(topic, message.getBytes(),qos,retain,new Callback<Void>(){
            		public void onSuccess(Void v) {
            			Helper.log("mqtt send message[" + message + "] to topic[" + topic + "] success","info",log);
            			connection.disconnect(null);
                    }
                    public void onFailure(Throwable value) {
                    	Helper.log("mqtt send message[" + message + "] to topic[" + topic + "] failure,cause:publish failure","info",log);
                    	connection.disconnect(null); // publish failed.
                    }
            	});
            }

            public void onFailure(Throwable value) {
                Helper.log("mqtt send message[" + message + "] to topic[" + topic + "] failure,cause:connect failure","info",log);
                connection.disconnect(null);
            }
        });*/
		/*if(Helper.mqttConnection == null){
			Helper.mqtt = new MQTT();
		 	try {
				Helper.mqtt.setHost(mqttConf.address,mqttConf.port);
				Helper.mqtt.setUserName(mqttConf.username);
			 	Helper.mqtt.setPassword(mqttConf.password);
			 	Helper.mqtt.setClientId("service_"+Helper.getStr(10));
			 	Helper.mqtt.setCleanSession(true);
			 	Helper.mqtt.setKeepAlive(mqttConf.keepalive);
			 	Helper.mqttConnection = mqtt.blockingConnection();
			 	try{
			 		Helper.mqttConnection.connect();
			 	}catch(Exception e){
			 		Helper.log("mqtt send message[" + message + "] to topic[" + topic + "] failure,cause:connect failure","info",log);
			 	}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				Helper.log("mqtt send message[" + message + "] to topic[" + topic + "] failure,cause:connect failure","info",log);
			}
		}
		try{
			Helper.mqttConnection.publish(topic, message.getBytes(),qos,retain);
			Helper.log("mqtt send message[" + message + "] to topic[" + topic + "] success","info",log);
		}catch(Exception e){
			Helper.log("mqtt send message[" + message + "] to topic[" + topic + "] failure,cause:publish failure","info",log);
		}*/
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
	
	/*public static void log(Object message,String logLevel,Log logger){
		String str = "["+Thread.currentThread().getStackTrace()[2].getClassName() + "->" + Thread.currentThread().getStackTrace()[2].getMethodName() + "]" + message.toString();
		switch(logLevel.toLowerCase()){
			case "fatal":
				if(publicConf.lfatal)
					log.fatal(message);
				break;
			case "error":
				if(publicConf.lerror)
					log.error(str);
				break;
			case "warn":
				if(publicConf.lwarm)
					log.warn(str);
				break;
			case "info":
				if(publicConf.linfo)
					log.info(str);
				break;
			case "debug":
				if(publicConf.ldebug)
					log.debug(str);
				break;
			case "trace":
				if(publicConf.ltrace)
					log.trace(str);
				break;
			default:
				break;
		}
	}*/
	
	@SuppressWarnings("unchecked")
	public static int findListMap(List<Map<String,String>> base,Object find,String keyword){
		int index = -1;
		String findStr = "";
		try{
			if(find instanceof Map){
				findStr = ((Map<String,String>) find).get(keyword);
			}else if(find instanceof String){
				findStr = (String) find;
			}
		}catch(Exception e){
			log.error(e.getMessage());
		}
		if(!findStr.isEmpty()){
			for(int i=0;i<base.size();i++){
				if(base.get(i).get(keyword).equals(findStr)){
					index = i;
					break;
				}
			}
		}
		return index;
	}
	
	public static int getAverageInt(List<String> stringList){
		if(stringList.size() > 0){
			int sum = 0;
			for(int i=0;i<stringList.size();i++){
				sum += Integer.parseInt(stringList.get(i));
			}
			return sum / stringList.size() + (sum % stringList.size() != 0 ? 1 : 0);
		}else{
			return 0;
		}
	}
	
	public static List<Map<String,String>> mergeList(List<Map<String,String>> bases,List<Map<String,String>> datas,String keyword){
    	Map<String,String> datasObject = null;
    	Map<String,String> basesObject = null;
    	Boolean flag = false;
    	if(bases.isEmpty()){
    		return datas;
    	}
    	if(datas.isEmpty()){
    		return bases;
    	}
    	for(int i=0;i<datas.size();i++){
    		datasObject = datas.get(i);
    		for(int j=0;j<bases.size();j++){
    			basesObject = bases.get(j);
    			if(datasObject.get(keyword) != null && datasObject.get(keyword).equals(basesObject.get(keyword))){
    				flag = true;
        			break;
    			}
    		}
    		if(flag){
    			basesObject.putAll(datasObject);
    			flag = false;
    		}else{
    			bases.add(datasObject);
    		}
    	}
    	return bases;
    }
	
	public static List<Map<String,String>> deletesList(List<Map<String,String>> bases,List<String> datas,String keyword){
    	String str = null;
    	Map<String,String> basesObject = null;
    	Boolean flag = false;
    	for(int i=0;i<datas.size();i++){
    		str = datas.get(i);
    		for(int j=0;j<bases.size();j++){
    			basesObject = bases.get(j);
    			if(basesObject.get(keyword) != null && basesObject.get(keyword).equals(str)){
    				flag = true;
        			break;
    			}
    		}
    		if(flag){
    			bases.remove(bases.indexOf(basesObject));
    			flag = false;
    		}
    	}
    	return bases;
    }
	
	public static Boolean compareVersion(String oldVer, String newVer){oldVer = "AC-V3.0-Build20230402155804";newVer = "AX820-AP-V3.0-Build20230818160845-ZHCN.ubin";
	    Boolean rs = false;
	    String oldVerStr="";
	    String newVerStr="";
	    Pattern p = Pattern.compile("(.*V)(\\d+.*)");
	    Matcher m = p.matcher(oldVer.toUpperCase());
	    while(m.find()){
	    	oldVerStr = m.group(2);
	    }
	    if(!oldVerStr.isEmpty()){
	    	m = p.matcher(newVer.toUpperCase());
	    	while(m.find()){
	    		newVerStr = m.group(2);
	    	}
	    	if(!newVerStr.isEmpty()){
	    		int lenth = newVerStr.length() > oldVerStr.length() ? oldVerStr.length() : newVerStr.length();
	    		for(int i=0;i<lenth;i++){
	    			if(newVerStr.charAt(i) > oldVerStr.charAt(i)){
	    				rs = true;
	    				break;
	    			}
	    		}
	    	}
	    }
	    return rs;
	}
	
	public static String encrypt(String str,String type){
		try{
			MessageDigest digest = MessageDigest.getInstance(type);
	        byte[] hashedBytes = digest.digest(str.getBytes());
	        
	        StringBuilder stringBuilder = new StringBuilder();
	        for (byte b : hashedBytes) {
	            stringBuilder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
	        }
	        return stringBuilder.toString();
		}catch(Exception e){
			log.error("encode failure");
			return str;
		}
	}
	
}
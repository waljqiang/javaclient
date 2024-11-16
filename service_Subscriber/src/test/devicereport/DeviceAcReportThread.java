package test.devicereport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.mqtt.client.QoS;

import test.com.Helper;

public class DeviceAcReportThread extends DeviceReportThread{
	
	private static final Log logger = LogFactory.getLog(DeviceAcReportThread.class);
	
	public DeviceAcReportThread(Device device,int waittime){
		super(device,waittime);
	}
	
	public void run(){
		if(waittime != 0){
			try{
				Thread.sleep(waittime * 1000);
			}catch(Exception e){
				logger.error("failure "+e.getMessage());
			}
		}
		
		while(true){
			String message = this.getMessage();
			try {
				mqttConnection.publish(topic_up, message.getBytes(), QoS.AT_LEAST_ONCE, false, null);
				logger.info("mqtt send message to device["+device.getIdenty()+"][" + device.getMac() + "] success");
			} catch (Exception e) {
				logger.error("Send message to device["+device.getMac()+"] failure,caused:"+e.getMessage());
			}
			
			try{
				Thread.sleep(publicConf.DEV_REPORT_INTERVAL*1000);
			}catch(Exception e){
				logger.error("The device["+device.getIdenty()+"]["+device.getMac()+"] updata failure,caused:"+e.getMessage());
			}
		}
	}

	@Override
	public String getBody() {
		String str = "{\"system\":"+this.getAcSystem()+",\"network\":"+this.getNetwork()+",\"time_reboot\":"+this.getTime_reboot()+"}";
		return str;
	}
	
	private String getAcSystem(){
		String mac = this.device.getMac();
		return "{\"name\":\"ceshi"+this.device.getIdenty()+"\",\"chip\":\""+deviceConf.DEV_AC_CHIP+"\",\"cpu\":\""+deviceConf.DEV_AC_CPU+"\",\"flash\":\""+deviceConf.DEV_AC_FLASH+"\",\"ram\":\""+deviceConf.DEV_AC_RAM+"\",\"cpu_use\":\""+Helper.getRandom(1,50)+"\",\"memory_use\":\""+Helper.getRandom(20,60)+"\","
				+ "\"mac\":\""+mac+"\",\"dev_ip\":\"192.168.10.2\",\"net_ip\":\"192.168.10.2\",\"version\":\""+deviceConf.DEV_AC_VERSION+"\",\"type\":\""+this.device.getType()+"\","
						+ "\"mode\":\""+deviceConf.DEV_AC_MODE+"\",\"ability\":"+deviceConf.DEV_AC_ABILITY+","
						+ "\"eth\":[{\"id\":\"br-lan\",\"mac\":\""+mac+"\"},{\"id\":\"rax0\",\"mac\":\""+mac+"\"},{\"id\":\"rax1\",\"mac\":\""+mac+"\"},{\"id\":\"rax2\",\"mac\":\""+mac+"\"},"
								+ "{\"id\":\"rax3\",\"mac\":\""+mac+"\"},{\"id\":\"ra0\",\"mac\":\""+mac+"\"},{\"id\":\"ra1\",\"mac\":\""+mac+"\"},{\"id\":\"ra2\",\"mac\":\""+mac+"\"},"
										+ "{\"id\":\"ra3\",\"mac\":\""+mac+"\"}],\"location\":{\"lat\":0,\"lng\":0},\"weblogin_pwd\":\""+deviceConf.DEV_AC_WEBLOGIN_PWD+"\",\"runtime\":\""+this.getRuntime()+"\","
												+ "\"log\":\""+deviceConf.DEV_AC_LOG+"\",\"alarm\":\""+deviceConf.DEV_AC_ALARM+"\",\"remote\":"+deviceConf.DEV_AC_REMOTE+"}";
	}
}

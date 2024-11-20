package deviceup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.QoS;

import conf.publicConf;
import deviceup.Device;
public class DeviceReportApThread extends DeviceReportThread{
	private static final Log logger = LogFactory.getLog(DeviceReportApThread.class);
   

	private int interval = publicConf.report_interval;

	public DeviceReportApThread(Device device){
	  super(device);
	}
	


	public void run(){
		try{
			while(true){
			  publishMessage();
			  Thread.sleep(2000);
			}
			
		}catch(Exception e){
			logger.fatal("receive message failure");
		}
	}
	
	
	public void publishMessage() {
		
		//logger.debug(device);
		String message =  this.getMessage();
		
		if(device.getWaittime() != 0){
			try{
				Thread.sleep(device.getWaittime() * 1000);
			}catch(Exception e){
				logger.error("failure "+e.getMessage());
			}
		}
	
		try {
			 Callback<Void> callback = new Callback<Void>() {
	            @Override
		        public void onSuccess(Void value) {
		              // 消息发送成功时的回调
		              logger.debug("mqtt send message to device[" + device.getIdenty() + "][" + device.getMac() + "] success");
		         }
				
				@Override
				public void onFailure(Throwable value) {
				      // 消息发送失败时的回调
				      logger.error("Send message to device[" + device.getMac() + "] failure, caused:" + value.getMessage(), value);
			    }
			};
			
			connection.publish(device.getPrtid() + "/" + device.getCltid() + "/dev2app", message.getBytes(), QoS.AT_LEAST_ONCE, false,callback);
			logger.info("mqtt send message to device["+device.getIdenty()+"][" + device.getMac() + "] success");
		} catch (Exception e) {
			logger.error("Send message to device["+device.getMac()+"] failure,caused:"+e.getMessage());
		}
		 
		
		try{
			Thread.sleep(interval*1000);
		}catch(Exception e){
			logger.error("The device["+device.getIdenty()+"]["+device.getMac()+"] updata failure,caused:"+e.getMessage());
		}
		
	   
	}

	@Override
	public String getBody() {
		return "{\"system\":" + getSystem(device.getMac(), device.getType()) + ",\"network\":" + getNetwork(device.getMac()) + ",\"wifi\":" + getWifi(device.getMac()) + ",\"time_reboot\":" + getTime_reboot(device.getMac()) + "}";
    }
	
   @Override
   public String getSystem(String mac, String type) {
	   return "{\"name\":\"ceshi" + mac + "\",\"chip\":\"MT7915\",\"cpu\":\"1GHz\",\"flash\":\"16\",\"ram\":\"2048\",\"cpu_use\":\"10\",\"memory_use\":\"28\",\"mac\":\"" + mac + "\",\"dev_ip\":\"192.168.10.2\",\"net_ip\":\"192.168.10.2\",\"version\":\"AX820-AP-V3.0-Build20230302155804\",\"type\":\"" + type + "\",\"mode\":\"5\",\"ability\":[\"00020001\",\"00040001\",\"00040002\",\"00060001\",\"00080001\",\"000C0001\",\"00070001\",\"00090001\",\"000D0005\",\"000D0006\",\"00030001\",\"00030003\"],\"eth\":[{\"id\":\"br-lan\",\"mac\":\"" + mac + "\"},{\"id\":\"rax0\",\"mac\":\"" + mac + "\"},{\"id\":\"rax1\",\"mac\":\"" + mac + "\"},{\"id\":\"rax2\",\"mac\":\"" + mac + "\"},{\"id\":\"rax3\",\"mac\":\"" + mac + "\"},{\"id\":\"ra0\",\"mac\":\"" + mac + "\"},{\"id\":\"ra1\",\"mac\":\"" + mac + "\"},{\"id\":\"ra2\",\"mac\":\"" + mac + "\"},{\"id\":\"ra3\",\"mac\":\"" + mac + "\"}],\"location\":{\"lat\":0,\"lng\":0},\"weblogin_pwd\":\"admin\",\"runtime\":\"56\",\"log\":\"1\",\"alarm\":\"0\"}";
    }
}

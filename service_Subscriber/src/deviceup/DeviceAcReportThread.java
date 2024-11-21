package deviceup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.mqtt.client.QoS;

import conf.deviceConf;

public class DeviceAcReportThread extends DeviceReportThread{
	
	private static final Log logger = LogFactory.getLog(DeviceAcReportThread.class);
	
	public DeviceAcReportThread(Device device){
		super(device);
	}
	
	public void run(){
		/*if(waittime != 0){
			try{
				Thread.sleep(waittime * 1000);
			}catch(Exception e){
				logger.error("failure "+e.getMessage());
			}
		}
		*/
		while(true){
			String message = this.getMessage();
//			try {
//				mqttConnection.publish(topic_up,message.getBytes(),QoS.AT_LEAST_ONCE,false);
//				logger.info("mqtt send message to device["+device.getIdenty()+"][" + device.getMac() + "] success");
//			} catch (Exception e) {
//				logger.error("Send message to device["+device.getMac()+"] failure,caused:"+e.getMessage());
//			}
			logger.info("mqtt send message to device["+device.getIdenty()+"][" + device.getMac() + "] success");
			
			try{
				//Thread.sleep(deviceConf.DEV_REPORT_INTERVAL*1000);
			}catch(Exception e){
				logger.error("The device["+device.getIdenty()+"]["+device.getMac()+"] updata failure,caused:"+e.getMessage());
			}
		}
	}

	@Override
	public String getBody() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getSystem(String mac, String type) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

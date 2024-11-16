package test.devicereport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;

public class DeviceManagerThread extends ReportBase implements Runnable{

	private static final Log logger = LogFactory.getLog(DeviceManagerThread.class);

	@Override
	public void run() {
		try{
			//AC设备
			if(publicConf.DEV_AC_NUM > 0){
				try{
					String mac = publicConf.DEV_AC_START_MAC;
					for(int i=1;i<=publicConf.DEV_AC_NUM;i++){
						Device device = this.getDeviceAc(mac,i);
						logger.info("The device["+device.getIdenty()+"]["+device.getMac()+"] prepared success");
						int reportwaittime = publicConf.DEV_REPORT_WAIT + this.getReportwait();
						DeviceAcReportThread deviceAcReportThr = new DeviceAcReportThread(device,reportwaittime);
						deviceAcReportThr.setName("dev-report-"+device.getIdenty());
						deviceAcReportThr.start();
						mac = this.getNextMac(mac);
					}
				}catch(Exception e){
					System.exit(0);
				}
				
			}
		}catch(Exception e){
			logger.error("process failure,caused:" + e.getMessage());
		}
	}
	
	private Device getDeviceAc(String mac,int identy) throws Exception{
		Device device = new Device();
		device.setIdenty(identy);
		device.setMac(mac);
		device.setPrtid(publicConf.PRODUCT_AC_PRTID);
		device.setType(publicConf.DEV_AC_TYPE);
		//获取客户端信息
		JSONObject client = this.getClient(device);
		if(client != null){
			device.setCltid(client.getString("cltid"));
			device.setMqtthost(client.getString("server"));
			device.setMqttport(client.getInteger("port"));
			//绑定设备
			String bind = this.bindDevice(device);
			if(!bind.isEmpty()){
				device.setBind(bind);
			}else{
				logger.info("The device["+device.getIdenty()+"]["+device.getMac()+"] prepared failure");
				throw new Exception("get bind code failure!");
			}
		}else{
			logger.info("The device["+device.getIdenty()+"]["+device.getMac()+"] prepared failure");
			throw new Exception("get client failure!");
		}
		return device;
	}
}

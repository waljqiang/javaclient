package test.devicereport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MainApp {

	private static final Log logger = LogFactory.getLog(MainApp.class);
	
	public static void main(String[] args) {
		try{
			Thread deviceManagerThr = new Thread(new DeviceManagerThread());
			deviceManagerThr.start();
		}catch(Exception e){
			logger.error("process failure,caused:" + e.getMessage());
		}
	}

}

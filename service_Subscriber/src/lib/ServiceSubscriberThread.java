package lib;

//重要的类:
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ServiceSubscriberThread extends Thread{

	private static final Log log = LogFactory.getLog(ServiceSubscriberThread.class);

	@Override
	public void run() {
		log.fatal("service start...");
		try {
			// 监听上报信息
			SubMessageThread subThr = new SubMessageThread();
			subThr.setName("subThr");
			subThr.start();
			//上线处理
			SubOnlineThread onlineThr = new SubOnlineThread();
			onlineThr.setName("onlineThr");
			onlineThr.start();
			//下线处理
			SubOfflineThread offlineThr = new SubOfflineThread();
			offlineThr.setName("offlineThr");
			offlineThr.start();
			//数据处理
			HandleDataThread handleDataThr = new HandleDataThread();
			handleDataThr.setName("handleDataThr");
			handleDataThr.start();
			//认证询问数据处理
			HandleAuthDataThread handleAuthDataThr = new HandleAuthDataThread();
			handleAuthDataThr.setName("handleAuthDataThr");
			handleAuthDataThr.start();
			//IP定位
			IplocationThread iplocationThr = new IplocationThread();
			iplocationThr.setName("iplocationThr");
			iplocationThr.start();
			//邮件处理
			SendEmailThread sendEmailThr = new SendEmailThread();
			sendEmailThr.setName("sendEmailThr");
			sendEmailThr.start();
			log.fatal("service start success");
		} catch (Exception e) {
			log.fatal("prossess failure:" + e.getMessage());
		}
	}

}

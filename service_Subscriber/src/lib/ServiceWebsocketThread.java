package lib;

//重要的类:
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ServiceWebsocketThread extends Thread {

	private static final Log log = LogFactory.getLog(ServiceWebsocketThread.class);

	@Override
	public void run() {
		log.fatal("Websocket start...");
		try {
			WebSocketThread webSocketThr = new WebSocketThread();
			webSocketThr.setName("webSocketThr");
			webSocketThr.start();
			Thread.sleep(1000);
			log.fatal("Websocket start success");
		} catch (Exception e) {
			//log.fatal("service_Subscriber sub 订阅处理" + e.getMessage());
			log.error("prossess failure:" + e.getMessage());
		}
	}

}

package service_Websocket;

//重要的类:
import lib.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class main {

	private static final Log log = LogFactory.getLog(main.class);

	public static void main(String[] args) {
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

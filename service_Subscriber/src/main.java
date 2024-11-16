//重要的类:
import lib.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class main {

	private static final Log log = LogFactory.getLog(main.class);

	public static void main(String[] args) {
		try {
			//Subscriber
			ServiceSubscriberThread subThr = new ServiceSubscriberThread();
			subThr.setName("serviceSubscriber");
			subThr.start();
			Thread.sleep(1000);
			//task
			ServiceTaskThread taskThr = new ServiceTaskThread();
			taskThr.setName("serviceTask");
			taskThr.start();
			Thread.sleep(1000);
			//websocket
			ServiceWebsocketThread websocketThr = new ServiceWebsocketThread();
			websocketThr.setName("serviceWebsocket");
			websocketThr.start();
			Thread.sleep(1000);
		} catch (Exception e) {
			//log.fatal("service_Subscriber sub 订阅处理" + e.getMessage());
			log.fatal("prossess failure:" + e.getMessage());
		}
	}

}

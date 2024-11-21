//重要的类:

import lib.threads.PerfThread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class main {

	private static final Log log = LogFactory.getLog(main.class);

	public static void main(String[] args) {
		try {
			PerfThread perft = new PerfThread();
			perft.start();
			
			Thread.sleep(1000);
		} catch (Exception e) {
			
			log.fatal("prossess failure:" + e.getMessage());
		}
	}

}

package test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.enums.ConnectionResult;
public class test {
	private static final Log log = LogFactory.getLog(test.class);

	public static void main(String[] args) {
		try {

			//SubMessageThread subThr = new SubMessageThread();
			//subThr.setName("subThr");
			//subThr.start();
			//Subscriber
			//TestHandleThread testHandleThr = new TestHandleThread();
			//testHandleThr.start();
			//Thread.sleep(1000);
			//Thread.sleep(1000);


			//检测redis服务是否异常  通过ping的方式检测 每秒检测一次
		   // TestHandleLinkRedisThread testHandleRedisThr = new TestHandleLinkRedisThread();
			//testHandleRedisThr.start();


			//检测mysql服务是否异常 通过连接查询一条sql的方式 每秒检测一次
			TestHandleLinkMysqlThread testHandleMysqlThr = new TestHandleLinkMysqlThread();
			testHandleMysqlThr.start();


			//检测emqx服务是否异常  通过建立一个客户端的方式 每秒检测一次
			//TestHandleLinkEmqxThread testHandleEmqxThr = new TestHandleLinkEmqxThread();
			//testHandleEmqxThr.start();
			//testHandleEmqxThr.stopThread();

			TestHandleLinkRedisThread testHandleEmqxThr = new TestHandleLinkRedisThread();

			testHandleEmqxThr.start();

			ConnectionResult result = testHandleEmqxThr.getConnectionResult();
			try {
				// 等待线程结束
				testHandleEmqxThr.join();
			} catch (InterruptedException e) {
				// 如果当前线程在等待过程中被中断，处理中断
				Thread.currentThread().interrupt();
			}


            // 获取线程的返回结果

			// 根据返回结果决定是否停止线程
			if (result == ConnectionResult.FAILURE || result == ConnectionResult.INTERRUPTED) {

				testHandleEmqxThr.stopThread();
			}




		} catch (Exception e) {
			//log.fatal("service_Subscriber sub 订阅处理" + e.getMessage());
			log.fatal("prossess failure:" + e.getMessage());
		}
	}



}

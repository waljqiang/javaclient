package service_Task;

//重要的类:
import lib.*;
import conf.publicConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class main {

	private static final Log log = LogFactory.getLog(main.class);

	public static void main(String[] args) {
		log.fatal("Timer task start...");
		try {
			//定时任务
			//经纬度定位任务
			TimertaskThread locationTaskThr = new TimertaskThread(new LocationExecutor(),publicConf.location_task_time,publicConf.location_interval);
			locationTaskThr.setName("locationTaskThr");
			locationTaskThr.start();
			//设备客户端统计任务
			TimertaskThread clientTaskThr = new TimertaskThread(new ClientStaticsExecutor(),publicConf.client_statics_task_time,publicConf.client_interval);
			clientTaskThr.setName("clientTaskThr");
			clientTaskThr.start();
			//批量绑定设备处理
			BindTaskThread bindTaskThr = new BindTaskThread();
			bindTaskThr.setName("bindTaskThr");
			bindTaskThr.start();		
			//激活设备数量采样任务
			TimertaskThread bindCollectTaskThr = new TimertaskThread(new BindCollectExecutor("day"),publicConf.binds_collect_task_time,publicConf.binds_collect_interval);
			bindCollectTaskThr.setName("bindCollectTaskThr");
			bindCollectTaskThr.start();
			//激活设备数量按天统计任务
			TimertaskThread bindsTaskDayThr = new TimertaskThread(new BindStatisticExecutor("day"),publicConf.binds_day_time,publicConf.binds_day_interval);
			bindsTaskDayThr.setName("bindsTaskDayThr");
			bindsTaskDayThr.start();
			//激活设备数量按周统计任务
			TimertaskThread bindsTaskWeekThr = new TimertaskThread(new BindStatisticExecutor("week"),publicConf.binds_week_time,publicConf.binds_week_interval);
			bindsTaskWeekThr.setName("bindsTaskWeekThr");
			bindsTaskWeekThr.start();
			//激活设备数量按月统计任务
			TimertaskThread bindsTaskMonthThr = new TimertaskThread(new BindStatisticExecutor("month"),publicConf.binds_month_time,publicConf.binds_month_interval);
			bindsTaskMonthThr.setName("bindsTaskMonthThr");
			bindsTaskMonthThr.start();
			//激活设备数量按季度统计任务
			TimertaskThread bindsTaskQuarterThr = new TimertaskThread(new BindStatisticExecutor("quarter"),publicConf.binds_quarter_time,publicConf.binds_quarter_interval);
			bindsTaskQuarterThr.setName("bindsTaskQuarterThr");
			bindsTaskQuarterThr.start();
			//激活设备数量按年统计任务
			TimertaskThread bindsTaskYearThr = new TimertaskThread(new BindStatisticExecutor("year"),publicConf.binds_year_time,publicConf.binds_year_interval);
			bindsTaskYearThr.setName("bindsTaskYearThr");
			bindsTaskYearThr.start();
			
			//设备在线数量采集任务
			TimertaskThread onlineCollectTaskThr = new TimertaskThread(new OnlineCollectExecutor("day"),publicConf.onlines_collect_task_time,publicConf.onlines_collect_interval);
			onlineCollectTaskThr.setName("onlineCollectTaskThr");
			onlineCollectTaskThr.start();
			//设备在线数量按天统计任务
			TimertaskThread onlinesTaskDayThr =  new TimertaskThread(new OnlineStatisticExecutor("day"),publicConf.onlines_day_time,publicConf.onlines_day_interval);
			onlinesTaskDayThr.setName("onlinesTaskDayThr");
			onlinesTaskDayThr.start();
			//设备在线数量按周统计任务
			TimertaskThread onlinesTaskWeekThr =  new TimertaskThread(new OnlineStatisticExecutor("week"),publicConf.onlines_week_time,publicConf.onlines_week_interval);
			onlinesTaskWeekThr.setName("onlinesTaskWeekThr");
			onlinesTaskWeekThr.start();
			//设备在线数量按月统计任务
			TimertaskThread onlinesTaskMonthThr =  new TimertaskThread(new OnlineStatisticExecutor("month"),publicConf.onlines_month_time,publicConf.onlines_month_interval);
			onlinesTaskMonthThr.setName("onlinesTaskMonthThr");
			onlinesTaskMonthThr.start();
			//设备在线数量按季度统计任务
			TimertaskThread onlinesTaskQuarterThr =  new TimertaskThread(new OnlineStatisticExecutor("quarter"),publicConf.onlines_quarter_time,publicConf.onlines_quarter_interval);
			onlinesTaskQuarterThr.setName("onlinesTaskQuarterThr");
			onlinesTaskQuarterThr.start();
			//设备在线数量按年统计任务
			TimertaskThread onlinesTaskYearThr =  new TimertaskThread(new OnlineStatisticExecutor("year"),publicConf.onlines_year_time,publicConf.onlines_year_interval);
			onlinesTaskYearThr.setName("onlinesTaskYearThr");
			onlinesTaskYearThr.start();
			
			Thread.sleep(1000);
			log.fatal("Timer task start success");
		} catch (Exception e) {
			//log.fatal("service_Subscriber sub 订阅处理" + e.getMessage());
			log.error("prossess failure:" + e.getMessage());
		}
	}

}

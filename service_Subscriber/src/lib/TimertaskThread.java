package lib;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//任务处理
public class TimertaskThread extends Thread{
	private static final Log log = LogFactory.getLog(TimertaskThread.class);
	private String start_time;
	private long interval;
	private ExcutorTask excutorTask;
	
	public TimertaskThread(ExcutorTask excutorTask,String start_time,long interval){
		this.start_time = start_time;
		this.interval = interval;
		this.excutorTask = excutorTask;
	}
	
	@Override
	public void run() {
		long initialDelay;
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		String[] startDate = this.start_time.split(" ");
		if(startDate.length == 2){
			String[] startDateFirst = startDate[0].split("-");
			String[] startDateSecond = startDate[1].split(":");
			if(startDateFirst.length == 3 && startDateSecond.length == 3){
				Date ldate = new Date();
				Calendar cal = Calendar.getInstance();
				String pattern = "-?[0-9]+(\\\\.[0-9]+)?";
				if(Pattern.matches(pattern, startDateFirst[0])){//年
					cal.set(Calendar.YEAR,Integer.parseInt(startDateFirst[0]));
				}
				if(Pattern.matches(pattern, startDateFirst[1])){//月
					cal.set(Calendar.MONTH,Integer.parseInt(startDateFirst[1])-1);
				}
				if(Pattern.matches(pattern, startDateFirst[2])){//天
					cal.set(Calendar.DATE,Integer.parseInt(startDateFirst[2]));
				}
				if(Pattern.matches(pattern, startDateSecond[0])){//时
					cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(startDateSecond[0]));
				}
				if(Pattern.matches(pattern, startDateSecond[1])){//分
					cal.set(Calendar.MINUTE,Integer.parseInt(startDateSecond[1]));
				}
				if(Pattern.matches(pattern, startDateSecond[2])){//秒
					cal.set(Calendar.SECOND,Integer.parseInt(startDateSecond[2]));
				}
				initialDelay = (cal.getTimeInMillis()-ldate.getTime()) / 1000;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				if(initialDelay < 0){
					if(-initialDelay / (cal.getActualMaximum(Calendar.DAY_OF_MONTH) * 86400) > 0){
						cal.add(Calendar.YEAR, 1);
					}else if(-initialDelay / 86400 > 0){
						cal.add(Calendar.MONTH, 1);
					}else if(-initialDelay / 3600 > 0){
						cal.add(Calendar.DAY_OF_YEAR, 1);
					}else if(-initialDelay / 60 > 0){
						cal.add(Calendar.HOUR, 1);
					}else{
						cal.add(Calendar.MINUTE, 1);
					}
				}
				
				initialDelay = (cal.getTimeInMillis()-ldate.getTime()) / 1000;
				service.scheduleAtFixedRate(this.excutorTask, initialDelay, this.interval, TimeUnit.SECONDS);
		        log.fatal("Task["+this.excutorTask.getName()+"] started begin["+sdf.format(cal.getTime())+"]with interval["+this.interval+"]");
			}
		}

	}
}
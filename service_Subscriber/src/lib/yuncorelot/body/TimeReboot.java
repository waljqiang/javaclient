package lib.yuncorelot.body;

import lib.yuncorelot.Base;

public class TimeReboot extends Base{
	private String enable;
	private String time;
	
	public void setEnable(String enable){
		this.enable = enable;
	}
	
	public String getEnable(){
		return this.enable;
	}
	
	public void setTime(String time){
		this.time = time;
	}
	
	public String getTime(){
		return this.time;
	}
}
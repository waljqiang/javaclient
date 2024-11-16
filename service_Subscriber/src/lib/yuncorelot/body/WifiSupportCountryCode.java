package lib.yuncorelot.body;

import java.util.List;

import lib.yuncorelot.Base;

public class WifiSupportCountryCode extends Base{
	private String code;
	private List<String> channel;
	private List<String> phymode;
	
	public void setCode(String code){
		this.code = code;
	}
	
	public String getCode(){
		return this.code;
	}
	
	public void setChannel(List<String> channel){
		this.channel = channel;
	}
	
	public List<String> getChannel(){
		return this.channel;
	}
	
	public void setPhymode(List<String> phymode){
		this.phymode = phymode;
	}
	
	public List<String> getPhymode(){
		return this.phymode;
	}
}
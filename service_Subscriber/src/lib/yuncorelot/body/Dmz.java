package lib.yuncorelot.body;

import lib.yuncorelot.Base;

public class Dmz extends Base{
	protected String enable;
	protected String address;
	
	public void setEnable(String enable){
		this.enable = enable;
	}
	
	public String getEnable(){
		return this.enable;
	}
	
	public void setAddress(String address){
		this.address = address;
	}
	
	public String getAddress(){
		return this.address;
	}
	
}
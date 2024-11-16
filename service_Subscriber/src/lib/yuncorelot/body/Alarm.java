package lib.yuncorelot.body;

import lib.yuncorelot.Base;

public class Alarm extends Base{
	private String type;
	private String value;
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public String toString(){
		return super.toString();
	}
}
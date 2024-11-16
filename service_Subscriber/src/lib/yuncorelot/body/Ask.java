package lib.yuncorelot.body;

import java.util.List;

import lib.yuncorelot.Base;

public class Ask extends Base{
	private String type;
	private List<String> child;
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setChild(List<String> child){
		this.child = child;
	}
	
	public List<String> getChild(){
		return this.child;
	}
	
	public String toString(){
		return super.toString();
	}
}
package lib.yuncorelot.body;

import lib.yuncorelot.Base;

public class CommResult extends Base{
	private String commid;
	private String status;
	
	public void setCommid(String commid){
		this.commid = commid;
	}
	public String getCommid(){
		return this.commid;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
	
	public String getStatus(){
		return this.status;
	}
	
}
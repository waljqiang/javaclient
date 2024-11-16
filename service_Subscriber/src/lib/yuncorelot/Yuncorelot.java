package lib.yuncorelot;

import lib.yuncorelot.Base;

public class Yuncorelot extends Base{
	private Header header;
	private Body body;
	private String now;
	
	public void setHeader(Header header){
		this.header = header;
	}
	
	public Header getHeader(){
		return this.header;
	}
	
	public void setBody(Body body){
		this.body = body;
	}
	
	public Body getBody(){
		return this.body;
	}
	
	public void setNow(String now){
		this.now = now;
	}
	
	public String getNow(){
		return this.now;
	}
	
/*	public String toString(){
		return "{\"header\":"+this.header.toString()+",\"body\":"+this.body.toString()+",\"now\":\""+this.now+"\"}";
	}*/
}
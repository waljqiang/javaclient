package lib.yuncorelot;

import lib.yuncorelot.Base;

public class Message extends Base{
	private String prtid;
	private String cltid;
	private String mac;
	private Yuncorelot data;
	
	public void setPrtid(String prtid){
		this.prtid = prtid;
	}
	
	public String getPrtid(){
		return this.prtid;
	}
	
	public void setCltid(String cltid){
		this.cltid = cltid;
	}
	
	public String getCltid(){
		return this.cltid;
	}
	
	public void setMac(String mac){
		this.mac = mac;
	}
	
	public String getMac(){
		return this.mac;
	}
	
	public void setData(Yuncorelot data){
		this.data = data;
	}
	
	public Yuncorelot getData(){
		return this.data;
	}
	
	/*public String toString(){
		return "{\"prtid\":\""+this.prtid+"\",\"cltid\":\""+this.cltid+"\",\"mac\":\""+this.mac+"\",\"data\":"+this.data.toString()+"}";
	}*/
}
package lib.yuncorelot.body;

import lib.yuncorelot.Base;

public class Auth extends Base{
	private String dev_ip;
	private String radioid;
	private String vapid;
	private String client_mac;
	private String client_ip;
	private String token;
	private String status;
	
	public void setDev_ip(String dev_ip){
		this.dev_ip = dev_ip;
	}
	
	public String getDev_ip(){
		return this.dev_ip;
	}
	
	public void setRadioid(String radioid){
		this.radioid = radioid;
	}
	
	public String getRadioid(){
		return this.radioid;
	}
	
	public void setVapid(String vapid){
		this.vapid = vapid;
	}
	
	public String getVapid(){
		return this.vapid;
	}
	
	public void setClient_mac(String client_mac){
		this.client_mac = client_mac;
	}
	
	public String getClient_mac(){
		return this.client_mac;
	}
	
	public void setClient_ip(String client_ip){
		this.client_ip = client_ip;
	}
	
	public String getClient_ip(){
		return this.client_ip;
	}
	
	public void setToken(String token){
		this.token = token;
	}
	
	public String getToken(){
		return this.token;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
	
	public String getStatus(){
		return this.status;
	}
	
	public String toString(){
		return super.toString();
	}
}
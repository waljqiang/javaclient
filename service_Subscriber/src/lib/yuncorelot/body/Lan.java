package lib.yuncorelot.body;

import java.util.List;

import lib.yuncorelot.Base;

public class Lan extends Base{
	private String ip;
	private String subnet;
	private String mac;
	private String gateway;
	private List<String> dns;
	private String dhcp_enable;
	private String dhcp_ip_start;
	private String dhcp_ip_end;
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public String getIp(){
		return this.ip;
	}
	
	public void setSubnet(String subnet){
		this.subnet = subnet;
	}
	
	public String getSubnet(){
		return this.subnet;
	}
	
	public void setMac(String mac){
		this.mac = mac;
	}
	
	public String getMac(){
		return this.mac;
	}
	
	public void setGateway(String gateway){
		this.gateway = gateway;
	}
	
	public String getGateway(){
		return this.gateway;
	}
	
	public void setDns(List<String> dns){
		this.dns = dns;
	}
	
	public List<String> getDns(){
		return this.dns;
	}
	
	public void setDhcp_enable(String dhcp_enable){
		this.dhcp_enable = dhcp_enable;
	}
	
	public String getDhcp_enable(){
		return this.dhcp_enable;
	}
	
	public void setDhcp_ip_start(String dhcp_ip_start){
		this.dhcp_ip_start = dhcp_ip_start;
	}
	
	public String getDhcp_ip_start(){
		return this.dhcp_ip_start;
	}
	
	public void setDhcp_ip_end(String dhcp_ip_end){
		this.dhcp_ip_end = dhcp_ip_end;
	}
	
	public String getDhcp_ip_end(){
		return this.dhcp_ip_end;
	}
}
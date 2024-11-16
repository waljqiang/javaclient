package lib.yuncorelot.body;

import java.util.List;
import java.util.Map;

import lib.yuncorelot.Base;

public class Wan extends Base{
	private String id;
	private String status;
	private String conn_type;
	private String ip;
	private String subnet;
	private String mac;
	private String gateway;
	private List<String> dns;
	private Map<String,String> sim_card;
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
	
	public String getStatus(){
		return this.status;
	}
	
	public void setConn_type(String conn_type){
		this.conn_type = conn_type;
	}
	
	public String getConn_type(){
		return this.conn_type;
	}
	
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
	
	public void setSim_card(Map<String,String> sim_card){
		this.sim_card = sim_card;
	}
	
	public Map<String,String> getSim_card(){
		return this.sim_card;
	}
}
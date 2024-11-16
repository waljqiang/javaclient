package lib.yuncorelot.body;

import java.util.List;
import java.util.Map;

import lib.yuncorelot.Base;

public class Network extends Base{
	private String policy;
	private List<Wan> wan;
	private List<Lan> lan;
	private Map<String,String> acdhcp;
	
	public void setPolicy(String policy){
		this.policy = policy;
	}
	
	public String getPolicy(){
		return this.policy;
	}
	
	public void setWan(List<Wan> wan){
		this.wan = wan;
	}
	
	public List<Wan> getWan(){
		return this.wan;
	}
	
	public void setLan(List<Lan> lan){
		this.lan = lan;
	}
	
	public List<Lan> getLan(){
		return this.lan;
	}
	
	public void setAcdhcp(Map<String,String> acdhcp){
		this.acdhcp = acdhcp;
	}
	
	public Map<String,String> getAcdhcp(){
		return this.acdhcp;
	}
	
}
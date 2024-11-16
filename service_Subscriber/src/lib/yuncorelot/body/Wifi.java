package lib.yuncorelot.body;

import java.util.List;
import java.util.Map;

import lib.yuncorelot.Base;

public class Wifi extends Base{
	private String total;
	private List<WifiRadio> radios;
	private Map<String,String> timer;
	
	public void setTotal(String total){
		this.total = total;
	}
	
	public String getTotal(){
		return this.total;
	}
	
	public void setRadios(List<WifiRadio> radios){
		this.radios = radios;
	}
	
	public List<WifiRadio> getRadios(){
		return this.radios;
	}
	
	public void setTimer(Map<String,String> timer){
		this.timer = timer;
	}
	
	public Map<String,String> getTimer(){
		return this.timer;
	}
}
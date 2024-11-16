package lib.yuncorelot.body;

import java.util.List;

import lib.yuncorelot.Base;

public class WifiSupport extends Base{
	private List<WifiSupportCountryCode> country_code;
	private List<String> encode;
	private List<String> phymode;
	private String max_sta;
	private String radio_password_regex;
	private String is_dfs;
	private String is_distance;
	
	public void setCountry_code(List<WifiSupportCountryCode> country_code){
		this.country_code = country_code;
	}
	
	public List<WifiSupportCountryCode> getCountry_code(){
		return this.country_code;
	}
	
	public void setEncode(List<String> encode){
		this.encode = encode;
	}
	
	public List<String> getEncode(){
		return this.encode;
	}
	
	public void setPhymode(List<String> phymode){
		this.phymode = phymode;
	}
	
	public List<String> getPhymode(){
		return this.phymode;
	}
	
	public void setMax_sta(String max_sta){
		this.max_sta = max_sta;
	}
	
	public String getMax_sta(){
		return this.max_sta;
	}
	
	public void setRadio_password_regex(String radio_password_regex){
		this.radio_password_regex = radio_password_regex;
	}
	
	public String getRadio_password_regex(){
		return this.radio_password_regex;
	}
	
	public void setIs_dfs(String is_dfs){
		this.is_dfs = is_dfs;
	}
	
	public String getIs_dfs(){
		return this.is_dfs;
	}
	
	public void setIs_distance(String is_distance){
		this.is_distance = is_distance;
	}
	
	public String getIs_distance(){
		return this.is_distance;
	}
}
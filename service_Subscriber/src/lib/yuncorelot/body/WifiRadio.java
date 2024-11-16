package lib.yuncorelot.body;

import java.util.List;
import java.util.Map;

import lib.yuncorelot.Base;

public class WifiRadio extends Base{
	private String radioid;
	private String country_code;
	private String total;
	private String radio_type;
	private String channel;
	private String channel_config;
	private String power;
	private String phymode;
	private String coveragethreshold;
	private String user_isolate;
	private String frag_threshold;
	private String rts_threshold;
	private String beacon_interval;
	private String shortgi;
	private String max_sta;
	private String dfs;
	private String distance;
	private WifiSupport support;
	private List<WifiRadioVap> vap;
	private Map<String,String> client;
	
	public void setRadioid(String radioid){
		this.radioid = radioid;
	}
	
	public String getRadioid(){
		return this.radioid;
	}
	
	public void setCountry_code(String country_code){
		this.country_code = country_code;
	}
	
	public String getCountry_code(){
		return this.country_code;
	}
	
	public void setTotal(String total){
		this.total = total;
	}
	
	public String getTotal(){
		return this.total;
	}
	
	public void setRadio_type(String radio_type){
		this.radio_type = radio_type;
	}
	
	public String getRadio_type(){
		return this.radio_type;
	}
	
	public void setChannel(String channel){
		this.channel = channel;
	}
	
	public String getChannel(){
		return this.channel;
	}
	
	public void setChannel_config(String channel_config){
		this.channel_config = channel_config;
	}
	
	public String getChannel_config(){
		return this.channel_config;
	}
	
	public void setPower(String power){
		this.power = power;
	}
	
	public String getPower(){
		return this.power;
	}
	
	public void setPhymode(String phymode){
		this.phymode = phymode;
	}
	
	public String getPhymode(){
		return this.phymode;
	}
	
	public void setCoveragethreshold(String coveragethreshold){
		this.coveragethreshold = coveragethreshold;
	}
	
	public String getCoveragethreshold(){
		return this.coveragethreshold;
	}
	
	public void setUser_isolate(String user_isolate){
		this.user_isolate = user_isolate;
	}
	
	public String getUser_isolate(){
		return this.user_isolate;
	}
	
	public void setFrag_threshold(String frag_threshold){
		this.frag_threshold = frag_threshold;
	}
	
	public String getFrag_threshold(){
		return this.frag_threshold;
	}
	
	public void setRts_threshold(String rts_threshold){
		this.rts_threshold = rts_threshold;
	}
	
	public String getRts_threshold(){
		return this.rts_threshold;
	}
	
	public void setBeacon_interval(String beacon_interval){
		this.beacon_interval = beacon_interval;
	}
	
	public String getBeacon_interval(){
		return this.beacon_interval;
	}
	
	public void setShortgi(String shortgi){
		this.shortgi = shortgi;
	}
	
	public String getShortgi(){
		return this.shortgi;
	}
	
	public void setMax_sta(String max_sta){
		this.max_sta = max_sta;
	}
	
	public String getMax_sta(){
		return this.max_sta;
	}
	
	public void setDfs(String dfs){
		this.dfs = dfs;
	}
	
	public String getDfs(){
		return this.dfs;
	}
	
	public void setDistance(String distance){
		this.distance = distance;
	}
	
	public String getDistance(){
		return this.distance;
	}
	
	public void setSupport(WifiSupport support){
		this.support = support;
	}
	
	public WifiSupport getSupport(){
		return this.support;
	}
	
	public void setVap(List<WifiRadioVap> vap){
		this.vap = vap;
	}
	
	public List<WifiRadioVap> getVap(){
		return this.vap;
	}
	
	public void setClient(Map<String,String> client){
		this.client = client;
	}
	
	public Map<String,String> getClient(){
		return this.client;
	}
}
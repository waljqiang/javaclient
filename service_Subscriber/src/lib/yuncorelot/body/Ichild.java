package lib.yuncorelot.body;

import java.util.List;
import lib.yuncorelot.Base;

public class Ichild extends Base{
	private String mac;
	private System system;
	private Network network;
	private Wifi wifi;
	private User user;
	private TimeReboot time_reboot;
	private List<Alarm> alarm;
	private Auth auth;
	private TimeGroup time_group;
	private IpGroup ip_group;
	private RateLimit rate_limit;
	private FilterUrl filter_url;
	private FilterIpport filter_ipport;
	private FilterMac filter_mac;
	private IpportMap ipport_map;
	private ClientConfig client_config;
	
	public void setMac(String mac){
		this.mac = mac;
	}
	
	public String getMac(){
		return this.mac;
	}
	
	public void setSystem(System system){
		this.system = system;
	}
	
	public System getSystem(){
		return this.system;
	}
	
	public void setNetwork(Network network){
		this.network = network;
	}
	
	public Network getNetwork(){
		return this.network;
	}
	
	public void setWifi(Wifi wifi){
		this.wifi = wifi;
	}
	
	public Wifi getWifi(){
		return this.wifi;
	}
	
	public void setUser(User user){
		this.user = user;
	}
	
	public User getUser(){
		return this.user;
	}
	
	public void setTime_reboot(TimeReboot time_reboot){
		this.time_reboot = time_reboot;
	}
	
	public TimeReboot getTime_reboot(){
		return this.time_reboot;
	}
	
	public void setAlarm(List<Alarm> alarm){
		this.alarm = alarm;
	}
	
	public List<Alarm> getAlarm(){
		return this.alarm;
	}
	
	public void setAuth(Auth auth){
		this.auth = auth;
	}
	
	public Auth getAuth(){
		return this.auth;
	}
	
	public void setTime_group(TimeGroup time_group){
		this.time_group = time_group;
	}
	
	public TimeGroup getTime_group(){
		return this.time_group;
	}
	
	public void setIp_group(IpGroup ip_group){
		this.ip_group = ip_group;
	}
	
	public IpGroup getIp_group(){
		return this.ip_group;
	}
	
	public void setRate_limit(RateLimit rate_limit){
		this.rate_limit = rate_limit;
	}
	
	public RateLimit getRate_limit(){
		return this.rate_limit;
	}
	
	public void setFilter_url(FilterUrl filter_url){
		this.filter_url = filter_url;
	}
	
	public FilterUrl getFilter_url(){
		return this.filter_url;
	}
	
	public void setFilter_ipport(FilterIpport filter_ipport){
		this.filter_ipport = filter_ipport;
	}
	
	public FilterIpport getFilter_iport(){
		return this.filter_ipport;
	}
	
	public void setFilter_mac(FilterMac filter_mac){
		this.filter_mac = filter_mac;
	}
	
	public FilterMac getFilter_mac(){
		return this.filter_mac;
	}
	
	public void setIpport_map(IpportMap ipport_map){
		this.ipport_map = ipport_map;
	}
	
	public IpportMap getIpport_map(){
		return this.ipport_map;
	}
	
	public void setClient_config(ClientConfig client_config){
		this.client_config = client_config;
	}
	
	public ClientConfig getClient_config(){
		return this.client_config;
	}

}
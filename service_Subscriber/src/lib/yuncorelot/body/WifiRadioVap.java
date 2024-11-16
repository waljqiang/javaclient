package lib.yuncorelot.body;

import lib.yuncorelot.Base;

public class WifiRadioVap extends Base{
	private String id;
	private String enable;
	private String vlan_id;
	private String bssid;
	private String ssid;
	private String ssid_hide;
	private String encode;
	private String password;
	private String radius_key;
	private String radius_server;
	private String radius_port;
	private String radius_acct_key;
	private String radius_acct_server;
	private String radius_acct_port;
	private String radius_nai_realm;
	private String radius_domain;
	private String radius_network_auth_type;
	private String radius_venue_name;
	private WifiRadioVapUsers users;
	
	public void setId(String id){
		this.id = id;
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setEnable(String enable){
		this.enable = enable;
	}
	
	public String getEnable(){
		return this.enable;
	}
	
	public void setVlan_id(String vlan_id){
		this.vlan_id = vlan_id;
	}
	
	public String getVlan_id(){
		return this.vlan_id;
	}
	
	public void setBssid(String bssid){
		this.bssid = bssid;
	}
	
	public String getBssid(){
		return this.bssid;
	}
	
	public void setSsid(String ssid){
		this.ssid = ssid;
	}
	
	public String getSsid(){
		return this.ssid;
	}
	
	public void setSsid_hide(String ssid_hide){
		this.ssid_hide = ssid_hide;
	}
	
	public String getSsid_hide(){
		return this.ssid_hide;
	}
	
	public void setEncode(String encode){
		this.encode = encode;
	}
	
	public String getEncode(){
		return this.encode;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	public void setUsers(WifiRadioVapUsers users){
		this.users = users;
	}
	
	public WifiRadioVapUsers getUsers(){
		return this.users;
	}

	public String getRadius_key() {
		return radius_key;
	}

	public void setRadius_key(String radius_key) {
		this.radius_key = radius_key;
	}

	public String getRadius_server() {
		return radius_server;
	}

	public void setRadius_server(String radius_server) {
		this.radius_server = radius_server;
	}

	public String getRadius_port() {
		return radius_port;
	}

	public void setRadius_port(String radius_port) {
		this.radius_port = radius_port;
	}

	public String getRadius_acct_key() {
		return radius_acct_key;
	}

	public void setRadius_acct_key(String radius_acct_key) {
		this.radius_acct_key = radius_acct_key;
	}

	public String getRadius_acct_server() {
		return radius_acct_server;
	}

	public void setRadius_acct_server(String radius_acct_server) {
		this.radius_acct_server = radius_acct_server;
	}

	public String getRadius_acct_port() {
		return radius_acct_port;
	}

	public void setRadius_acct_port(String radius_acct_port) {
		this.radius_acct_port = radius_acct_port;
	}

	public String getRadius_nai_realm() {
		return radius_nai_realm;
	}

	public void setRadius_nai_realm(String radius_nai_realm) {
		this.radius_nai_realm = radius_nai_realm;
	}

	public String getRadius_domain() {
		return radius_domain;
	}

	public void setRadius_domain(String radius_domain) {
		this.radius_domain = radius_domain;
	}

	public String getRadius_network_auth_type() {
		return radius_network_auth_type;
	}

	public void setRadius_network_auth_type(String radius_network_auth_type) {
		this.radius_network_auth_type = radius_network_auth_type;
	}

	public String getRadius_venue_name() {
		return radius_venue_name;
	}

	public void setRadius_venue_name(String radius_venue_name) {
		this.radius_venue_name = radius_venue_name;
	}
}
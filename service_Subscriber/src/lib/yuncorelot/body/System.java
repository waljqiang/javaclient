package lib.yuncorelot.body;

import java.util.List;
import java.util.Map;

import lib.yuncorelot.Base;

public class System extends Base{
	private String name;
	private String chip;
	private String cpu;
	private String flash;
	private String ram;
	private String cpu_use;
	private String memory_use;
	private String mac;
	private String dev_ip;
	private String net_ip;
	private String version;
	private String prtid;
	private String type;
	private String mode;
	private List<String> ability;
	private String status;
	private Map<String,String> parent;
	private Map<String,String> location;
	private List<Map<String,String>> eth;
	private String dev_username;
	private String weblogin_pwd;
	private String log;
	private String runtime;
	private Map<String,String> remote;
	private String rebind;
	private String is_forceunbind;
	private String alarm;
	private String hwnat_enable;
	private String lang;
	private String sn;
	private String is_tdma;
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setChip(String chip){
		this.chip = chip;
	}
	
	public String getChip(){
		return this.chip;
	}
	
	public void setCpu(String cpu){
		this.cpu = cpu;
	}
	
	public String getCpu(){
		return this.cpu;
	}
	
	public void setFlash(String flash){
		this.flash = flash;
	}
	
	public String getFlash(){
		return this.flash;
	}
	
	public void setRam(String ram){
		this.ram = ram;
	}
	
	public String getRam(){
		return this.ram;
	}
	
	public void setCpu_use(String cpu_use){
		this.cpu_use = cpu_use;
	}
	
	public String getCpu_use(){
		return this.cpu_use;
	}
	
	public void setMemory_use(String memory_use){
		this.memory_use = memory_use;
	}
	
	public String getMemory_use(){
		return this.memory_use;
	}
	
	public void setMac(String mac){
		this.mac = mac;
	}
	
	public String getMac(){
		return this.mac;
	}
	
	public void setDev_ip(String dev_ip){
		this.dev_ip = dev_ip;
	}
	
	public String getDev_ip(){
		return this.dev_ip;
	}
	
	public void setNet_ip(String net_ip){
		this.net_ip = net_ip;
	}
	
	public String getNet_ip(){
		return this.net_ip;
	}
	
	public void setVersion(String version){
		this.version = version;
	}
	
	public String getVersion(){
		return this.version;
	}
	
	public void setPrtid(String prtid){
		this.prtid = prtid;
	}
	
	public String getPrtid(){
		return this.prtid;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setMode(String mode){
		this.mode = mode;
	}
	
	public String getMode(){
		return this.mode;
	}
	
	public void setAbility(List<String> ability){
		this.ability = ability;
	}
	
	public List<String> getAbility(){
		return this.ability;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
	
	public String getStatus(){
		return this.status;
	}
	
	public void setParent(Map<String,String> parent){
		this.parent = parent;
	}
	
	public Map<String,String> getParent(){
		return this.parent;
	}
	
	public void setLocation(Map<String,String> location){
		this.location = location;
	}
	
	public Map<String,String> getLocation(){
		return this.location;
	}
	
	public void setEth(List<Map<String,String>> eth){
		this.eth = eth;
	}
	
	public List<Map<String,String>> getEth(){
		return this.eth;
	}
	
	public void setDev_username(String username){
		this.dev_username = username;
	}
	
	public String getDev_username(){
		return this.dev_username;
	}
	
	public void setWeblogin_pwd(String weblogin_pwd){
		this.weblogin_pwd = weblogin_pwd;
	}
	
	public String getWeblogin_pwd(){
		return this.weblogin_pwd;
	}
	
	public void setLog(String log){
		this.log = log;
	}
	
	public String getLog(){
		return this.log;
	}
	
	public void setRuntime(String runtime){
		this.runtime = runtime;
	}
	
	public String getRuntime(){
		return this.runtime;
	}
	
	public void setRemote(Map<String,String> remote){
		this.remote = remote;
	}
	
	public Map<String,String> getRemote(){
		return this.remote;
	}
	
	public void setRebind(String rebind){
		this.rebind = rebind;
	}
	
	public String getRebind(){
		return this.rebind;
	}
	
	public void setIs_forceunbind(String is_forceunbind){
		this.is_forceunbind = is_forceunbind;
	}
	
	public String getIs_forceunbind(){
		return this.is_forceunbind;
	}
	
	public void setLang(String lang){
		this.lang = lang;
	}
	
	public String getLang(){
		return this.lang;
	}
	
	public void setAlarm(String alarm){
		this.alarm = alarm;
	}
	
	public String getAlarm(){
		return this.alarm;
	}
	
	public void setHwnat_enable(String hwnat_enable){
		this.hwnat_enable = hwnat_enable;
	}
	
	public String getHwnat_enable(){
		return this.hwnat_enable;
	}
	
	public void setSn(String sn){
		this.sn = sn;
	}
	
	public String getSn(){
		return this.sn;
	}
	
	public void setIstdma(String is_tdma){
		this.is_tdma = is_tdma;
	}
	
	public String getIs_tdma(){
		return this.is_tdma;
	}
}
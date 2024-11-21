package deviceup;

public class Device extends Base{
	private String identy;
	private String prtid;
	private String cltid;
	private String mac;
	private String type;
	private String bind;
	private String mqtthost;
	private int mqttport;
	private int report_interval;
	private int waittime;
	
	public String getIdenty() {
		return identy;
	}
	public void setIdenty(String identy) {
		this.identy = identy;
	}
	public String getPrtid() {
		return prtid;
	}
	public void setPrtid(String prtid) {
		this.prtid = prtid;
	}
	public String getCltid() {
		return cltid;
	}
	public void setCltid(String cltid) {
		this.cltid = cltid;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBind() {
		return bind;
	}
	public void setBind(String bind) {
		this.bind = bind;
	}
	public String getMqtthost() {
		return mqtthost;
	}
	public void setMqtthost(String mqtthost) {
		this.mqtthost = mqtthost;
	}
	public int getMqttport() {
		return mqttport;
	}
	public void setMqttport(int mqttport) {
		this.mqttport = mqttport;
	}
	public int getWaittime() {
		return waittime;
	}
	public void setWaittime(int waittime) {
		this.waittime = waittime;
	}
	public int getReportInterval() {
		return report_interval;
	}
	public void setReportInterval(int report_interval) {
		this.report_interval = report_interval;
	}
}

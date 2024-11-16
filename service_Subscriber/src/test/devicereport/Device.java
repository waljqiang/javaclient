package test.devicereport;

public class Device extends Base{
	private int identy;
	private String prtid;
	private String cltid;
	private String mac;
	private String type;
	private String bind;
	private String mqtthost;
	private int mqttport;
	public int getIdenty() {
		return identy;
	}
	public void setIdenty(int identy) {
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
}

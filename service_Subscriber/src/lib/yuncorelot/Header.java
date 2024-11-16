package lib.yuncorelot;

import lib.yuncorelot.Base;

public class Header extends Base{
	private String protocol;
	private String type;
	private String encode;
	private String compress;
	private String mid;
	private String bind;
	private Boolean debug;
	
	public void setProtocol(String protocol){
		this.protocol = protocol;
	}
	
	public String getProtocol(){
		return this.protocol;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setEncode(String encode){
		this.encode = encode;
	}
	
	public String getEncode(){
		return this.encode;
	}
	
	public void setCompress(String compress){
		this.compress = compress;
	}
	
	public String getCompress(){
		return this.compress;
	}
	
	public void setMid(String mid){
		this.mid = mid;
	}
	
	public String getMid(){
		return this.mid;
	}
	
	public void setBind(String bind){
		this.bind = bind;
	}
	
	public String getBind(){
		return this.bind;
	}
	
	public void setDebug(Boolean debug){
		this.debug = debug;
	}
	
	public Boolean getDebug(){
		return this.debug;
	}
	
	/*public String toString(){
		String str = "{\"protocol\":\""+this.protocol+"\",\"type\":\""+this.type+"\",\"encode\":\""+encode+"\",\"mid\":\""+this.mid+"\"";
		if(this.bind != null){
			str += ",\"bind\":\""+this.bind+"\"";
		}
		if(this.debug != null){
			str += ",\"debug\":\""+this.debug+"\"";
		}
		str += "}";
		return str;
	}*/
}
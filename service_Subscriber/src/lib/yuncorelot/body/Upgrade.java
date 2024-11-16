package lib.yuncorelot.body;

import java.util.Map;

public class Upgrade{
	private String orderid;
	private Object status;
	
	public void setOrderid(String orderid){
		this.orderid = orderid;
	}
	
	public String getOrderid(){
		return this.orderid;
	}
	
	public void setStatus(Object status){
		this.status = status;
	}
	public Object getStatus(){
		return this.status;
	}
	
	@SuppressWarnings("unchecked")
	public String toString(){
		String str = "";
		if(this.status instanceof String){
			str = "{\"orderid\":\""+this.orderid+"\",\"status\":\""+this.status.toString()+"\"}";
		}
		if(this.status instanceof Map<?,?>){
			for(Map.Entry<String,String> entry : ((Map<String, String>) this.status).entrySet()){
				str += "\""+entry.getKey()+"\":\""+entry.getValue()+"\",";
			}
			str = "{"+str.substring(0,str.length()-1)+"}";
		}
		return str;
	}
}
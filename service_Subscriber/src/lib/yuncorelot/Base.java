package lib.yuncorelot;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;

import lib.Helper;

public class Base{
	private static final Log log = LogFactory.getLog(Base.class);
	
	@SuppressWarnings("unchecked")
	public Object mergeObj(Object obj){
		Object newObj = this;
		Field[] fields = obj.getClass().getDeclaredFields();
		for(int i=0;i<fields.length;i++){
			try{
				String property = fields[i].getName();
				String mproperty = property.substring(0,1).toUpperCase() + property.substring(1);
				//获取属性类型
				String type = fields[i].getGenericType().toString();
				//java.lang.System.out.println(type);
				if(type.equals("class java.lang.String")){
					newObj.getClass().getMethod("set"+mproperty,String.class).invoke(newObj,(String)obj.getClass().getMethod("get"+mproperty).invoke(obj));
				}else if(type.equals("java.util.List<java.lang.String>")){
					newObj.getClass().getMethod("set"+mproperty,List.class).invoke(newObj,(List<String>)obj.getClass().getMethod("get"+mproperty).invoke(obj));
				}else if(type.equals("java.util.List<java.util.Map<java.lang.String, java.lang.String>>")){
					newObj.getClass().getMethod("set"+mproperty,List.class).invoke(newObj,(List<Map<String,String>>)obj.getClass().getMethod("get"+mproperty).invoke(obj));
				}else if(type.equals("java.util.Map<java.lang.String, java.lang.String>")){
					newObj.getClass().getMethod("set"+mproperty,Map.class).invoke(newObj,(Map<String,String>)obj.getClass().getMethod("get"+mproperty).invoke(obj));
				}else{
					
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
		return newObj;
	}
	
	public String toString(){
	/*	String str = "";
		Field[] fields = this.getClass().getDeclaredFields();
		for(int i=0;i<fields.length;i++){
			//获取属性值
			String property = fields[i].getName();
			String mproperty = property.substring(0,1).toUpperCase() + property.substring(1);
			//获取属性类型
			String type = fields[i].getGenericType().toString();

			try{
				Method m = this.getClass().getMethod("get"+mproperty);
				Object value = m.invoke(this);
				if(type.substring(0,5).equals("class")){
					if(type.equals("class java.lang.String")){
						if(value != null){
							str += ",\""+property+"\":\""+value.toString()+"\"";
						}
					}else{
						str += ",\""+property+"\":"+value.toString()+"";
					}
				}else if(type.equals("java.util.List<java.lang.String>")){//List<String>
					List<String> plist = (List<String>) value;
					if(plist.isEmpty()){
						str += ",\""+property+"\":[]";
					}else{
						str += ",\""+property+"\":[";
						for(int j=0;j<plist.size();j++){
							str += "\""+plist.get(j)+"\",";
						}
						str = str.substring(0,str.length()-1) + "]";
					}
				}else if(type.equals("java.util.Map<java.lang.String, java.lang.String>")){//Map<String,String>
					Map<String,String> pmap = (Map<String,String>) value;
					if(pmap.isEmpty()){
						str += ",\""+property+"\":{}";
					}else{
						str += ",\""+property+"\":{";
						for(Map.Entry<String,String> entry : pmap.entrySet()){
							str += "\""+entry.getKey()+"\":\""+entry.getValue().toString()+"\",";
						}
						str = str.substring(0,str.length()-1) + "}";
					}
				}else if(type.equals("java.util.List<java.util.Map<java.lang.String, java.lang.String>>")){//List<Map<String,String>>
					List<Map<String,String>> plmap = (List<Map<String,String>>) value;
					if(plmap.isEmpty()){
						str += ",\""+property+"\":[]";
					}else{
						str += ",\""+property+"\":[";
						for(int k=0;k<plmap.size();k++){
							str += "{";
							Map<String,String> pmap = plmap.get(k);
							for(Map.Entry<String,String> entry : pmap.entrySet()){
								str += "\""+entry.getKey()+"\":\""+entry.getValue()+"\",";
							}
							str = str.substring(0,str.length()-1) + "},";
						}
						str = str.substring(0,str.length()-1) + "]";
					}
				}else if(type.equals("java.util.List<lib.yuncorelot.body.Alarm>")){
					List<Alarm> plc = (List<Alarm>) value;
					if(plc.isEmpty()){
						str += ",\""+property+"\":[]";
					}else{
						str += ",\""+property+"\":[";
						for(int k=0;k<plc.size();k++){
							str += plc.get(k).toString() + ",";
						}
						str = str.substring(0,str.length()-1) + "]";
					}
				}else if(type.equals("java.util.List<lib.yuncorelot.body.Ichild>")){
					List<Ichild> plChild = (List<Ichild>) value;
					if(plChild.isEmpty()){
						str += ",\""+property+"\":[]";
					}else{
						str += ",\""+property+"\":[";
						for(int n=0;n<plChild.size();n++){
							str += plChild.get(n).toString() + ",";
						}
						str = str.substring(0,str.length()-1) + "]";
					}
				}else if(type.equals("java.util.List<lib.yuncorelot.body.WifiSupportCountryCode>")){
					List<WifiSupportCountryCode> wifiSupportCountryCode = (List<WifiSupportCountryCode>) value;
					if(wifiSupportCountryCode.isEmpty()){
						str += ",\""+property+"\":[]";
					}else{
						str += ",\""+property+"\":[";
						for(int n=0;n<wifiSupportCountryCode.size();n++){
							str += wifiSupportCountryCode.get(n).toString() + ",";
						}
						str = str.substring(0,str.length()-1) + "]";
					}
				}else if(type.equals("java.util.List<lib.yuncorelot.body.WifiRadioVap>")){
					List<WifiRadioVap> wifiRadioVap = (List<WifiRadioVap>) value;
					if(wifiRadioVap.isEmpty()){
						str += ",\""+property+"\":[]";
					}else{
						str += ",\""+property+"\":[";
						for(int n=0;n<wifiRadioVap.size();n++){
							str += wifiRadioVap.get(n).toString() + ",";
						}
						str = str.substring(0,str.length()-1) + "]";
					}
				}else if(type.equals("java.util.List<lib.yuncorelot.body.WifiRadio>")){
					List<WifiRadio> wifiRadio = (List<WifiRadio>) value;
					if(wifiRadio.isEmpty()){
						str += ",\""+property+"\":[]";
					}else{
						str += ",\""+property+"\":[";
						for(int n=0;n<wifiRadio.size();n++){
							str += wifiRadio.get(n).toString() + ",";
						}
						str = str.substring(0,str.length()-1) + "]";
					}
				}else{
					
				}
			}catch(Exception e){
				
			}
		}
		if(!str.isEmpty()){
			str = "{" + str.substring(1) + "}";
		}
		return str;*/
		return JSON.toJSONString(this);
	}
}
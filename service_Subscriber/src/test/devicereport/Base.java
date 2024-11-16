package test.devicereport;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;

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
		return JSON.toJSONString(this);
	}
}

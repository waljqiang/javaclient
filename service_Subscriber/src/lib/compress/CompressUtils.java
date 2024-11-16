package lib.compress;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;

import lib.compress.driver.CompressGzip;

public class CompressUtils{
	//压缩方法
	private CompressInterface compress_driver;
	
	public void setDriver(CompressDrivers driver){
		String ldriver = driver.toString().toLowerCase();
		String driverName = "Compress" + ldriver.substring(0,1).toUpperCase() + ldriver.substring(1);
		compress_driver = new CompressGzip();
	}
	
	public byte[] compress(String str) throws CompressException{
		return this.compress_driver.compress(str);
	}
	
	public String uncompress(byte[] bytes) throws CompressException{
		return this.compress_driver.uncompress(bytes);
	}
	
	public String compressBase64(String str) throws CompressException{
		byte[] bytes = this.compress(str);
		return Base64.getEncoder().encodeToString(bytes);
	}
	
	public String uncompressBase64(String str) throws CompressException{
		byte[] bytes = Base64.getDecoder().decode(str);
		return this.uncompress(bytes);
	}
	
	public String toString(){
		Class clazz = this.getClass();
		StringBuilder sb = new StringBuilder();//利用StringBuilder来保存字符串
		Package packageName = clazz.getPackage();
		sb.append("package: "+packageName.getName()+"\n");
		sb.append("-----------------------------------------------------\n");
		
		String className = clazz.getSimpleName();
		sb.append("class: "+className+"\n");
		sb.append("-----------------------------------------------------\n");
		
		sb.append("property: \n");
		sb.append("----------\n");
		Field[] fields = clazz.getDeclaredFields();
		for(Field field:fields){
			sb.append(field.toGenericString()+"\n");
		}
		sb.append("-----------------------------------------------------\n");
		
		sb.append("method: \n");
		sb.append("----------\n");
		Constructor[] constructors = clazz.getDeclaredConstructors();
		for(Constructor constructor:constructors){
			sb.append(constructor.toGenericString() + "\n");
		}
		Method[] methods = clazz.getDeclaredMethods();
		for(Method method:methods){
		    sb.append(method.toGenericString()+"\n");
		}
		sb.append("-----------------------------------------------------\n");
		return sb.toString();
	}
	
}
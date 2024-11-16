package conf;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class config{
	private static final String ROOT_PATH = System.getProperty("user.dir");
	private static final Log log = LogFactory.getLog(config.class);
	private static Properties p;
	
	public static String getIni(String key){
		return config.getIni(key,"");
	}
	
	public static String getIni(String key,String value){
		if(null == p){
			try{
				p = new Properties();
				p.load(new FileInputStream(config.ROOT_PATH + "/config.ini"));
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}
		String res = p.getProperty(key);
		return res == null ? value : res;
	}
}
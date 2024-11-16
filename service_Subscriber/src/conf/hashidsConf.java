package conf;

import java.util.HashMap;
import java.util.Map;

public class hashidsConf{
	public static Map<String,String> hashid;
	static{
		hashid = new HashMap<String,String>();
		hashid.put("salt", "cloudnetlot");
		hashid.put("length", "25");
		hashid.put("header", "cnl");
		hashid.put("alphabet","abcdefghijklmnopqrstuvwxyz");
		hashid.put("enable", config.getIni("APP_HASHIDS_ENABLE","TRUE"));
	}
	public static Map<String,String> hashPrtid;
	static{
		hashPrtid = new HashMap<String,String>();
		hashPrtid.put("salt","product");
		hashPrtid.put("length","22");
		hashPrtid.put("header","prt");
		hashPrtid.put("alphabet","abcdefghijklmnopqrstuvwxyz");
	}
	
	public static Map<String,String> hashCltid;
	static{
		hashCltid = new HashMap<String,String>();
		hashCltid.put("salt","client");
		hashCltid.put("length","27");
		hashCltid.put("header","clt");
		hashCltid.put("alphabet","abcdefghijklmnopqrstuvwxyz0123456789");
	};
}
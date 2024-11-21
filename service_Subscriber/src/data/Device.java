package data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



import conf.mysqlConf;
import conf.publicConf;
import conf.redisConf;
import lib.Helper;
import lib.JdbcUtils;
import lib.JdbcUtilsPool;


public class Device implements Serializable{
	/** 
     *  
     */  
	private static final Log log = LogFactory.getLog(Device.class);
   
    public static final String sql_get_register = "select m.`name`,p.`uid` as developUid,p.`prtid`,p.`type`,"
    		+ "p.`size`,p.`aud_status`,n.`cltid`,n.`mac`,m.`user_id` as bindUid,m.`bind`,m.`bind_status`,m.`group_id`"
    		+ " as gid,m.`dev_username`,m.`weblogin_pwd`,m.`pwd_encrypt`,m.`resource` from "+mysqlConf.prefix+"device m,"
    				+ ""+mysqlConf.prefix+"develop_client n,"+mysqlConf.prefix+"develop_product p where m.`dev_mac`=? "
    						+ "and m.`dev_mac`=n.`mac` and n.`prtid`=p.`prtid` and m.`prtid`=p.`prtid`";
    
 
    private BigInteger id;
    private BigInteger user_id;
    private String dev_mac;
    private String dev_ip;
    private String net_ip;
    private String heartbeat;
    private String name;
    private String prtid;
    private Integer prt_type;
    private String prt_size;
    private String cltid;
    private String type;
    private Integer mode;
    private String version;
    private BigInteger up_time;
    private String pid;
    private Long area;
    private String country;
    private String province;
    private String city;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String chip;
    private String sn;
    private String notes;
    private BigInteger group_id;
    private Integer is_ip_location;
    private String bind;
    private Short is_del;
    private BigInteger join_time;
    private String dev_username;
    private String weblogin_pwd;
    private String pwd_encrypt;
    private Short lstatus;
    private BigInteger created_at;
    private BigInteger updated_at;
   
    public Device() {  
        // TODO Auto-generated constructor stub  
    }
    
    public String getuser_id(){
    	return this.user_id.toString();
    }
    
    public String getdev_mac(){
    	return this.dev_mac;
    }
    
    public String getgroup_id(){
    	return this.group_id.toString();
    }
    
    public String getname(){
    	return this.name;
    }
    
    public Map<String,Object> getRegisterInfo(String mac){
    	Map<String,Object> result = null;
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	result = this.getRegisterInfo(mac, jdbcUtils);
		jdbcUtils.releaseConn();
		return result;
    }
    
    public Map<String,Object> getRegisterInfo(String mac,JdbcUtils jdbcUtils){
    	Map<String,Object> result = null;
    	List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(mac);
		try{
			 result = jdbcUtils.findSimpleResult(sql_get_register,paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return result;
    }
    

    
   
    

   
    
   
  
   
    
  
    
   
    
   
   
    
   
    
   
   

    
   

   
   

 
 
  

    

    

    


    


   
  
    
 
  
    
 


   
    

  
    
  
 
  
   
  
    
 
   
 
    
 
    
   
    
  
   
    
   
   
    

    

    @Override  
    public String toString() {  
        return mysqlConf.prefix+"device [id=" + id + ",user_id=" + user_id + ",dev_mac=" + dev_mac + ",dev_ip=" + dev_ip + ",net_ip="+net_ip+",heartbeat="+heartbeat+",name="+name+",prtid="+prtid+",prt_type="+prt_type+","
        		+ ",prt_size="+prt_size+",cltid="+cltid+",type="+type+",mode="+mode+",version="+version+",up_time="+up_time+",pid="+pid+",area="+area+",country="+country+",province="+province+",city="+city+""
        				+ ",address="+address+",latitude="+latitude+",longitude="+longitude+",chip="+chip+",sn="+sn+",notes="+notes+",group_id="+group_id+",is_ip_location="+is_ip_location+""
        						+ ",bind="+bind+",is_del="+is_del+",join_time="+join_time+",dev_username="+dev_username+",weblogin_pwd="+weblogin_pwd+",lstatus="+lstatus+",created_at="+created_at+",updated_at="+updated_at+"]";  
    }  
}
package data;

import java.io.Serializable;
import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import conf.mysqlConf;


public class Command implements Serializable{
	/** 
     *  
     */
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(Device.class);
	private static final long serialVersionUID = 1L;
	
	public static final String sql_update = "update "+mysqlConf.prefix+"command set `status`=?,`updated_at`=? where `dev_mac`=? and `comm_id`=?";
    
	private BigInteger id;
	private BigInteger user_id;
	private String dev_mac;
	private String comm_id;
	private String content;
	private String describe;
	private Short status;
	private String dev_ip;
	private String dev_name;
	private String dev_version;
	private String dev_type;
	private Short retry;
	private Short is_del;
	private BigInteger created_at;
	private BigInteger updated_at;
   
    public Command() {  
        // TODO Auto-generated constructor stub  
    }
    
    @Override  
    public String toString() {  
        return mysqlConf.prefix+"command [id=" + id + ",user_id=" + user_id + ",dev_mac=" + dev_mac + ",comm_id=" + comm_id + ",content="+content+",describe="+describe+",status="+status+",dev_ip="+dev_ip+",dev_name="+dev_name+","
        		+ ",dev_version="+dev_version+",dev_type="+dev_type+",retry="+retry+",is_del="+is_del+",created_at="+created_at+",updated_at="+updated_at+"]";  
    }  
}
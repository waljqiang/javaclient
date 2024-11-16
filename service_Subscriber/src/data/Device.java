package data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import conf.mysqlConf;
import conf.publicConf;
import conf.redisConf;
import lib.Helper;
import lib.JdbcUtils;
import lib.JdbcUtilsPool;
import redis.clients.jedis.Jedis;

public class Device implements Serializable{
	/** 
     *  
     */  
	private static final Log log = LogFactory.getLog(Device.class);
    private static final long serialVersionUID = 1L;
    public static final String sql_get_register = "select m.`name`,p.`uid` as developUid,p.`prtid`,p.`type`,p.`size`,p.`aud_status`,n.`cltid`,n.`mac`,m.`user_id` as bindUid,m.`bind`,m.`bind_status`,m.`group_id` as gid,m.`dev_username`,m.`weblogin_pwd`,m.`pwd_encrypt`,m.`resource` from "+mysqlConf.prefix+"device m,"+mysqlConf.prefix+"develop_client n,"+mysqlConf.prefix+"develop_product p where m.`dev_mac`=? and m.`dev_mac`=n.`mac` and n.`prtid`=p.`prtid` and m.`prtid`=p.`prtid`";
    
    public static final String sql_get_device = "select m.`id`,m.`user_id`,m.`dev_mac`,m.`dev_ip`,m.`net_ip`,m.`name`,m.`type`,m.`mode`,m.`version`,m.`lang`,m.`pid`,m.`latitude`,m.`longitude`,m.`chip`,m.`sn`,m.`group_id`,m.`is_ip_location`,m.`dev_username`,m.`bind`,m.`bind_status`,m.`rebind`,m.`is_forceunbind`,m.`weblogin_pwd`,m.`pwd_encrypt`,m.`prtid`,m.`cltid`,n.`id` as rid,m.`version`,m.`newversion`,m.`package_fid`,m.`is_tdma` from "+mysqlConf.prefix+"device m left join "+mysqlConf.prefix+"device_relation n on m.`dev_mac`=n.`mac` where `dev_mac`=?";
    public static final String sql_insert_device = "insert into "+mysqlConf.prefix+"device(";
    public static final String sql_update_device = "update "+mysqlConf.prefix+"device set ";
    
    public static final String sql_replace_relation = "replace into "+mysqlConf.prefix+"device_relation(`uid`,`mac`,`pid`,`created_at`,`updated_at`)values(?,?,?,?,?)";
    public static final String sql_insert_clients_nums = "insert into "+mysqlConf.prefix+"device_clients_nums(`mac`,`uid`,`onlines`,`created_at`,`updated_at`)values(?,?,?,?,?)";
    
    public static final String sql_get_childs = "select * from "+mysqlConf.prefix+"device where `pid`=?";
    public static final String sql_get_user_childs = "select `id`,`username` from "+mysqlConf.prefix+"users where pid=?";
    public static final String sql_del_childs = "delete from "+mysqlConf.prefix+"device where `dev_mac` in(";
    public static final String sql_add_child = "insert into "+mysqlConf.prefix+"device(`chip`,`weblogin_pwd`,`latitude`,`dev_username`,`created_at`,`pid`,`prt_type`,`type`,`version`,`join_time`,`mode`,`dev_mac`,`net_ip`,`updated_at`,`user_id`,`group_id`,`name`,`dev_ip`,`is_ip_location`,`longitude`,`newVersion`,`package_fid`)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    public static final String sql_replace_child = "replace into "+mysqlConf.prefix+"device(`chip`,`weblogin_pwd`,`latitude`,`dev_username`,`created_at`,`pid`,`prt_type`,`type`,`version`,`join_time`,`mode`,`dev_mac`,`net_ip`,`updated_at`,`user_id`,`group_id`,`name`,`dev_ip`,`is_ip_location`,`longitude`,`newVersion`,`package_fid`,`sn`,`is_tdma`)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    
    public static final String sql_del_topgraphy = "delete from "+mysqlConf.prefix+"topgraphy where `uid`=? and `mac` != '0' and `pid` != '-1'";
    public static final String sql_del_relations = "delete from "+mysqlConf.prefix+"device_relation where `mac` in(";
    public static final String sql_del_relation = "delete from "+mysqlConf.prefix+"device_relation where `mac`=?";
    
    public static final String sql_clear_bind = "update "+mysqlConf.prefix+"device set `user_id`=0,`dev_ip`=\"\",`net_ip`=\"\",`name`=\"\",`version`=\"\",`up_time`=0,`pid`=\"\",`area`=0,`country`=\"\",`province`=\"\",`city`=\"\",`address`=\"\",`latitude`=null,`longitude`=null,`chip`=\"\",`sn`=\"\",`notes`=\"\",`group_id`=0,`bind`=\"\",`join_time`=null,`updated_at`=? where `dev_mac`=?";
    
    public static final String sql_get_orderinfo = "select m.`orderid`,m.`package_id`,m.`package_name`,m.`version`,m.`exec_time`,m.`status`,n.`dev_mac`,n.`status` as devstatus from "+mysqlConf.prefix+"upgrade_order m left join "+mysqlConf.prefix+"upgrade_order_dev n on m.`orderid`=n.`orderid` where m.`orderid`=?";
    public static final String sql_update_order_dev_status = "update "+mysqlConf.prefix+"upgrade_order_dev set `status`=?,`updated_at`=? where `orderid`=? and `dev_mac` in(";
    public static final String sql_update_order_status = "update "+mysqlConf.prefix+"upgrade_order set `status`=?,`updated_at`=? where `orderid`=?";
    
    public static final String sql_get_area = "select `code` from "+mysqlConf.prefix+"area where `name`=?";
    public static final String sql_update_ip_location = "update "+mysqlConf.prefix+"device set `area`=?,`country`=?,`province`=?,`city`=?,`address`=?,`longitude`=?,`latitude`=?,`is_ip_location`=0,`updated_at`=? where `dev_mac`=?";
    public static final String sql_get_no_location = "select `dev_mac`,`country`,`province`,`city`,`address`,`latitude`,`longitude` from "+mysqlConf.prefix+"device where `latitude` is not null and `longitude` is not null and `lstatus`=1 order by `id` asc limit ?";  
    public static final String sql_update_location = "update "+mysqlConf.prefix+"device set ";
    public static final String sql_update_location_lstatus = "update "+mysqlConf.prefix+"device set `lstatus`=?,`updated_at`=? where `dev_mac` in(";
    
    public static final String sql_get_no_static_client = "select * from "+mysqlConf.prefix+"device_clients_nums where `status`in(1,3) and `retry`<? order by `id` asc limit ?";
    public static final String sql_add_user_statics = "replace into "+mysqlConf.prefix+"user_clients_statics_hour(`uid`,`onlines`,`hours`,`created_at`,`updated_at`)values";
    public static final String sql_add_client_statics = "replace into "+mysqlConf.prefix+"device_clients_statics_hour(`mac`,`uid`,`onlines`,`hours`,`created_at`,`updated_at`)values";
    public static final String sql_update_statics_status = "update "+mysqlConf.prefix+"device_clients_nums set `status`=?,`retry`=`retry`+1 where `id` in(";
    
    public static final String sql_get_alarm_set = "select * from "+mysqlConf.prefix+"device_alarm_set where `mac`=?";
    public static final String sql_get_alarm_notice_set = "select m.`mac`,m.`uid`,m.`lang`,n.`username`,n.`email`,n.`timeZone`,n.`isSummerTime`,p.`open_id` from "+mysqlConf.prefix+"device_notice m left join "+mysqlConf.prefix+"users n on m.`uid`=n.`id` left join "+mysqlConf.prefix+"wechat_bind p on m.`uid`=p.`uid` where m.`mac`=?";    
    public static final String sql_add_alarm = "insert into "+mysqlConf.prefix+"device_alarm(`mac`,`content`,`created_at`,`updated_at`)values(?,?,?,?)";
    public static final String sql_del_alarm_set = "delete from "+mysqlConf.prefix+"device_alarm_set where `mac`=?";
    public static final String sql_del_alarm_notice = "delete from "+mysqlConf.prefix+"device_notice where `mac`=?";
    
    public static final String sql_user_get = "select `id` from "+mysqlConf.prefix+"users where `id`=? and `is_del`=0 and `status`=1";
    public static final String sql_count_bind = "select `type`,count(`id`) as nums from "+mysqlConf.prefix+"device where `user_id` != 0 and `group_id` != 0 and `pid` = '' and `join_time` between ? and ? group by `type`";
    public static final String sql_count_bind_day_old_get = "select `type`,max(nums) as nums from "+mysqlConf.prefix+"ct_dev_binds_day where `day` <= ? and `status`=1 group by `type`";
    
    public static final String sql_count_bind_day_get = "select `type`,max(nums) as nums from "+mysqlConf.prefix+"ct_dev_binds_day where `day` >= ? and `day` <= ? and `status`=1 group by `type`";
    public static final String sql_count_bind_month_get = "select `type`,max(`nums`) as nums from "+mysqlConf.prefix+"ct_dev_binds_month where `month` >= ? and `month` <= ? and `status`=1 group by `type`";
    public static final String sql_count_bind_day_add = "insert into "+mysqlConf.prefix+"ct_dev_binds_day(`day`,`type`,`nums`,`status`,`created_at`,`updated_at`)values";
    public static final String sql_count_bind_month_add = "insert into "+mysqlConf.prefix+"ct_dev_binds_month(`month`,`type`,`nums`,`status`,`created_at`,`updated_at`)values";
    public static final String sql_count_bind_year_add = "insert into "+mysqlConf.prefix+"ct_dev_binds_year(`year`,`type`,`nums`,`status`,`created_at`,`updated_at`)values";
    public static final String sql_count_bind_quarter_add = "insert into "+mysqlConf.prefix+"ct_dev_binds_quarter(`quarter`,`type`,`nums`,`status`,`created_at`,`updated_at`)values";
    public static final String sql_count_bind_week_add = "insert into "+mysqlConf.prefix+"ct_dev_binds_week(`week`,`type`,`nums`,`status`,`created_at`,`updated_at`)values";
    
    public static final String sql_all_device_get = "select `dev_mac`,`type` from "+mysqlConf.prefix+"device where `user_id` != 0 and `group_id` != 0 and `pid` = '' and `created_at` between ? and ?";
    public static final String sql_all_device_get_no_day = "select `dev_mac`,`type` from "+mysqlConf.prefix+"device where `user_id` != 0 and `group_id` != 0 and `pid` = ''";
    public static final String sql_count_online_day_get = "select `type`,max(`nums`) as nums from "+mysqlConf.prefix+"ct_dev_onlines_day where `day` >= ? and `day` <= ? and `status`=1 group by `type`";
    public static final String sql_count_online_month_get = "select `type`,max(`nums`) as nums from "+mysqlConf.prefix+"ct_dev_onlines_month where `month` >= ? and `month` <= ? and `status`=1 group by `type`";
    public static final String sql_count_online_day_add = "insert into "+mysqlConf.prefix+"ct_dev_onlines_day(`day`,`type`,`nums`,`status`,`created_at`,`updated_at`)values";
    public static final String sql_count_online_month_add = "insert into "+mysqlConf.prefix+"ct_dev_onlines_month(`month`,`type`,`nums`,`status`,`created_at`,`updated_at`)values";
    public static final String sql_count_online_year_add = "insert into "+mysqlConf.prefix+"ct_dev_onlines_year(`year`,`type`,`nums`,`status`,`created_at`,`updated_at`)values";
    public static final String sql_count_online_quarter_add = "insert into "+mysqlConf.prefix+"ct_dev_onlines_quarter(`quarter`,`type`,`nums`,`status`,`created_at`,`updated_at`)values";
    public static final String sql_count_online_week_add = "insert into "+mysqlConf.prefix+"ct_dev_onlines_week(`week`,`type`,`nums`,`status`,`created_at`,`updated_at`)values";

    public static final String sql_command_get = "select * from "+mysqlConf.prefix+"command where `comm_id`=?";
    
    public static final String sql_auth_with_policy_get = "SELECT m.`prtid`,m.`cltid`,m.`dev_mac`,m.`name` as dev_name,m.`user_id` as uid,m.`group_id`,n.`radioid`,n.`vapid`,n.`enable`,n.`method`,n.`portal_id`,p.`id` as policy_id,n.`lang`,p.`expire_in`,p.`heartbeat`,p.`white_domain`,p.`black_domain`,p.`white_mac`,p.`black_mac`,q.`resource`,q.`type`,q.`info`,q.`url`,q.`title`,q.`location`,q.`color`,q.`welcome` FROM " +mysqlConf.prefix+ "device m LEFT JOIN " + mysqlConf.prefix + "device_auth_config n ON m.`user_id`=n.`uid` AND m.`dev_mac`=n.`mac` AND n.`radioid` = ? AND n.`vapid`= ? LEFT JOIN " + mysqlConf.prefix + "auth_policy p ON n.`uid`=p.`uid` AND n.`policy_id` = p.`id` LEFT JOIN " + mysqlConf.prefix + "auth_portal q ON n.`uid`=q.`uid` AND n.`portal_id`=q.`portal_id` WHERE m.`dev_mac`=? AND m.`user_id` !=0 AND m.`bind_status` = 2 AND m.`bind` != ''";
    public static final String sql_clear_device_auth = "delete from "+mysqlConf.prefix+"device_auth_config where `mac`=?";
    public static final String sql_auth_login_set = "update " + mysqlConf.prefix+"device_auth_login set `status`=?,`revoked`=1 where `dev_mac`=? and `client_mac`=? and `token`=?";
    public static final String sql_auth_member = "select `id` as mid,`uid`,`username` from "+mysqlConf.prefix+"device_auth_member where `id`=?";
    
    public static final String sql_group_devnums_get = "select count(`id`) as device_nums from "+mysqlConf.prefix+"device where `pid`='' and `user_id` != 0 and `group_id`=?";
    public static final String sql_handle_group_nums = "update "+mysqlConf.prefix+"group set `device_nums`=? where `id`=?";
    
    public static final String sql_packages_get = "select m.`fid`,m.`category`,m.`name`,m.`url`,`version`,m.`lang`,m.`file_md5`,m.`is_local` FROM " + mysqlConf.prefix+"package m where m.`category`=? and m.`is_local`=? and m.`fid` in (select n.`fid` from "+mysqlConf.prefix+"package_type n where n.`type`=?)";
    												   
    
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
    
    public Map<String,Object> getRegisterInfoPool(String mac){
    	Map<String,Object> result = null;
    	List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(mac);
		try{
			 result = JdbcUtilsPool.findSimpleResult(sql_get_register,paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return result;
    }
    
    public Map<String,Object> getDevice(String mac){
    	Map<String,Object> result = new HashMap<String,Object>();
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	result = this.getDevice(mac, jdbcUtils);
    	jdbcUtils.releaseConn();
		return result;
    }
    
    public Map<String,Object> getDevice(String mac,JdbcUtils jdbcUtils){
    	Map<String,Object> result = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(mac);
		try{
			result = jdbcUtils.findSimpleResult(sql_get_device, paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return result;
    }
    
    public Map<String,Object> getDevicePool(String mac){
    	Map<String,Object> result = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(mac);
		try{
			result = JdbcUtilsPool.findSimpleResult(sql_get_device, paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return result;
    }
    
    public Boolean addDevice(Map<String,String> params){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean flag = this.addDevice(params, jdbcUtils);
    	jdbcUtils.releaseConn();
		return flag;
    }
    
    public Boolean addDevice(Map<String,String> params,JdbcUtils jdbcUtils){
    	Boolean flag = true;
    	if(!params.isEmpty()){
	    	String sql = sql_insert_device;
	    	String values = "values(";
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	//组装sql
	    	Iterator<?> iter = params.entrySet().iterator();
	    	while(iter.hasNext()){
	    		Map.Entry<?,?> entry = (Map.Entry<?,?>) iter.next();
	    		String key = entry.getKey().toString();
	    		String value = entry.getValue().toString();
	    		sql += "`"+key+"`,";
	    		values += "?,";
	    		paramsQuery.add(value);
	    	}
	    	sql = sql.substring(0,sql.length()-1)+")"+values.substring(0,values.length()-1)+")";
			try{
				flag = jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
			}catch(Exception e){
				flag = false;
				log.error(e.getMessage());
			}
    	}
		return flag;
    }
    
    public Boolean addDevicePool(Map<String,String> params){
    	Boolean flag = true;
    	if(!params.isEmpty()){
	    	String sql = sql_insert_device;
	    	String values = "values(";
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	//组装sql
	    	Iterator<?> iter = params.entrySet().iterator();
	    	while(iter.hasNext()){
	    		Map.Entry<?,?> entry = (Map.Entry<?,?>) iter.next();
	    		String key = entry.getKey().toString();
	    		String value = entry.getValue().toString();
	    		sql += "`"+key+"`,";
	    		values += "?,";
	    		paramsQuery.add(value);
	    	}
	    	sql = sql.substring(0,sql.length()-1)+")"+values.substring(0,values.length()-1)+")";
			try{
				flag = JdbcUtilsPool.updateByPreparedStatement(sql, paramsQuery);
			}catch(Exception e){
				flag = false;
				log.error(e.getMessage());
			}
    	}
		return flag;
    }
    
    public Boolean updateDevice(String mac,Map<String,String> datas){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	Boolean flag = this.updateDevice(mac, datas, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return flag;
    }
    
    public Boolean updateDevice(String mac,Map<String,String> datas,JdbcUtils jdbcUtils){
    	Boolean flag = true;
    	if(!datas.isEmpty()){
	    	String sql = sql_update_device;
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	//组装sql
	    	Iterator<?> iter = datas.entrySet().iterator();
	    	while(iter.hasNext()){
	    		Map.Entry<?,?> entry = (Map.Entry<?,?>) iter.next();
	    		String key = entry.getKey().toString();
	    		String value = entry.getValue().toString();
				sql += "`"+key+"`=?,";
				paramsQuery.add(value);
	    	}
	    	sql = sql.substring(0,sql.length()-1)+" where `dev_mac`=?";
	    	paramsQuery.add(mac);
	    	try{
				flag = jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
			}catch(Exception e){
				flag = false;
				log.error(e.getMessage());
			}
    	}
    	return flag;
    }
    
    public Boolean updateDevicePool(String mac,Map<String,String> datas){
    	Boolean flag = true;
    	if(!datas.isEmpty()){
	    	String sql = sql_update_device;
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	//组装sql
	    	Iterator<?> iter = datas.entrySet().iterator();
	    	while(iter.hasNext()){
	    		Map.Entry<?,?> entry = (Map.Entry<?,?>) iter.next();
	    		String key = entry.getKey().toString();
	    		String value = entry.getValue().toString();
				sql += "`"+key+"`=?,";
				paramsQuery.add(value);
	    	}
	    	sql = sql.substring(0,sql.length()-1)+" where `dev_mac`=?";
	    	paramsQuery.add(mac);
	    	try{
				flag = JdbcUtilsPool.updateByPreparedStatement(sql, paramsQuery);
			}catch(Exception e){
				flag = false;
				log.error(e.getMessage());
			}
    	}
    	return flag;
    }
    
    public Boolean toRelation(String toUid,String mac,String parent,Long time){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean flag = this.toRelation(toUid, mac, parent, time, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return flag;
    }
    
    public Boolean toRelation(String toUid,String mac,String parent,Long time,JdbcUtils jdbcUtils){
    	Boolean flag = false;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(toUid);
    	paramsQuery.add(mac);
    	paramsQuery.add(parent);
    	paramsQuery.add(time);
    	paramsQuery.add(time);
    	
    	try{
			flag = jdbcUtils.updateByPreparedStatement(sql_replace_relation, paramsQuery);
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage());
		}
    	return flag;
    }
    
    public Boolean toRelationPool(String toUid,String mac,String parent,Long time){
    	Boolean flag = false;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(toUid);
    	paramsQuery.add(mac);
    	paramsQuery.add(parent);
    	paramsQuery.add(time);
    	paramsQuery.add(time);
    	
    	try{
			flag = JdbcUtilsPool.updateByPreparedStatement(sql_replace_relation, paramsQuery);
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage());
		}
    	return flag;
    }
    
    public Boolean deleteChildsByMacs(List<String> macs){
    	Boolean flag = true;
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	flag = this.deleteChildsByMacs(macs, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return flag;
    }
    
    public Boolean deleteChildsByMacs(List<String> macs,JdbcUtils jdbcUtils){
    	Boolean flag = true;
    	if(!macs.isEmpty()){
    		String sql = sql_del_childs;
    		String sql1 = sql_del_relations;
        	List<Object> paramsQuery = new ArrayList<Object>();
        	List<Object> paramsQuery1 = new ArrayList<Object>();
        	for(int i=0;i<macs.size();i++){
        		sql += "?,";
        		sql1 += "?,";
        		paramsQuery.add(macs.get(i));
        		paramsQuery1.add(macs.get(i));
        	}
        	sql = sql.substring(0,sql.length()-1)+")";
        	sql1 = sql1.substring(0,sql1.length()-1)+")";
        	try{
        		jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
        		jdbcUtils.updateByPreparedStatement(sql1, paramsQuery1);
        	}catch(Exception e){
        		flag = false;
        		log.error(e.getMessage());
        	}
    	}
    	return flag;
    }
    
    public Boolean deleteChildsByMacsPool(List<String> macs){
    	Boolean flag = true;
    	if(!macs.isEmpty()){
    		String sql = sql_del_childs;
    		String sql1 = sql_del_relations;
        	List<Object> paramsQuery = new ArrayList<Object>();
        	List<Object> paramsQuery1 = new ArrayList<Object>();
        	for(int i=0;i<macs.size();i++){
        		sql += "?,";
        		sql1 += "?,";
        		paramsQuery.add(macs.get(i));
        		paramsQuery1.add(macs.get(i));
        	}
        	sql = sql.substring(0,sql.length()-1)+")";
        	sql1 = sql1.substring(0,sql1.length()-1)+")";
        	try{
        		JdbcUtilsPool.updateByPreparedStatement(sql, paramsQuery);
        		JdbcUtilsPool.updateByPreparedStatement(sql1, paramsQuery1);
        	}catch(Exception e){
        		flag = false;
        		log.error(e.getMessage());
        	}
    	}
    	return flag;
    }
    
    public Boolean deleteTopgraphyByUid(String uid){
    	Boolean flag = true;
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	flag = this.deleteTopgraphyByUid(uid,jdbcUtils);
    	jdbcUtils.releaseConn();
    	return flag;
    }
    
    public Boolean deleteTopgraphyByUid(String uid,JdbcUtils jdbcUtils){
    	Boolean flag = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(uid);
    	try{
    		jdbcUtils.updateByPreparedStatement(sql_del_topgraphy, paramsQuery);
    	}catch(Exception e){
    		flag = false;
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean deleteTopgraphyByUidPool(String uid){
    	Boolean flag = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(uid);
    	try{
    		JdbcUtilsPool.updateByPreparedStatement(sql_del_topgraphy, paramsQuery);
    	}catch(Exception e){
    		flag = false;
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean deleteRelationByMac(String mac){
    	Boolean flag = true;
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	flag = this.deleteRelationByMac(mac,jdbcUtils);
    	jdbcUtils.releaseConn();
    	return flag;
    }
    
    public Boolean deleteRelationByMac(String mac,JdbcUtils jdbcUtils){
    	Boolean flag = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		jdbcUtils.updateByPreparedStatement(sql_del_relation, paramsQuery);
    	}catch(Exception e){
    		flag = false;
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean deleteRelationByMacPool(String mac){
    	Boolean flag = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		JdbcUtilsPool.updateByPreparedStatement(sql_del_relation, paramsQuery);
    	}catch(Exception e){
    		flag = false;
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean deleteAlarmSet(String mac){
    	Boolean flag = true;
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	flag = this.deleteAlarmSet(mac,jdbcUtils);
    	jdbcUtils.releaseConn();
    	return flag;
    }
    
    public Boolean deleteAlarmSet(String mac,JdbcUtils jdbcUtils){
    	Boolean flag = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		jdbcUtils.updateByPreparedStatement(sql_del_alarm_set, paramsQuery);
    	}catch(Exception e){
    		flag = false;
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean deleteAlarmSetPool(String mac){
    	Boolean flag = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		JdbcUtilsPool.updateByPreparedStatement(sql_del_alarm_set, paramsQuery);
    	}catch(Exception e){
    		flag = false;
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean deleteAlarmNotice(String mac){
    	Boolean flag = true;
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	flag = this.deleteAlarmNotice(mac,jdbcUtils);
    	jdbcUtils.releaseConn();
    	return flag;
    }
    
    public Boolean deleteAlarmNotice(String mac,JdbcUtils jdbcUtils){
    	Boolean flag = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		jdbcUtils.updateByPreparedStatement(sql_del_alarm_notice, paramsQuery);
    	}catch(Exception e){
    		flag = false;
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean deleteAlarmNoticePool(String mac){
    	Boolean flag = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		JdbcUtilsPool.updateByPreparedStatement(sql_del_alarm_notice, paramsQuery);
    	}catch(Exception e){
    		flag = false;
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean clearBind(String mac,Long time){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean flag = this.clearBind(mac, time, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return flag;
    }
    
    public Boolean clearBind(String mac,Long time,JdbcUtils jdbcUtils){
    	Boolean flag = false;
    	String sql = sql_clear_bind;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(time);
    	paramsQuery.add(mac);
    	try{
    		flag = jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean clearBindPool(String mac,Long time){
    	Boolean flag = false;
    	String sql = sql_clear_bind;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(time);
    	paramsQuery.add(mac);
    	try{
    		flag = JdbcUtilsPool.updateByPreparedStatement(sql, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean updateOrderDeviceStatus(String orderID,String status,Long time,List<String> macs){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean flag = this.updateOrderDeviceStatus(orderID,status,time,macs,jdbcUtils);
    	jdbcUtils.releaseConn();
    	return flag;
    }
    
    public Boolean updateOrderDeviceStatus(String orderID,String status,Long time,List<String> macs,JdbcUtils jdbcUtils){
    	Boolean flag = true;
    	if(!macs.isEmpty()){
	    	String sql = sql_update_order_dev_status;
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	paramsQuery.add(status);
	    	paramsQuery.add(time);
	    	paramsQuery.add(orderID);
	    	for(int i=0;i<macs.size();i++){
	    		sql += "?,";
	    		paramsQuery.add(macs.get(i));
	    	}
	    	sql = sql.substring(0,sql.length()-1)+")";
	    	try{
	    		flag = jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		flag = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return flag;
    }
    
    public Boolean updateOrderDeviceStatusPool(String orderID,String status,Long time,List<String> macs){
    	Boolean flag = true;
    	if(!macs.isEmpty()){
	    	String sql = sql_update_order_dev_status;
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	paramsQuery.add(status);
	    	paramsQuery.add(time);
	    	paramsQuery.add(orderID);
	    	for(int i=0;i<macs.size();i++){
	    		sql += "?,";
	    		paramsQuery.add(macs.get(i));
	    	}
	    	sql = sql.substring(0,sql.length()-1)+")";
	    	try{
	    		flag = JdbcUtilsPool.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		flag = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return flag;
    }
    
    public Boolean updateOrderDeviceStatus(String orderID,String status,Long time){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean flag = this.updateOrderDeviceStatus(orderID, status, time, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return flag;
    }
    
    public Boolean updateOrderDeviceStatus(String orderID,String status,Long time,JdbcUtils jdbcUtils){
    	Boolean flag = false;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(status);
    	paramsQuery.add(time);
    	paramsQuery.add(time);
    	try{
    		flag = jdbcUtils.updateByPreparedStatement(sql_update_order_status, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public Boolean updateOrderDeviceStatusPool(String orderID,String status,Long time){
    	Boolean flag = false;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(status);
    	paramsQuery.add(time);
    	paramsQuery.add(time);
    	try{
    		flag = JdbcUtilsPool.updateByPreparedStatement(sql_update_order_status, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return flag;
    }
    
    public List<Map<String,Object>> getChilds(String mac,Boolean withParams,Jedis jedis){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	result = this.getChilds(mac, withParams, jedis, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return result;
    }
    @SuppressWarnings("unused")
    public List<Map<String,Object>> getChilds(String mac,Boolean withParams,Jedis jedis,JdbcUtils jdbcUtils){
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		result = jdbcUtils.findModeResult(sql_get_childs,paramsQuery);
    		if(!result.isEmpty() && withParams){
    			for(int i=0;i<result.size();i++){
    				Map<String,Object> params = new HashMap<String,Object>();
    				Map<String,Object> device = result.get(i);
    				Map<String,String> redisParams = jedis.hgetAll(redisConf.DEVICE_PARAMS+device.get("dev_mac").toString());
        	    	device.put("params",redisParams);
        	    	result.set(i,device);
    			}
    		}
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return result;
    }
    
    public List<Map<String,Object>> getChildsPool(String mac,Boolean withParams,Jedis jedis){
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		result = JdbcUtilsPool.findModeResult(sql_get_childs,paramsQuery);
    		if(!result.isEmpty() && withParams){
    			for(int i=0;i<result.size();i++){
    				Map<String,Object> device = result.get(i);
    				Map<String,String> redisParams = jedis.hgetAll(redisConf.DEVICE_PARAMS+device.get("dev_mac").toString());
        	    	device.put("params",redisParams);
        	    	result.set(i,device);
    			}
    		}
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return result;
    }
    
    public Map<String,Object> getDeviceAreaByCity(String city){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Map<String,Object> area = this.getDeviceAreaByCity(city, jdbcUtils);
    	jdbcUtils.releaseConn();
		return area;
    }
    
    public Map<String,Object> getDeviceAreaByCity(String city,JdbcUtils jdbcUtils){
    	Map<String,Object> area = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(city);
		try{
			area = jdbcUtils.findSimpleResult(Device.sql_get_area,paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return area;
    }
    
    public Map<String,Object> getDeviceAreaByCityPool(String city){
    	Map<String,Object> area = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(city);
		try{
			area = JdbcUtilsPool.findSimpleResult(Device.sql_get_area,paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return area;
    }
    
    public Boolean saveLocation(String mac,String area,Map<String,String> locationInfo,Long time){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean rs = this.saveLocation(mac, area, locationInfo, time, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Boolean saveLocation(String mac,String area,Map<String,String> locationInfo,Long time,JdbcUtils jdbcUtils){
    	Boolean rs = false;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(area);
    	paramsQuery.add(locationInfo.get("country"));
    	paramsQuery.add(locationInfo.get("province"));
    	paramsQuery.add(locationInfo.get("city"));
    	paramsQuery.add(locationInfo.get("address"));
    	paramsQuery.add(locationInfo.get("longitude"));
    	paramsQuery.add(locationInfo.get("latitude"));
    	paramsQuery.add(time);
    	paramsQuery.add(mac);
    	try{
    		rs = jdbcUtils.updateByPreparedStatement(sql_update_ip_location, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public Boolean saveLocationPool(String mac,String area,Map<String,String> locationInfo,Long time){
    	Boolean rs = false;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(area);
    	paramsQuery.add(locationInfo.get("country"));
    	paramsQuery.add(locationInfo.get("province"));
    	paramsQuery.add(locationInfo.get("city"));
    	paramsQuery.add(locationInfo.get("address"));
    	paramsQuery.add(locationInfo.get("longitude"));
    	paramsQuery.add(locationInfo.get("latitude"));
    	paramsQuery.add(time);
    	paramsQuery.add(mac);
    	try{
    		rs = JdbcUtilsPool.updateByPreparedStatement(sql_update_ip_location, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public List<Map<String,Object>> getDatasWithNolocation(int num){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	List<Map<String,Object>> result = this.getDatasWithNolocation(num, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return result;
    }
    
    public List<Map<String,Object>> getDatasWithNolocation(int num,JdbcUtils jdbcUtils){
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(num);
    	try{
    		result = jdbcUtils.findModeResult(sql_get_no_location, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return result;
    }
    
    public List<Map<String,Object>> getDatasWithNolocationPool(int num){
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(num);
    	try{
    		result = JdbcUtilsPool.findModeResult(sql_get_no_location, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return result;
    }
    
    public Boolean upLocationWithlnglat(String mac,Map<String,String> data,Long time){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	Boolean rs = this.upLocationWithlnglat(mac, data, time, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Boolean upLocationWithlnglat(String mac,Map<String,String> data,Long time,JdbcUtils jdbcUtils){
    	Boolean rs = true;
    	if(!data.isEmpty()){
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	String sql = sql_update_location;
	    	for(Map.Entry<String,String> entry : data.entrySet()){
	    		String skey = entry.getKey().toString();
	    		String value = entry.getValue();
	    		sql += "`"+skey+"`=?,";
	    		paramsQuery.add(value);
	    	}
	    	sql += "updated_at=? WHERE `dev_mac`=?";
	    	paramsQuery.add(time);
	    	paramsQuery.add(mac);
	    	try{
	    		rs = jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		rs = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return rs;
    }
    
    public Boolean upLocationWithlnglatPool(String mac,Map<String,String> data,Long time){
    	Boolean rs = true;
    	if(!data.isEmpty()){
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	String sql = sql_update_location;
	    	for(Map.Entry<String,String> entry : data.entrySet()){
	    		String skey = entry.getKey().toString();
	    		String value = entry.getValue();
	    		sql += "`"+skey+"`=?,";
	    		paramsQuery.add(value);
	    	}
	    	sql += "updated_at=? WHERE `dev_mac`=?";
	    	paramsQuery.add(time);
	    	paramsQuery.add(mac);
	    	try{
	    		rs = JdbcUtilsPool.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		rs = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return rs;
    }
    
    public Boolean upLocationLstatus(List<String> macs,String lstatus,Long time){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	Boolean rs = this.upLocationLstatus(macs, lstatus, time, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Boolean upLocationLstatus(List<String> macs,String lstatus,Long time,JdbcUtils jdbcUtils){
    	Boolean rs = true;
    	if(!macs.isEmpty()){
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	String sql = sql_update_location_lstatus;
	    	paramsQuery.add(lstatus);
	    	paramsQuery.add(time);
	    	for(int i=0;i<macs.size();i++){
	    		sql += "?,";
	    		paramsQuery.add(macs.get(i));
	    	}
	    	sql = sql.substring(0,sql.length()-1)+")";
	    	try{
	    		rs = jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		rs = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return rs;
    }
    
    public Boolean upLocationLstatusPool(List<String> macs,String lstatus,Long time){
    	Boolean rs = true;
    	if(!macs.isEmpty()){
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	String sql = sql_update_location_lstatus;
	    	paramsQuery.add(lstatus);
	    	paramsQuery.add(time);
	    	for(int i=0;i<macs.size();i++){
	    		sql += "?,";
	    		paramsQuery.add(macs.get(i));
	    	}
	    	sql = sql.substring(0,sql.length()-1)+")";
	    	try{
	    		rs = JdbcUtilsPool.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		rs = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return rs;
    }
    
    public Boolean saveUserStatics(JSONObject datas){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean rs = this.saveUserStatics(datas, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Boolean saveUserStatics(JSONObject datas,JdbcUtils jdbcUtils){
    	Boolean rs = true;
    	if(!datas.isEmpty()){
	    	String sql = sql_add_user_statics;
	    	List<Object> paramsQuery = new ArrayList<Object>();
	
	    	for(Map.Entry<String,Object> entry : datas.entrySet()){
				JSONObject value = JSON.parseObject(entry.getValue().toString());
				for(Map.Entry<String,Object> entry1 : value.entrySet()){
					JSONObject valueObj = JSON.parseObject(entry1.getValue().toString());
					sql += "(?,?,?,?,?),";
					paramsQuery.add(valueObj.getString("uid"));
					paramsQuery.add(valueObj.getString("onlines"));
					paramsQuery.add(valueObj.getString("hours"));
					paramsQuery.add(valueObj.getString("created_at"));
					paramsQuery.add(valueObj.getString("updated_at"));
				}
	    	}
	    	sql = sql.substring(0,sql.length()-1);
	    	try{
	    		rs = jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		rs = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return rs;
    }
    
    public Boolean saveUserStaticsPool(JSONObject datas){
    	Boolean rs = true;
    	if(!datas.isEmpty()){
	    	String sql = sql_add_user_statics;
	    	List<Object> paramsQuery = new ArrayList<Object>();
	
	    	for(Map.Entry<String,Object> entry : datas.entrySet()){
				JSONObject value = JSON.parseObject(entry.getValue().toString());
				for(Map.Entry<String,Object> entry1 : value.entrySet()){
					JSONObject valueObj = JSON.parseObject(entry1.getValue().toString());
					sql += "(?,?,?,?,?),";
					paramsQuery.add(valueObj.getString("uid"));
					paramsQuery.add(valueObj.getString("onlines"));
					paramsQuery.add(valueObj.getString("hours"));
					paramsQuery.add(valueObj.getString("created_at"));
					paramsQuery.add(valueObj.getString("updated_at"));
				}
	    	}
	    	sql = sql.substring(0,sql.length()-1);
	    	try{
	    		rs = JdbcUtilsPool.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		rs = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return rs;
    }
    
    public Boolean saveClientsStatics(JSONObject datas){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean rs = this.saveClientsStatics(datas, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Boolean saveClientsStatics(JSONObject datas,JdbcUtils jdbcUtils){
    	Boolean rs = true;
    	if(!datas.isEmpty()){
	    	String sql = sql_add_client_statics;
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	for(Map.Entry<String,Object> entry : datas.entrySet()){
				JSONObject value = JSON.parseObject(entry.getValue().toString());
				sql += "(?,?,?,?,?,?),";
				paramsQuery.add(value.getString("mac"));
				paramsQuery.add(value.getString("uid"));
				paramsQuery.add(value.getString("onlines"));
				paramsQuery.add(value.getString("hours"));
				paramsQuery.add(value.getString("created_at"));
				paramsQuery.add(value.getString("updated_at"));
	
	    	}
	    	sql = sql.substring(0,sql.length()-1);
	    	try{
	    		rs = jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		rs = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return rs;
    }
    
    public Boolean saveClientsStaticsPool(JSONObject datas){
    	Boolean rs = true;
    	if(!datas.isEmpty()){
	    	String sql = sql_add_client_statics;
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	for(Map.Entry<String,Object> entry : datas.entrySet()){
				JSONObject value = JSON.parseObject(entry.getValue().toString());
				sql += "(?,?,?,?,?,?),";
				paramsQuery.add(value.getString("mac"));
				paramsQuery.add(value.getString("uid"));
				paramsQuery.add(value.getString("onlines"));
				paramsQuery.add(value.getString("hours"));
				paramsQuery.add(value.getString("created_at"));
				paramsQuery.add(value.getString("updated_at"));
	
	    	}
	    	sql = sql.substring(0,sql.length()-1);
	    	try{
	    		rs = JdbcUtilsPool.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		rs = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return rs;
    }
    
    public Boolean saveStaticsStatus(List<Object>ids,String status){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean rs = this.saveStaticsStatus(ids, status, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Boolean saveStaticsStatus(List<Object>ids,String status,JdbcUtils jdbcUtils){
    	Boolean rs = true;
    	if(!ids.isEmpty()){
	    	String sql = sql_update_statics_status;
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	paramsQuery.add(status);
	    	for(int i=0;i<ids.size();i++){
	    		sql += "?,";
	    		paramsQuery.add(ids.get(i));
	    	}
	    	sql = sql.substring(0,sql.length()-1)+")";
	    	try{
	    		rs = jdbcUtils.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		rs = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return rs;
    }
    
    public Boolean saveStaticsStatusPool(List<Object> ids,String status){
    	Boolean rs = true;
    	if(!ids.isEmpty()){
	    	String sql = sql_update_statics_status;
	    	List<Object> paramsQuery = new ArrayList<Object>();
	    	paramsQuery.add(status);
	    	for(int i=0;i<ids.size();i++){
	    		sql += "?,";
	    		paramsQuery.add(ids.get(i));
	    	}
	    	sql = sql.substring(0,sql.length()-1)+")";
	    	try{
	    		rs = JdbcUtilsPool.updateByPreparedStatement(sql, paramsQuery);
	    	}catch(Exception e){
	    		rs = false;
	    		log.error(e.getMessage());
	    	}
    	}
    	return rs;
    }
    
    public Map<String,Object> getAuthMember(String mid){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Map<String,Object> result = this.getAuthMember(mid,jdbcUtils);
    	jdbcUtils.releaseConn();
    	return result;
    }
    
    public Map<String,Object> getAuthMember(String mid,JdbcUtils jdbcUtils){
    	Map<String,Object> result = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mid);
    	try{
    		result = jdbcUtils.findSimpleResult(sql_auth_member, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return result;
    }
    
    public Map<String,Object> getAuthMemberPool(String mid){
    	Map<String,Object> result = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mid);
    	try{
    		result = JdbcUtilsPool.findSimpleResult(sql_auth_member, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return result;
    }
    
    public List<Map<String,Object>> getAlarmSet(String mac){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	List<Map<String,Object>> result = this.getAlarmSet(mac, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return result;
    }
    
    public List<Map<String,Object>> getAlarmSet(String mac,JdbcUtils jdbcUtils){
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		result = jdbcUtils.findModeResult(sql_get_alarm_set, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return result;
    }
    
    public List<Map<String,Object>> getAlarmSetPool(String mac){
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		result = JdbcUtilsPool.findModeResult(sql_get_alarm_set, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return result;
    }
    
    public Map<String,Object> getAlarmNoticeSet(String mac){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Map<String,Object> result = this.getAlarmNoticeSet(mac, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return result;
    }
    
    public Map<String,Object> getAlarmNoticeSet(String mac,JdbcUtils jdbcUtils){
    	Map<String,Object> result = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		List<Map<String,Object>> infos = jdbcUtils.findModeResult(sql_get_alarm_notice_set, paramsQuery);
    		if(!infos.isEmpty()){
    			result.put("lang", infos.get(0).get("lang"));
    			List<Map<String,Object>> users = new ArrayList<Map<String,Object>>();
    			for(int i=0;i<infos.size();i++){
    				Map<String,Object> user = new HashMap<String,Object>();
    				user.put("uid",infos.get(i).get("uid"));
    				user.put("username",infos.get(i).get("username"));
    				user.put("email",infos.get(i).get("email"));
    				user.put("timeZone",infos.get(i).get("timeZone"));
    				user.put("isSummerTime",infos.get(i).get("isSummerTime"));
    				user.put("open_id",infos.get(i).get("open_id"));
    				users.add(user);
    			}
    			result.put("user",JSON.parseArray(JSON.toJSONString(users)));
    		}
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	
    	return result;
    }
    
    public Map<String,Object> getAlarmNoticeSetPool(String mac){
    	Map<String,Object> result = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		List<Map<String,Object>> infos = JdbcUtilsPool.findModeResult(sql_get_alarm_notice_set, paramsQuery);
    		if(!infos.isEmpty()){
    			result.put("lang", infos.get(0).get("lang"));
    			List<Map<String,Object>> users = new ArrayList<Map<String,Object>>();
    			for(int i=0;i<infos.size();i++){
    				Map<String,Object> user = new HashMap<String,Object>();
    				user.put("uid",infos.get(i).get("uid"));
    				user.put("username",infos.get(i).get("username"));
    				user.put("email",infos.get(i).get("email"));
    				user.put("timeZone",infos.get(i).get("timeZone"));
    				user.put("isSummerTime",infos.get(i).get("isSummerTime"));
    				user.put("open_id",infos.get(i).get("open_id"));
    				users.add(user);
    			}
    			result.put("user",JSON.parseArray(JSON.toJSONString(users)));
    		}
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	
    	return result;
    }
    
    public Boolean addDeviceAlarm(String mac,String content,Long time){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean rs = this.addDeviceAlarm(mac, content, time, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Boolean addDeviceAlarm(String mac,String content,Long time,JdbcUtils jdbcUtils){
    	Boolean rs = false;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	paramsQuery.add(content);
    	paramsQuery.add(time);
    	paramsQuery.add(time);
    	try{
    		rs = jdbcUtils.updateByPreparedStatement(sql_add_alarm, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public Boolean addDeviceAlarmPool(String mac,String content,Long time){
    	Boolean rs = false;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	paramsQuery.add(content);
    	paramsQuery.add(time);
    	paramsQuery.add(time);
    	try{
    		rs = JdbcUtilsPool.updateByPreparedStatement(sql_add_alarm, paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public Map<String,Object> getCommandByCid(String cid){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Map<String,Object> rs = this.getCommandByCid(cid,jdbcUtils);
    	jdbcUtils.releaseConn();
		return rs;
    }
    
    public Map<String,Object> getCommandByCid(String cid,JdbcUtils jdbcUtils){
    	Map<String,Object> result = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(cid);
		try{
			result = jdbcUtils.findSimpleResult(sql_command_get,paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return result;
    }
    
    public Map<String,Object> getCommandByCidPool(String cid){
    	Map<String,Object> result = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(cid);
		try{
			result = JdbcUtilsPool.findSimpleResult(sql_command_get,paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return result;
    }
    
    public Boolean saveCommandStatus(String cid,String mac,String status,Long time){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Boolean rs = this.saveCommandStatus(cid, mac, status, time, jdbcUtils);
    	jdbcUtils.releaseConn();
		return rs;
    }
    
    public Boolean saveCommandStatus(String cid,String mac,String status,Long time,JdbcUtils jdbcUtils){
    	Boolean rs = false;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(status);
    	paramsQuery.add(time);
		paramsQuery.add(mac);
		paramsQuery.add(cid);
		try{
			rs = jdbcUtils.updateByPreparedStatement(Command.sql_update,paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return rs;
    }
    
    public Boolean saveCommandStatusPool(String cid,String mac,String status,Long time){
    	Boolean rs = false;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(status);
    	paramsQuery.add(time);
		paramsQuery.add(mac);
		paramsQuery.add(cid);
		try{
			rs = JdbcUtilsPool.updateByPreparedStatement(Command.sql_update,paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return rs;
    }
    
    public List<Map<String,Object>> getDeviceByUid(String uid){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	List<Map<String,Object>> result = this.getDeviceByUid(uid, jdbcUtils);
    	jdbcUtils.releaseConn();
    	return result;
    }
    
    public List<Map<String,Object>> getDeviceByUid(String uid,JdbcUtils jdbcUtils){
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(uid);
    	try{
    		result = jdbcUtils.findModeResult(sql_get_user_childs,paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return result;
    }
    
    public List<Map<String,Object>> getDeviceByUidPool(String uid){
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(uid);
    	try{
    		result = JdbcUtilsPool.findModeResult(sql_get_user_childs,paramsQuery);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return result;
    }
    
    public List<Map<String,Object>> getOrderInfo(String orderID){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	List<Map<String,Object>> result = this.getOrderInfo(orderID,jdbcUtils);
    	jdbcUtils.releaseConn();
		return result;
    }
    
    public List<Map<String,Object>> getOrderInfo(String orderID,JdbcUtils jdbcUtils){
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(orderID);
		try{
			result = jdbcUtils.findModeResult(sql_get_orderinfo,paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return result;
    }
    
    public List<Map<String,Object>> getOrderInfoPool(String orderID){
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(orderID);
		try{
			result = JdbcUtilsPool.findModeResult(sql_get_orderinfo,paramsQuery);
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return result;
    }
    
    public Map<String, Object> getDeviceAuthWithPolicy(String mac,String radioid,String vapid){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Map<String,Object> result = this.getDeviceAuthWithPolicy(mac,radioid,vapid,jdbcUtils);
    	jdbcUtils.releaseConn();
		return result;
    }
    
    public Map<String, Object> getDeviceAuthWithPolicy(String mac,String radioid,String vapid,JdbcUtils jdbcUtils){
    	List<Map<String, Object>> results =new ArrayList<Map<String,Object>>();
    	Map<String,Object> result = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
		if(radioid == null || radioid.isEmpty()){
			paramsQuery.add("");
		}else{
			paramsQuery.add(radioid);
		}
		if(vapid == null || vapid.isEmpty()){
			paramsQuery.add("");
		}else{
			paramsQuery.add(vapid);
		}
		paramsQuery.add(mac);
		try{
			results = jdbcUtils.findModeResult(sql_auth_with_policy_get,paramsQuery);
			if(!results.isEmpty()){
				Map<String,Object> data = results.get(0);
				if(data.get("policy_id") != "" && data.get("portal_id") != ""){
					Map<String,Map<String,String>> defaultPortal = publicConf.defaultPortal;
					result.put("prtid", data.get("prtid"));
					result.put("cltid", data.get("cltid"));
					result.put("dev_mac", data.get("dev_mac"));
					result.put("dev_name", data.get("dev_name"));
					result.put("uid", data.get("uid"));
					result.put("group_id", data.get("group_id"));
					result.put("radioid", data.get("radioid"));
					result.put("vapid", data.get("vapid"));
					result.put("enable", data.get("enable"));
					result.put("method", data.get("method"));
					result.put("portal_id", data.get("portal_id"));
					result.put("portal_url", defaultPortal);
					result.put("policy_id", data.get("policy_id"));
					result.put("lang", data.get("lang"));
					result.put("expire_in", data.get("expire_in"));
					result.put("heartbeat", data.get("heartbeat"));
					result.put("white_domain", data.get("white_domain"));
					result.put("black_domain", data.get("black_domain"));
					result.put("white_mac", data.get("white_mac"));
					result.put("black_mac", data.get("black_mac"));
					
					for(int i=0;i<results.size();i++){
						Map<String,Object> tmp = results.get(i);
						String resource = tmp.get("resource").toString();
						String type = tmp.get("type").toString();
						if((resource.equals("1")||resource.equals("2")) && (type.equals("before")||type.equals("process")||type.equals("after"))){
							String key = "pc";
							if(resource.equals("2")){
								key = "mobile";
							}
							Map<String,String> typeUrl = defaultPortal.get(key);
							typeUrl.put(type,tmp.get("url").toString());
							defaultPortal.put(key, typeUrl);
						}
					}
				}			
			}
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return result;
    }
    
    public Map<String, Object> getDeviceAuthWithPolicyPool(String mac,String radioid,String vapid){
    	List<Map<String, Object>> results =new ArrayList<Map<String,Object>>();
    	Map<String,Object> result = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
		if(radioid == null || radioid.isEmpty()){
			paramsQuery.add("");
		}else{
			paramsQuery.add(radioid);
		}
		if(vapid == null || vapid.isEmpty()){
			paramsQuery.add("");
		}else{
			paramsQuery.add(vapid);
		}
		paramsQuery.add(mac);
		try{
			results = JdbcUtilsPool.findModeResult(sql_auth_with_policy_get,paramsQuery);
			if(!results.isEmpty()){
				Map<String,Object> data = results.get(0);
				if(data.get("policy_id") != "" && data.get("portal_id") != ""){
					Map<String,Map<String,String>> defaultPortal = publicConf.defaultPortal;
					result.put("prtid", data.get("prtid"));
					result.put("cltid", data.get("cltid"));
					result.put("dev_mac", data.get("dev_mac"));
					result.put("dev_name", data.get("dev_name"));
					result.put("uid", data.get("uid"));
					result.put("group_id", data.get("group_id"));
					result.put("radioid", data.get("radioid"));
					result.put("vapid", data.get("vapid"));
					result.put("enable", data.get("enable"));
					result.put("method", data.get("method"));
					result.put("portal_id", data.get("portal_id"));
					result.put("portal_url", defaultPortal);
					result.put("policy_id", data.get("policy_id"));
					result.put("lang", data.get("lang"));
					result.put("expire_in", data.get("expire_in"));
					result.put("heartbeat", data.get("heartbeat"));
					result.put("white_domain", data.get("white_domain"));
					result.put("black_domain", data.get("black_domain"));
					result.put("white_mac", data.get("white_mac"));
					result.put("black_mac", data.get("black_mac"));
					
					for(int i=0;i<results.size();i++){
						Map<String,Object> tmp = results.get(i);
						String resource = tmp.get("resource").toString();
						String type = tmp.get("type").toString();
						if((resource.equals("1")||resource.equals("2")) && (type.equals("before")||type.equals("process")||type.equals("after"))){
							String key = "pc";
							if(resource.equals("2")){
								key = "mobile";
							}
							Map<String,String> typeUrl = defaultPortal.get(key);
							typeUrl.put(type,tmp.get("url").toString());
							defaultPortal.put(key, typeUrl);
						}
					}
				}			
			}
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return result;
    }
    
    public Boolean clearDeviceAuth(String mac){
    	Boolean rs = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	rs = this.clearDeviceAuth(mac,jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Boolean clearDeviceAuth(String mac,JdbcUtils jdbcUtils){
    	Boolean rs = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		jdbcUtils.updateByPreparedStatement(sql_clear_device_auth,paramsQuery);
    	}catch(Exception e){
    		rs = false;
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public Boolean clearDeviceAuthPool(String mac){
    	Boolean rs = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(mac);
    	try{
    		JdbcUtilsPool.updateByPreparedStatement(sql_clear_device_auth,paramsQuery);
    	}catch(Exception e){
    		rs = false;
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public Boolean setAuthLoginInfo(String token,String mac,String client_mac,String status){
    	Boolean rs = true;
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	rs = this.setAuthLoginInfo(token, mac, client_mac, status,jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Boolean setAuthLoginInfo(String token,String mac,String client_mac,String status,JdbcUtils jdbcUtils){
    	Boolean rs = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(status);
    	paramsQuery.add(mac);
    	paramsQuery.add(client_mac);
    	paramsQuery.add(token);
    	try{
    		jdbcUtils.updateByPreparedStatement(sql_auth_login_set,paramsQuery);
    	}catch(Exception e){
    		rs = false;
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public Boolean setAuthLoginInfoPool(String token,String mac,String client_mac,String status){
    	Boolean rs = true;
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(status);
    	paramsQuery.add(mac);
    	paramsQuery.add(client_mac);
    	paramsQuery.add(token);
    	try{
    		JdbcUtilsPool.updateByPreparedStatement(sql_auth_login_set,paramsQuery);
    	}catch(Exception e){
    		rs = false;
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public Boolean handleGroupNums(String gid){
    	Boolean rs = true;
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	rs = this.handleGroupNums(gid,jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Boolean handleGroupNums(String gid,JdbcUtils jdbcUtils){
    	Boolean rs = true;
    	List<Object> paramsQuery = new ArrayList<Object>();	
		List<Object> paramsAddQuery = new ArrayList<Object>();
    	try{
    		paramsQuery.add(gid);
    		Map<String,Object> groupDevices = jdbcUtils.findSimpleResult(sql_group_devnums_get,paramsQuery);
    		
    		paramsAddQuery.add(groupDevices.get("device_nums").toString());
    		paramsAddQuery.add(gid);
    		jdbcUtils.updateByPreparedStatement(sql_handle_group_nums,paramsAddQuery);
    	}catch(Exception e){
    		rs = false;
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public Boolean handleGroupNumsPool(String gid){
    	Boolean rs = true;
    	List<Object> paramsQuery = new ArrayList<Object>();	
		List<Object> paramsAddQuery = new ArrayList<Object>();
    	try{
    		paramsQuery.add(gid);
    		Map<String,Object> groupDevices = JdbcUtilsPool.findSimpleResult(sql_group_devnums_get,paramsQuery);
    		
    		paramsAddQuery.add(groupDevices.get("device_nums").toString());
    		paramsAddQuery.add(gid);
    		JdbcUtilsPool.updateByPreparedStatement(sql_handle_group_nums,paramsAddQuery);
    	}catch(Exception e){
    		rs = false;
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public Map<String,Object> getCloudLatestPackageByType(String type){
    	JdbcUtils jdbcUtils = new JdbcUtils();
    	jdbcUtils.getConnection();
    	Map<String,Object> rs = this.getCloudLatestPackageByType(type,jdbcUtils);
    	jdbcUtils.releaseConn();
    	return rs;
    }
    
    public Map<String,Object> getCloudLatestPackageByType(String type,JdbcUtils jdbcUtils){
    	Map<String,Object> rs = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(1);
    	paramsQuery.add(0);
    	paramsQuery.add(type);
    	try{
    		List<Map<String,Object>> results = jdbcUtils.findModeResult(sql_packages_get,paramsQuery);
    		if(!results.isEmpty()){
    			rs = results.get(0);
    			for(Map<String,Object> result:results){
    				if(Helper.compareVersion(rs.get("version").toString(), result.get("version").toString())){
    					rs = result;
    				}
    			}
    		}
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    public Map<String,Object> getCloudLatestPackageByTypePool(String type){
    	Map<String,Object> rs = new HashMap<String,Object>();
    	List<Object> paramsQuery = new ArrayList<Object>();
    	paramsQuery.add(1);
    	paramsQuery.add(0);
    	paramsQuery.add(type);
    	try{
    		List<Map<String,Object>> results = JdbcUtilsPool.findModeResult(sql_packages_get,paramsQuery);
    		if(!results.isEmpty()){
    			rs = results.get(0);
    			for(Map<String,Object> result:results){
    				if(Helper.compareVersion(rs.get("version").toString(), result.get("version").toString())){
    					rs = result;
    				}
    			}
    		}
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    	return rs;
    }
    
    @Override  
    public String toString() {  
        return mysqlConf.prefix+"device [id=" + id + ",user_id=" + user_id + ",dev_mac=" + dev_mac + ",dev_ip=" + dev_ip + ",net_ip="+net_ip+",heartbeat="+heartbeat+",name="+name+",prtid="+prtid+",prt_type="+prt_type+","
        		+ ",prt_size="+prt_size+",cltid="+cltid+",type="+type+",mode="+mode+",version="+version+",up_time="+up_time+",pid="+pid+",area="+area+",country="+country+",province="+province+",city="+city+""
        				+ ",address="+address+",latitude="+latitude+",longitude="+longitude+",chip="+chip+",sn="+sn+",notes="+notes+",group_id="+group_id+",is_ip_location="+is_ip_location+""
        						+ ",bind="+bind+",is_del="+is_del+",join_time="+join_time+",dev_username="+dev_username+",weblogin_pwd="+weblogin_pwd+",lstatus="+lstatus+",created_at="+created_at+",updated_at="+updated_at+"]";  
    }  
}
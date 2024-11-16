package lib;

import java.lang.reflect.Field;  
import java.sql.Connection;  
import java.sql.DriverManager;  
import java.sql.PreparedStatement;  
import java.sql.ResultSet;  
import java.sql.ResultSetMetaData;  
import java.sql.SQLException;  
import java.util.ArrayList;  
import java.util.HashMap;  
import java.util.List;  
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import conf.mysqlConf;

public class JdbcUtils {
	private static final Log log = LogFactory.getLog(JdbcUtils.class);
//	//数据库用户名  
//    private static final String USERNAME = "root";  
//    //数据库密码  
//    private static final String PASSWORD = "yanzi";  
//    //驱动信息   
//    private static final String DRIVER = "com.mysql.jdbc.Driver";  
//    //数据库地址  
//    private static final String URL = "jdbc:mysql://localhost:3306/mydb";  
    private Connection connection;  
    private PreparedStatement pstmt;  
    private ResultSet resultSet;  
    public JdbcUtils() {  
        // TODO Auto-generated constructor stub  
        try{  
            Class.forName(mysqlConf.driver);  
        }catch(Exception e){  
        	log.error("数据库连接失败！"+e.getMessage());	
        }  
    }  
      
    /** 
     * 获得数据库的连接 
     * @return 
     */  
    public Connection getConnection(){  
        try {  
            connection = DriverManager.getConnection(mysqlConf.url+"&user="+mysqlConf.username+"&password="+mysqlConf.password);  
        } catch (SQLException e) {  
            // TODO Auto-generated catch block  
        	log.error(e.getMessage()); 
        	
        }  
        return connection;  
    }
    
    public void setAutoCommit (Boolean autoCommit){
    	try{
    		connection.setAutoCommit(autoCommit);
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    }
    
    public void rollback(){
    	try{
    		connection.rollback();
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    }
    
    public void commit(){
    	try{
    		connection.commit();
    	}catch(Exception e){
    		log.error(e.getMessage());
    	}
    }
  
      
    /** 
     * 增加、删除、改 
     * @param sql 
     * @param params 
     * @return 
     * @throws SQLException 
     */  
    public boolean updateByPreparedStatement(String sql, List<Object>params)throws SQLException{
    	log.info(sql + params.toString());
        boolean flag = false;  
        int result = -1;  
        pstmt = connection.prepareStatement(sql);  
        int index = 1;  
        if(params != null && !params.isEmpty()){  
            for(int i=0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        result = pstmt.executeUpdate();
        
        flag = result >= 0 ? true : false;  
        return flag;  
    }  
    
    //生成记录的key
    public Long getGeneratedKeys() throws SQLException
    {
    	Long ret=Long.MIN_VALUE;
    	 pstmt = connection.prepareStatement("SELECT LAST_INSERT_ID()");
    	 
    	ResultSet rs = pstmt.executeQuery();    
        if (rs.next()) {    
        	ret= rs.getLong(1);
        }
        return ret;
    }
  
    /** 
     * 查询单条记录 
     * @param sql 
     * @param params 
     * @return 
     * @throws SQLException 
     */  
	public Map<String, Object> findSimpleResult(String sql, List<Object> params) throws SQLException{
		log.info(sql + params.toString());
        Map<String, Object> map = new HashMap<String, Object>();  
        int index  = 1;  
        pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i=0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        resultSet = pstmt.executeQuery();//返回查询结果  
        ResultSetMetaData metaData = resultSet.getMetaData();  
        int col_len = metaData.getColumnCount();  
        while(resultSet.next()){  
            for(int i=0; i<col_len; i++ ){  
                String cols_name = metaData.getColumnLabel(i+1);
                Object cols_value = resultSet.getObject(cols_name);  
                if(cols_value == null){  
                    cols_value = "";  
                }  
                map.put(cols_name, cols_value);  
            }  
        }  
        return map;  
    }  
  
    /**查询多条记录 
     * @param sql 
     * @param params 
     * @return 
     * @throws SQLException 
     */  
    public List<Map<String, Object>> findModeResult(String sql, List<Object> params) throws SQLException{
    	log.info(sql + params.toString());
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();  
        int index = 1;  
        pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i = 0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        resultSet = pstmt.executeQuery();  
        ResultSetMetaData metaData = resultSet.getMetaData();  
        int cols_len = metaData.getColumnCount();

        while(resultSet.next()){  
            Map<String, Object> map = new HashMap<String, Object>();  
            for(int i=0; i<cols_len; i++){  
                String cols_name = metaData.getColumnLabel(i+1);
                Object cols_value = resultSet.getObject(cols_name);  
                if(cols_value == null){  
                    cols_value = "";  
                }  
                map.put(cols_name, cols_value);  
            }  
            list.add(map);  
        }  
  
        return list;  
    }  
  
    /**通过反射机制查询单条记录 
     * @param sql 
     * @param params 
     * @param cls 
     * @return 
     * @throws Exception 
     */  
    public <T> T findSimpleRefResult(String sql, List<Object> params,  
            Class<T> cls )throws Exception{
    	log.info(sql + params.toString());
        T resultObject = null;  
        int index = 1;  
        pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i = 0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        resultSet = pstmt.executeQuery();  
        ResultSetMetaData metaData  = resultSet.getMetaData();  
        int cols_len = metaData.getColumnCount();  
        while(resultSet.next()){  
            //通过反射机制创建一个实例  
            resultObject = cls.newInstance();  
            for(int i = 0; i<cols_len; i++){  
                String cols_name = metaData.getColumnName(i+1);  
                Object cols_value = resultSet.getObject(cols_name);  
                if(cols_value == null){  
                    cols_value = "";  
                }  
                Field field = cls.getDeclaredField(cols_name);
                field.setAccessible(true); //打开javabean的访问权限  
                field.set(resultObject, cols_value);  
            }  
        }  
        return resultObject;  
  
    }  
    
    /**通过反射机制查询单条记录 
     * @param sql 
     * @param params 
     * @param cls 
     * @return 
     * @throws Exception 
     */  
    @SuppressWarnings("unused")
	public Object findSimpleRefResult(String sql)throws Exception{
    	log.info(sql);
    	Object resultObject = null;  
        //int index = 1;  
        pstmt = connection.prepareStatement(sql);  
       
        resultSet = pstmt.executeQuery();  
        ResultSetMetaData metaData  = resultSet.getMetaData();  
        int cols_len = metaData.getColumnCount();  
        if (resultSet.next()){  
            //通过反射机制创建一个实例
            for(int i = 0; i<cols_len; i++){  
                String cols_name = metaData.getColumnName(i+1); 
                Object cols_value = resultSet.getObject(cols_name);  
                if(cols_value == null){  
                    cols_value = "";  
                }  
                return cols_value;
            }  
        }  
        return resultObject;  
  
    }  
  
    /**通过反射机制查询多条记录 
     * @param sql  
     * @param params 
     * @param cls 
     * @return 
     * @throws Exception 
     */  
    public <T> List<T> findMoreRefResult(String sql, List<Object> params,  
            Class<T> cls )throws Exception {
    	log.info(sql + params.toString());
        List<T> list = new ArrayList<T>();  
        int index = 1;  
        pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i = 0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        resultSet = pstmt.executeQuery();  
        ResultSetMetaData metaData  = resultSet.getMetaData();  
        int cols_len = metaData.getColumnCount();  
        while(resultSet.next()){  
            //通过反射机制创建一个实例  
            T resultObject = cls.newInstance();  
            for(int i = 0; i<cols_len; i++){  
                String cols_name = metaData.getColumnName(i+1);  
                Object cols_value = resultSet.getObject(cols_name);  
                if(cols_value == null){  
                    cols_value = "";  
                }  
                Field field = cls.getDeclaredField(cols_name);  
                field.setAccessible(true); //打开javabean的访问权限  
                field.set(resultObject, cols_value);  
            }  
            list.add(resultObject);  
        }  
        return list;  
    }  
  
    /** 
     * 释放数据库连接 
     */  
    public void releaseConn(){  
        if(resultSet != null){  
            try{  
                resultSet.close();
                resultSet=null;
            }catch(SQLException e){  
            	log.error(e.getMessage());
            }  
        }
        if(connection != null){  
            try{ 
            	
            	connection.close();
            	connection=null;            	
            }catch(SQLException e){  
            	log.error(e.getMessage());
            }  
        }
        if(pstmt != null){  
            try{ 
            	
            	pstmt.close();
            	pstmt=null;            	
            }catch(SQLException e){  
            	log.error(e.getMessage());
            }  
        }
        
    }  
  
}

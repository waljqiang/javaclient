package lib;

import java.lang.reflect.Field;  
import java.sql.Connection;
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

public class JdbcUtilsPool {
	private static final Log log = LogFactory.getLog(JdbcUtilsPool.class);  
    /** 
     * 增加、删除、改 
     * @param sql 
     * @param params 
     * @return 
     * @throws SQLException 
     */  
    public static boolean updateByPreparedStatement(String sql, List<Object>params)throws SQLException{
    	log.info(sql + params.toString());
        boolean flag = false;  
        int result = -1;
        Connection connection = DruidUtils.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql);  
        int index = 1;  
        if(params != null && !params.isEmpty()){  
            for(int i=0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        result = pstmt.executeUpdate();
        
        flag = result >= 0 ? true : false;
        DruidUtils.close(null, pstmt, connection);
        return flag;  
    }  
    
    //生成记录的key
    public static Long getGeneratedKeys() throws SQLException{
    	Long ret=Long.MIN_VALUE;
    	Connection connection = DruidUtils.getConnection();
    	PreparedStatement pstmt = connection.prepareStatement("SELECT LAST_INSERT_ID()");
    	ResultSet rs = pstmt.executeQuery();    
        if (rs.next()) {    
        	ret= rs.getLong(1);
        }
        DruidUtils.close(rs, pstmt, connection);
        return ret;
    }
  
    /** 
     * 查询单条记录 
     * @param sql 
     * @param params 
     * @return 
     * @throws SQLException 
     */  
	public static Map<String, Object> findSimpleResult(String sql, List<Object> params) throws SQLException{
		log.info(sql + params.toString());
        Map<String, Object> map = new HashMap<String, Object>();  
        int index  = 1;  
        Connection connection = DruidUtils.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i=0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        ResultSet resultSet = pstmt.executeQuery();//返回查询结果  
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
        DruidUtils.close(resultSet, pstmt, connection);
        return map;  
    }  
  
    /**查询多条记录 
     * @param sql 
     * @param params 
     * @return 
     * @throws SQLException 
     */  
    public static List<Map<String, Object>> findModeResult(String sql, List<Object> params) throws SQLException{
    	log.info(sql + params.toString());
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();  
        int index = 1;  
        Connection connection = DruidUtils.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i = 0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        ResultSet resultSet = pstmt.executeQuery();  
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
        DruidUtils.close(resultSet, pstmt, connection);
        return list;  
    }  
  
    /**通过反射机制查询单条记录 
     * @param sql 
     * @param params 
     * @param cls 
     * @return 
     * @throws Exception 
     */  
    public static <T> T findSimpleRefResult(String sql, List<Object> params,  
            Class<T> cls )throws Exception{
    	log.info(sql + params.toString());
        T resultObject = null;  
        int index = 1;  
        Connection connection = DruidUtils.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i = 0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        ResultSet resultSet = pstmt.executeQuery();  
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
        DruidUtils.close(resultSet, pstmt, connection);
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
	public static Object findSimpleRefResult(String sql)throws Exception{
    	log.info(sql);
    	Object resultObject = null;  
        //int index = 1;  
    	Connection connection = DruidUtils.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql); 
        
        ResultSet resultSet = pstmt.executeQuery();  
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
        DruidUtils.close(resultSet, pstmt, connection);
        return resultObject;  
  
    }  
  
    /**通过反射机制查询多条记录 
     * @param sql  
     * @param params 
     * @param cls 
     * @return 
     * @throws Exception 
     */  
    public static <T> List<T> findMoreRefResult(String sql, List<Object> params,  
            Class<T> cls )throws Exception {
    	log.info(sql + params.toString());
        List<T> list = new ArrayList<T>();  
        int index = 1;  
        Connection connection = DruidUtils.getConnection();
        PreparedStatement pstmt = connection.prepareStatement(sql);  
        if(params != null && !params.isEmpty()){  
            for(int i = 0; i<params.size(); i++){  
                pstmt.setObject(index++, params.get(i));  
            }  
        }  
        ResultSet resultSet = pstmt.executeQuery();  
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
        DruidUtils.close(resultSet, pstmt, connection);
        return list;  
    }  
  
}

package lib;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import conf.mysqlConf;

public class DruidUtils {
	private static final Log log = LogFactory.getLog(DruidUtils.class);
	
	private static DataSource dataSource;
	
	
	static{
		try{		
			Properties properties = new Properties();
			properties.put("driverClassName",mysqlConf.driver);
			properties.put("url", mysqlConf.url+"&rewriteBatchedStatements=true");
			properties.put("username", mysqlConf.username);
			properties.put("password", mysqlConf.password);
			properties.put("initialSize",mysqlConf.initialSize);
			properties.put("minIdle",mysqlConf.minIdle);
			properties.put("maxActive",mysqlConf.maxActive);
			properties.put("maxWait",mysqlConf.maxWait);
/*			properties.put("timeBetweenEvictionRunsMillis","60000");
			properties.put("minEvictableIdleTimeMillis","300000");
			properties.put("validationQuery","SELECT 1");
			properties.put("testWhileIdle","true");
			properties.put("testOnBorrow","false");
			properties.put("testOnReturn","false");
			properties.put("poolPreparedStatements","true");*/
			dataSource = DruidDataSourceFactory.createDataSource(properties);
		}catch(Exception e){
			log.error("JdbcUtilsPool failure,"+e.getMessage());
		}
	}
	
	public static Connection getConnection() throws SQLException{
		return dataSource.getConnection();
	}
	
	public static void close(ResultSet resultSet,Statement statement,Connection connection){
		try{
			if(resultSet != null){
				resultSet.close();
			}
			if(statement != null){
				statement.close();
			}
			if(connection != null){
				connection.close();
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
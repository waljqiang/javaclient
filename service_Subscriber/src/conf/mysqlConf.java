package conf;

public class mysqlConf extends config{
	public static final String driver = "com.mysql.jdbc.Driver";
	public static final String url = "jdbc:mysql://"+config.getIni("MYSQL_HOST","127.0.0.1")+":"+config.getIni("MYSQL_PORT","3306")+"/"+config.getIni("MYSQL_DBNAME","cloudnetlot")+"?"+"&useUnicode=true&useSSL=false&characterEncoding=UTF8&autoReconnect=true&rewriteBatchedStatements=true";
	public static final String username = config.getIni("MYSQL_USER","");
	public static final String password = config.getIni("MYSQL_PASSWORD","");
	public static final String prefix = config.getIni("MYSQL_TABLE_PREFIX", "cnl_");
	
	public static final String initialSize = config.getIni("MYSQL_POOL_INITIALSIZE","50");
	public static final String minIdle = config.getIni("MYSQL_POOL_MINIDLE","10");
	public static final String maxActive = config.getIni("MYSQL_POOL_MAXACTIVE","100");
	public static final String maxWait = config.getIni("MYSQL_POOL_MAXWAIT","5000");
}
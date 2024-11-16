package test;

import conf.mysqlConf;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestHandleLinkMysqlThread extends Thread{

    private String jdbcUrl;
    private String username;
    private String password;


    public TestHandleLinkMysqlThread()
    {
        this.jdbcUrl = mysqlConf.url;
        this.username = mysqlConf.username;
        this.password = mysqlConf.password;
    }

    @Override
    public void run() {
        //停止mysql服务后启动之后能检测到
        while (true) {
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                // 执行一个简单的查询来检查连接是否正常
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery("SELECT 1");
                    if (resultSet.next()) {
                        //1秒打印一次
                        System.out.println("MySQL连接正常。当前时间: " + System.currentTimeMillis());
                    }
                }
            } catch (SQLException e) {
                // 打印异常信息
                System.err.println("MySQL连接异常: " + e.getMessage());
            }

            // 等待一秒
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("监控线程被中断。");
                break;
            }
        }
    }
}

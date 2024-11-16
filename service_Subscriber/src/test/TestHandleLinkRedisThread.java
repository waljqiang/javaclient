package test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import conf.redisConf;
import test.enums.ConnectionResult;

public class TestHandleLinkRedisThread extends Thread{
    private JedisPool jedisPool; // 声明 jedisPool 成员变量
    //线程是否运行
    private volatile  boolean isRunning = true;
    private ConnectionResult connectionResult = ConnectionResult.FAILURE;
    public TestHandleLinkRedisThread()
    {
        // 配置连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxActive(redisConf.DRIVER_Redis_MAX_ACTIVE); // 设置最大连接数
        poolConfig.setMaxIdle(redisConf.DRIVER_Redis_MAX_IDLE); // 设置最大空闲连接数
        poolConfig.setMaxWait(redisConf.DRIVER_Redis_MAX_WAIT);
        poolConfig.setTestOnBorrow(redisConf.DRIVER_Redis_TEST_ON_BORROW); //
        if (redisConf.password.isEmpty())
            jedisPool = new JedisPool(poolConfig,redisConf.host,redisConf.port,redisConf.timeOut,null,redisConf.db);
        else
            jedisPool = new JedisPool(poolConfig,redisConf.host,redisConf.port,redisConf.timeOut,redisConf.password,redisConf.db);

    }

    @Override
    public void run() {
        while (isRunning) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                // 尝试发送一个ping命令来检查连接
                String pong = jedis.ping();
                if ("PONG".equals(pong)) {
                    System.out.println("Redis连接池连接成功！");
                    connectionResult = ConnectionResult.SUCCESS;
                    // 在这里执行连接成功后的操作
                   // break; // 如果只需要检测一次，连接成功后退出循环
                } else {
                    System.out.println("Redis连接池连接异常：未收到预期的PONG响应");
                    connectionResult = ConnectionResult.FAILURE;
                }
            } catch (Exception e) {
                // 捕获到异常，说明连接Redis服务器时出现问题
                System.out.println("Redis连接池连接异常：" + e.getMessage());
                connectionResult = ConnectionResult.FAILURE;
            }
            // 等待一段时间后再次尝试连接
            try {
                Thread.sleep(1000); // 等待1秒
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // 重新设置中断状态
                System.out.println("连接检测线程被中断");
                connectionResult = ConnectionResult.INTERRUPTED;
                break; // 如果线程被中断，退出循环
            }

        }


    }

    public void stopThread() {
        isRunning = false;
        System.out.println("停止线程");
    }

    public ConnectionResult getConnectionResult() {
        // 实现细节取决于你如何存储和处理线程的返回结果
        // 这里只是一个示例
        System.out.println("线程返回结果2: " + connectionResult);
        return connectionResult;
    }
}

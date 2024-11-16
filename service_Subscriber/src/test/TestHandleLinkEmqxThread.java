package test;

import conf.mqttConf;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import test.enums.ConnectionResult;

public class TestHandleLinkEmqxThread extends Thread{


    //线程是否运行
    private volatile  boolean isRunning = true;
    private ConnectionResult connectionResult = ConnectionResult.FAILURE;

    private String host;
    private String port;
    private String username;
    private String password;
    private boolean clean;
    private Short keepalive;





    public TestHandleLinkEmqxThread() {
        this.host = mqttConf.address;
        this.port = String.valueOf(mqttConf.port);
        this.username = mqttConf.username;
        this.password = mqttConf.password;
        this.clean = mqttConf.clean;
        this.keepalive = mqttConf.keepalive;
    }

    @Override
    public void run() {


         while (isRunning){

             MQTT mqtt = new MQTT();
             try {
                 mqtt.setHost("tcp://"+ host+':'+port);
                 mqtt.setClientId("EmqxStatusCheckerClient");
                 mqtt.setUserName(username);
                 mqtt.setPassword(password);
                 mqtt.setCleanSession(clean);
                 mqtt.setKeepAlive( keepalive);
                 //检查线程是否被中断
                 while (!Thread.currentThread().isInterrupted()) {
                     try {
                         //创建一个阻塞连接对象，用于与 MQTT 代理服务器进行通信。
                         BlockingConnection connection = mqtt.blockingConnection();
                         connection.connect();
                         System.out.println("EMQX服务端连接成功！");
                         connection.disconnect();
                         connectionResult = ConnectionResult.SUCCESS;
                     } catch (Exception e) {
                         //断开之后走不到这里
                         System.err.println("EMQX服务端连接失败: " + e.getMessage());
                         connectionResult = ConnectionResult.FAILURE;
                     }
                     try {
                         Thread.sleep(1000); // 等待一秒
                     } catch (InterruptedException e) {
                         Thread.currentThread().interrupt(); // 重新设置中断状态
                         connectionResult = ConnectionResult.INTERRUPTED;
                     }
                 }
             } catch (Exception e) {
                 System.err.println("配置错误: " + e.getMessage());
             }
             connectionResult = ConnectionResult.FAILURE;
         }

    }


}

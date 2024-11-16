package lib;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import conf.publicConf;
import redis.clients.jedis.Jedis;

//队列数据处理
public class WebSocketThread extends Thread{
	private static final Log log = LogFactory.getLog(WebSocketThread.class);
	private MyWebSocket websocket;
	private Map<String,Jedis> jedisResourse = new HashMap<String,Jedis>();
	private Integer pos = 0;
	
	public WebSocketThread(){
		for(int i =0;i<10;i++){
    		this.jedisResourse.put("jedis"+i,helperRedis.getJedis());
    	}
	}
	
	 private Jedis getJedisResourse(){
    	Jedis jedis = null;
    	synchronized(this.pos){
           if(this.pos >= this.jedisResourse.size()){
        	   this.pos = 0;
           }
           jedis = this.jedisResourse.get("jedis"+this.pos);
           //轮询
           this.pos++;
        }
    	return jedis;
    }
	
	@Override
	public void run() {
		try{
			int port = publicConf.ws_port;
			this.websocket = new MyWebSocket(new InetSocketAddress(port));
			this.websocket.start();
			Thread.sleep(1000);
			//消息通知
			Thread messagePushThr = new Thread(new MessagePushThread(this.websocket,this.getJedisResourse()));
			messagePushThr.setName("messagePushThr");
			messagePushThr.start();
		}catch(Exception e){
			log.error("websocket failure" + e.getMessage());
		}
	}
	
}
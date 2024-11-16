package test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import conf.mysqlConf;
import conf.redisConf;
import lib.JdbcUtilsPool;
import lib.helperRedis;
import lib.yuncorelot.Message;
import redis.clients.jedis.Jedis;

//队列数据处理
public class TestHandleThread extends Thread{
	private static final Log log = LogFactory.getLog(TestHandleThread.class);
	
	private Jedis jedis = null;
	private HandleData handleData;
	private String str;
	private Message message;
	private String mac;
	
	private static final int NUM_THREADS = 4; // 线程池大小
	private static final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
	private static final int MAX_HASH = Integer.MAX_VALUE; // 哈希值的最大范围  
	
	public TestHandleThread(){
		this.jedis=helperRedis.getJedis();
		this.handleData = new HandleData();
	}
	
	@Override
	public void run() {
		while(true){
			try{
				this.str = jedis.rpop(redisConf.DEVICE_QUEUE_DATA);
				if(str == null || str == ""){
					Thread.sleep(1000);
				}else{
					this.message = JSON.parseObject(str, new TypeReference<Message>(){});
					this.mac = message.getMac();
					processData(this.mac,this.message);
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}
		}  	        
	}
	
	//获取hash值
	private int getHash(String str){
		final int p = 16777619;  
        int hash = (int) 2166136261L;  
        for (int i = 0; i < str.length(); i++)  
            hash = (hash ^ str.charAt(i)) * p;  
        hash += hash << 13;  
        hash ^= hash >> 7;  
        hash += hash << 3;  
        hash ^= hash >> 17;  
        hash += hash << 5;  
  
        // 如果算出来的值为负数则取其绝对值  
        if (hash < 0)  
            hash = Math.abs(hash);  
        return hash;
	}
	
	// 一致性哈希函数 
    private int consistentHash(String mac){
    	return (int) this.getHash(mac) % NUM_THREADS;
    }
    
    public void processData(String mac, Message data) {
    	List<String> macs = this.generateMacs(100);
    	for(String tmp:macs){
    		System.out.println(tmp);
    	}
    	
//        int threadIndex = consistentHash(mac);  
//        executor.submit(() -> {  
//            // 这里是线程要执行的任务，处理与MAC地址相关的数据 
//        	log.debug("Thread " + threadIndex + " is processing MAC " + mac);
//            //this.handleData.handle(data);  
//        });  
    }
    
    private List<String> generateMacs(int num){
    	String mac = "AC:AC:AC:AC:AC:AC";
    	List<String> macs = new ArrayList<String>();
    	for(int i=0;i<num;i++){
    		macs.add(mac);
    		mac = this.getNextMac(mac);
    	}
    	return macs;
    }
    
    private String getNextMac(String mac){
    	String res = "";
    	return res;
    }
}
package lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import lib.ssl.SSLUtil;

//http处理请求
public class HttpRequest {
	private static final Log log = LogFactory.getLog(HttpRequest.class);
	 /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
        	if(url.substring(0,5).equals("https")){
    			HttpRequest.trustAllHttpsCertificates();
    		}
            String urlNameString = (param != null) ? url + "?" + param : url;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            /*connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");*/
            // 建立实际的连接
            connection.connect();
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {            
            log.error("发送GET请求出现异常 "+e.getMessage());
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
            	log.error("发送GET请求出现异常2 "+e2.getMessage());
            }
        }
        return result;
    }
    
    private static void trustAllHttpsCertificates()throws Exception{
    	try{
	    	SSLContext sslContext = new SSLUtil().setTrustAll(true).createContext();
	    	HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    	}catch(Exception e){
    		throw e;
    	}
    }
    
    public static String sendPostJson(String url,String jsonStr){
    	String result = "";
    	try{
    		if(url.substring(0,5).equals("https")){
    			HttpRequest.trustAllHttpsCertificates();
    		}
    		//创建资源
    		URL realUrl = new URL(url);
    		//建立连接
    		HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
    		// 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection","Keep-Alive");
            conn.setRequestProperty("Charset","UTF-8");
            //转换为字节数组
            byte[] data = jsonStr.getBytes();
            //设置文件长度
            conn.setRequestProperty("Content-Length",String.valueOf(data.length));
            //设置文件类型
            conn.setRequestProperty("Content-Type","application/json");
            //开始连接请求
            conn.connect();
            OutputStream out = conn.getOutputStream();
            //写入请求字符串
            out.write(data);
            out.flush();
            out.close();
            //请求返回状态
            if(conn.getResponseCode() == 200){
            	InputStream in = conn.getInputStream();
            	try{
            		byte[] data1 = new byte[in.available()];
            		in.read(data1);
            		//转成字符串
            		result = new String(data1);
            	}catch(Exception e){
            		log.error("Send post failure:"+e.getMessage());
            	}
            }
    	}catch(Exception e){
    		log.error("Send post exception:"+e.getMessage());
    	}
    	return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
        	if(url.substring(0,5).equals("https")){
    			HttpRequest.trustAllHttpsCertificates();
    		}
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
        	log.error("发送POST请求出现异常 "+e.getMessage());
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
            	log.error("发送POST请求出现异常2 "+ex.getMessage());
            }
        }
        return result;
    }    
    
    public static String sendGet_base(String url) {
        String result = "";
        BufferedReader in = null;
        try {
        	if(url.substring(0,5).equals("https")){
    			HttpRequest.trustAllHttpsCertificates();
    		}
            String urlNameString = url ;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("Authorization",
            		"Basic " + Base64.getUrlEncoder().encodeToString(( "admin:public").getBytes()));
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {            
            log.error("发送GET请求出现异常 "+e.getMessage());
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
            	log.error("发送GET请求出现异常2 "+e2.getMessage());
            }
        }
        return result;
    }
}
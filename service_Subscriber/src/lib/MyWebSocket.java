package lib;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import data.Device;

public class MyWebSocket extends WebSocketServer{
	private static final Log log = LogFactory.getLog(MyWebSocket.class);
	
	private static List<String> methods;
	static{
		methods = new ArrayList<String>();
		methods.add("group");
		methods.add("notice");
	}
	
	//保存项目ID对应每个客户端的连接
	private static Map<String,List<WebSocket>>conns = new HashMap<String,List<WebSocket>>();
	
	public static Map<String,String> hashPrtid;
	static{
		hashPrtid = new HashMap<String,String>();
		hashPrtid.put("salt","product");
		hashPrtid.put("length","22");
		hashPrtid.put("header","prt");
		hashPrtid.put("alphabet","abcdefghijklmnopqrstuvwxyz");
	}
	
	public MyWebSocket(int port) throws UnknownHostException{
		super(new InetSocketAddress(port));
	}

	public MyWebSocket(InetSocketAddress address){
		super(address);
	}

	public MyWebSocket(int port,Draft_6455 draft){
		super(new InetSocketAddress(port),Collections.<Draft>singletonList(draft));
	}

	@Override
	public void onOpen(WebSocket conn,ClientHandshake handshake){
		String[] descriptor = conn.getResourceDescriptor().split("/");
		if(descriptor.length != 3){
			conn.send("{\"code\":\""+MyException.PARAMS_REQUIRED+"\",\"message\":\"params is error\"}");
			conn.close();
		}
		String method = descriptor[1];
		String euid = descriptor[2];
		if(!methods.contains(method)){
			conn.send("{\"code\":\""+MyException.HTTP_REQUEST_NO_EXISTS+"\",\"message\":\"request is no exists\"}");
			conn.close();
		}
		String uid = this.getUid(euid);
	
		if(uid.isEmpty()){
			conn.send("{\"code\":\""+MyException.PARAMS_REQUIRED+"\",\"message\":\"Must be user id for the params\"}");
			conn.close();
		}else{
			if(!this.auth(uid,method,conn)){
				conn.send("{\"code\":\""+MyException.USER_NO_EXISTS+"\",\"message\":\"The user is no exists\"}");
				conn.close();
			}
			conn.send("{\"code\":\""+MyException.SUCCESS+"\",\"data\":[]}");
		}
	}

	@Override
	public void onClose(WebSocket conn,int code,String reason,boolean remote){
		String[] descriptor = conn.getResourceDescriptor().split("/");
		if(descriptor.length == 3){
			String method = descriptor[1];
			String euid = descriptor[2];
			if(methods.contains(method)){
				String uid = this.getUid(euid);
				this.removeConn(this.getIdentify(method, uid),conn);
			}
		}
	}

	@Override
	public void onMessage(WebSocket conn,String message){
		
	}

	@Override
	public void onMessage(WebSocket conn,ByteBuffer message){
		
	}

	@Override
	public void onError(WebSocket conn,Exception ex){
		log.error("ws error:" +ex.getClass().toString() + ex.getMessage());
		if(conn != null){
			String[] descriptor = conn.getResourceDescriptor().split("/");
			if(descriptor.length == 3){
				String method = descriptor[1];
				String euid = descriptor[2];
				if(methods.contains(method)){
					String uid = this.getUid(euid);
					this.removeConn(this.getIdentify(method, uid),conn);
				}
			}
		}
	}

	@Override
	public void onStart(){
		log.fatal("websocket server started on port:" + this.getPort());
	}
	
	public void sendToUser(String userID,String method,String message){
		List<WebSocket> userConns = conns.get(this.getIdentify(method, userID));
		if(userConns != null && !userConns.isEmpty()){
			for(int i=0;i<userConns.size();i++){
				WebSocket userConn = userConns.get(i);
				if(userConn != null && userConn.isOpen()){
					log.debug("Push message["+message+"] for "+method+" to the user["+userID+"]by conn["+userConn+"]");
					userConn.send(message);
				}
			}
		}
	}
	
	private Boolean auth (String uid,String method,WebSocket conn){
		Boolean res = false;
		String sql_find = Device.sql_user_get;
		List<Object> paramsQuery = new ArrayList<Object>();
		paramsQuery.add(uid);
		JdbcUtils jdbcUtils = new JdbcUtils();
		jdbcUtils.getConnection();
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			result = jdbcUtils.findSimpleResult(sql_find, paramsQuery);
			if(!result.isEmpty()){
				this.addConn(this.getIdentify(method, uid),conn);
				res = true;
			}
		}catch(Exception e){
			log.error(e.getMessage());
		}
		jdbcUtils.releaseConn();
		return res;
	}
	
	private void addConn(String identify,WebSocket conn){
		if(conn != null && conn.isOpen()){
			List<WebSocket> userConns = conns.get(identify);
			if(userConns == null){
				List<WebSocket> tmp = new ArrayList<WebSocket>();
				tmp.add(conn);
				conns.put(identify, tmp);
			}else{
				userConns.add(conn);
				conns.put(identify, userConns);
			}
		}
		log.debug("websocket addConn["+conn+"] conns["+conns.toString()+"]");
	}
	
	private void removeConn(String identify,WebSocket conn){
		if(!identify.isEmpty()){
			List<WebSocket> userConns = conns.get(identify);
			if(userConns != null && !userConns.isEmpty()){
				userConns.remove(conn);
				if(userConns.isEmpty()){
					conns.remove(identify);
				}
				if(conn != null && conn.isOpen())
					conn.close();
				log.debug("websocket removeConn["+conn+"] conns["+conns.toString()+"]");
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void addConns(String identify,List<WebSocket> conn){
		List<WebSocket> userConns = conns.get(identify);
		if(userConns == null){
			conns.put(identify, conn);
		}else{
			userConns.addAll(conn);
			conns.put(identify, userConns);
		}
	}
	
	@SuppressWarnings("unused")
	private void removeConns(String identify){
		if(!identify.isEmpty()){
			List<WebSocket> userConns = conns.get(identify);
			if(!userConns.isEmpty()){
				for(int i=0;i<userConns.size();i++){
					WebSocket userConn = userConns.get(i);
					if(userConn != null && userConn.isOpen()){
						userConn.close();
					}
				}
			}
			conns.remove(identify);
		}
	}
	
	private String getUid(String euid){
		String uid = "";
		if(!euid.isEmpty()){
			uid = Helper.decodeUid(euid);
		}
		return uid;
	}
	
	private String getIdentify(String method,String uid){
		return method + "-"+uid;
	}
	
	private String getUidFromIdentify(String identify){
		String uid = "";
		String[] identifies = identify.split("-");
		if(identifies.length == 2){
			uid = identifies[1];
		}
		return uid;
	}
	
	public List<String> getUidsForNoticeFromConns(){
		List<String> results = new ArrayList<String>();
		Iterator<Map.Entry<String,List<WebSocket>>> entries = conns.entrySet().iterator();
		while(entries.hasNext()){
			Map.Entry<String,List<WebSocket>> entry = entries.next();
			String identify = entry.getKey();
			String uid = this.getUidFromIdentify(identify);
			if(!uid.isEmpty()){
				results.add(this.getUidFromIdentify(identify));
			}
		}
		return results;
	}
}

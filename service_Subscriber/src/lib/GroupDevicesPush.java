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

public class GroupDevicesPush extends WebSocketServer{
	private static final Log log = LogFactory.getLog(GroupDevicesPush.class);
	
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
	
	public GroupDevicesPush(int port) throws UnknownHostException{
		super(new InetSocketAddress(port));
	}

	public GroupDevicesPush(InetSocketAddress address){
		super(address);
	}

	public GroupDevicesPush(int port,Draft_6455 draft){
		super(new InetSocketAddress(port),Collections.<Draft>singletonList(draft));
	}

	@Override
	public void onOpen(WebSocket conn,ClientHandshake handshake){
		String uid = this.getUidFromConn(conn);
		if(uid.isEmpty()){
			conn.send("{\"code\":\""+MyException.PARAMS_REQUIRED+"\",\"message\":\"Must be user id for the params\"}");
			conn.close();
		}else{
			if(!this.auth(uid,conn)){
				conn.send("{\"code\":\""+MyException.USER_NO_EXISTS+"\",\"message\":\"The user is no exists\"}");
				conn.close();
			}
			conn.send("{\"code\":\""+MyException.SUCCESS+"\",\"data\":[]}");
		}
	}

	@Override
	public void onClose(WebSocket conn,int code,String reason,boolean remote){
		String uid = this.getUidFromConn(conn);
		this.removeConn(uid,conn);
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
			String uid = this.getUidFromConn(conn);
			this.removeConn(uid,conn);
		}
	}

	@Override
	public void onStart(){
		log.fatal("GroupDevices push server started on port:" + this.getPort());
	}
	
	public void sendToUsers(Map<String,Map<String,String>> userIDs){
		log.debug("websocket push to users["+userIDs.toString()+"]");
		log.debug("websocket push conns["+conns.toString()+"]");
		Iterator<Map.Entry<String,Map<String,String>>> entries = userIDs.entrySet().iterator();
		while(entries.hasNext()){
			Map.Entry<String,Map<String,String>> entry = entries.next();
			this.sendToUser(entry.getKey(),entry.getValue());
		}
	}
	
	public void sendToUser(String userID,Map<String,String>groupNums){
		List<WebSocket> userConns = conns.get(userID);
		if(userConns != null && !userConns.isEmpty()){
			for(int i=0;i<userConns.size();i++){
				WebSocket userConn = userConns.get(i);
				if(userConn != null && userConn.isOpen()){
					String message = "";
					Iterator<Map.Entry<String,String>> entries = groupNums.entrySet().iterator();
					while(entries.hasNext()){
						Map.Entry<String, String> entry = entries.next();
						message += "{\"gid\":\""+Helper.hashid.encode(Long.parseLong(entry.getKey()))+"\",\"device_nums\":\""+entry.getValue()+"\"},";
					}
					if(!message.isEmpty()){
						message = "["+message.substring(0,message.length()-1)+"]";
						log.debug("Push device_nums with message["+message+"] to the user["+userID+"]by conn["+userConn+"]");
						userConn.send(message);
					}
				}
			}
		}
	}
	
	private Boolean auth (String uid,WebSocket conn){
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
				this.addConn(uid,conn);
				res = true;
			}
		}catch(Exception e){
			log.error(e.getMessage());
		}
		jdbcUtils.releaseConn();
		return res;
	}
	
	private void addConn(String uid,WebSocket conn){
		if(conn != null && conn.isOpen()){
			List<WebSocket> userConns = conns.get(uid);
			if(userConns == null){
				List<WebSocket> tmp = new ArrayList<WebSocket>();
				tmp.add(conn);
				conns.put(uid, tmp);
			}else{
				userConns.add(conn);
				conns.put(uid, userConns);
			}
		}
		log.debug("websocket addConn conns["+conns.toString()+"]");
	}
	
	private void removeConn(String uid,WebSocket conn){
		if(!uid.isEmpty()){
			List<WebSocket> userConns = conns.get(uid);
			if(userConns != null && !userConns.isEmpty()){
				userConns.remove(conn);
				if(userConns.isEmpty()){
					conns.remove(uid);
				}
				if(conn != null && conn.isOpen())
					conn.close();
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void addConns(String uid,List<WebSocket> conn){
		List<WebSocket> userConns = conns.get(uid);
		if(userConns == null){
			conns.put(uid, conn);
		}else{
			userConns.addAll(conn);
			conns.put(uid, userConns);
		}
	}
	
	@SuppressWarnings("unused")
	private void removeConns(String uid){
		if(!uid.isEmpty()){
			List<WebSocket> userConns = conns.get(uid);
			if(!userConns.isEmpty()){
				for(int i=0;i<userConns.size();i++){
					WebSocket userConn = userConns.get(i);
					if(userConn != null && userConn.isOpen()){
						userConn.close();
					}
				}
			}
			conns.remove(uid);
		}
	}
	
	private String getUidFromConn(WebSocket conn){
		String uid = "";
		String euid = conn.getResourceDescriptor().substring(1);
		if(!euid.isEmpty()){
			uid = Helper.decodeUid(euid);
		}
		return uid;
	}
}

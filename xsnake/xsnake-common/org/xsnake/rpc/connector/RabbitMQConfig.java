package org.xsnake.rpc.connector;

public class RabbitMQConfig {

	private String host;
	private int port;
	private String username;
	private String password;
	
	protected RabbitMQConfig(String type,String host,String port,String username,String password) throws Exception{
		this.host = host;
		try{
			this.port = Integer.parseInt(port);
		}catch(Exception e){
			throw new Exception("无法连接到服务器");
		}
		this.username = username;
		this.password = password;
	}

	public static RabbitMQConfig getConfigure(String url) throws Exception {
		String[] typeAndConnectionString = getTypeAndConnectionString(url);
		String type = typeAndConnectionString[0];
		String connectionString = typeAndConnectionString[1];
		String[] addressAndUserInfo = getAddressAndUserInfo(connectionString);
		String address = addressAndUserInfo[0];
		String userInfo = addressAndUserInfo[1];
		String[] hostAndPort = getHostAndPort(address);
		String host = hostAndPort[0];
		String port = hostAndPort[1];
		String[] usernameAndPassword = getUsernameAndPassword(userInfo);
		String username = null;
		String password = null;
		for(String info : usernameAndPassword){
			String[] s = info.split("=");
			if("username".equalsIgnoreCase(s[0])){
				if(s.length > 1){
					username = s[1];
				}else{
					username = "";
				}
			}
			
			else if("password".equalsIgnoreCase(s[0])){
				if(s.length > 1){
					password = s[1];
				}else{
					password = "";
				}
			}
		}
		return new RabbitMQConfig(type,host,port,username,password);
	}
	
	
	private static String[] getTypeAndConnectionString(String url) throws Exception{
		String[] s = url.split("://");
		if(s.length != 2){
			throw new Exception("无法连接到服务器，连接地址有误");
		}
		return s;
	}
	
	private static String[] getAddressAndUserInfo(String connectionString) throws Exception{
		String[] s = connectionString.split("\\?");
		if(s.length != 2){
			throw new Exception("无法连接到服务器，连接地址有误");
		}
		return s;
	}
	
	private static String[] getHostAndPort(String address)throws Exception{
		String[] s = address.split(":");
		if(s.length != 2){
			throw new Exception("无法连接到服务器，连接地址有误");
		}
		return s;
	}
	
	private static String[] getUsernameAndPassword(String userInfo)throws Exception{
		String[] s = userInfo.split("&");
		if(s.length != 2){
			throw new Exception("无法连接到服务器，连接地址有误");
		}
		return s;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}

}

package org.xsnake.remote.server;

import java.io.Serializable;
import java.util.List;

import javax.sql.DataSource;

public class XSnakeContext implements Serializable{

	private static final long serialVersionUID = 1L;

	//配置项
	protected String serverId;
	
	protected String host; //RMI的地址

	protected int port = 0; //RMI端口号
	
	protected int timeout = 10; //连接zooKeeper的超时时间
	
	protected String zookeeperAddress;
	
	protected List<String> trustAddress; //信任的IP地址过滤列表
	
	protected List<String> interceptors; //拦截器

	protected String authenticationInterface; //身份验证接口
	
	protected DataSource dataSource; //日志数据源

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
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

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getZookeeperAddress() {
		return zookeeperAddress;
	}

	public void setZookeeperAddress(String zookeeperAddress) {
		this.zookeeperAddress = zookeeperAddress;
	}

	public List<String> getTrustAddress() {
		return trustAddress;
	}

	public void setTrustAddress(List<String> trustAddress) {
		this.trustAddress = trustAddress;
	}

	public List<String> getInterceptors() {
		return interceptors;
	}

	public void setInterceptors(List<String> interceptors) {
		this.interceptors = interceptors;
	}

	public String getAuthenticationInterface() {
		return authenticationInterface;
	}

	public void setAuthenticationInterface(String authenticationInterface) {
		this.authenticationInterface = authenticationInterface;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
}

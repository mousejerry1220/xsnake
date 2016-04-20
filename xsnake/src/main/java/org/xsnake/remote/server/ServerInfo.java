package org.xsnake.remote.server;

import java.util.Date;

public class ServerInfo {
	
	protected String serverId ; // 服务器ID
	
	protected String host ; //服务器地址
	
	protected int port;  //服务器端口
	
	protected Date startupDate; //服务器运行开始时间
	
	protected long startupUseTime; //服务器启动耗时

	public String getServerId() {
		return serverId;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public Date getStartupDate() {
		return startupDate;
	}

	public long getStartupUseTime() {
		return startupUseTime;
	}
	
}

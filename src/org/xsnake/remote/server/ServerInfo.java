package org.xsnake.remote.server;

import java.util.Date;

public class ServerInfo {
	
	protected String serverId ;
	
	protected String host ;
	
	protected int port;
	
	protected Date startupDate;
	
	protected long startupUseTime;

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

package org.xsnake.logs.mysql;

import java.net.Socket;
import java.util.Date;

import org.xsnake.remote.server.ServerInfo;
import org.xsnake.remote.server.XSnakeContext;

public class MysqlNotTrustAddressLogs extends MysqlRejectLogs{
	
	public MysqlNotTrustAddressLogs(Socket socket) {
		super(socket);
	}

	Object[] getArgs() {
		ServerInfo serverInfo = XSnakeContext.getServerInfo();
		return new Object[]{serverInfo.getServerId(),serverInfo.getHost(),serverInfo.getPort(),
				"NotTrustAddress",socket.getInetAddress().getHostAddress(),socket.getPort(),new Date()};
	}

}

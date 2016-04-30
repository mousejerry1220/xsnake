package org.xsnake.logs.mysql;

import java.net.Socket;

import org.xsnake.logs.XSnakeLogsInterface;
import org.xsnake.remote.server.InvokeInfo;
import org.xsnake.remote.server.ServerInfo;


//默认日志，mysql实现
public class XSnakeLogsImpl implements XSnakeLogsInterface{

	public void log4InvokeMethod(ServerInfo info, InvokeInfo invokeInfo) {
		new InvokeMethodLogs(info,invokeInfo).log(); 
	}

	public void log4ServerStartup(ServerInfo info) {
		
	}

	public void log4XSnakeException(ServerInfo info, Throwable e) {
		
	}

	public void log4ServerRestart(ServerInfo info) {
		
	}

	public void log4AuthenticationFailed(ServerInfo info, Socket s) {
		
	}

	public void log4NotTrustAddress(ServerInfo info, Socket s) {
		
	}
	
}

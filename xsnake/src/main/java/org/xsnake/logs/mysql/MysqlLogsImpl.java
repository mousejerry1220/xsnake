package org.xsnake.logs.mysql;

import java.net.Socket;

import org.xsnake.logs.XSnakeLogsInterface;
import org.xsnake.remote.server.InvokeInfo;


//默认日志，mysql实现
public class MysqlLogsImpl implements XSnakeLogsInterface{

	public void log4InvokeMethod(InvokeInfo invokeInfo) {
		new MysqlInvokeMethodLogs(invokeInfo).log(); 
	}

	public void log4ServerStartup() {
		new MysqlServerStartupLogs().log();
	}

	public void log4XSnakeException(Throwable e) {
		new MysqlXSnakeExceptionLogs(e).log();
	}

	public void log4ServerRestart() {
		
	}

	public void log4AuthenticationFailed(Socket s) {
		
	}

	public void log4NotTrustAddress(Socket s) {
		
	}
	
}

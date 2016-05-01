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

	public void log4XSnakeException(InvokeInfo invokeInfo,Throwable e) {
		new MysqlXSnakeExceptionLogs(invokeInfo,e).log();
	}

	public void log4ServerRestart() {
		
	}

	public void log4AuthenticationFailed(Socket s) {
		new MysqlAuthenticationFailedLogs(s).log();
	}

	public void log4NotTrustAddress(Socket s) {
		new MysqlNotTrustAddressLogs(s).log();
	}
	
}

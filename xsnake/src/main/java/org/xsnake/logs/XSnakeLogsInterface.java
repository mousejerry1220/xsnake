package org.xsnake.logs;

import java.net.Socket;

import org.xsnake.remote.server.InvokeInfo;
import org.xsnake.remote.server.ServerInfo;

public interface XSnakeLogsInterface {
	//当方法被调用
	void log4InvokeMethod(ServerInfo info, InvokeInfo invokeInfo);
	//当服务启动完成
	void log4ServerStartup(ServerInfo info);
	//当服务层抛出异常
	void log4XSnakeException(ServerInfo info,Throwable e);
	//当服务器重启
	void log4ServerRestart(ServerInfo info);
	//当RMI身份验证失败
	void log4AuthenticationFailed(ServerInfo info,Socket s);
	//当非白名单地址链接
	void log4NotTrustAddress(ServerInfo info,Socket s);
}

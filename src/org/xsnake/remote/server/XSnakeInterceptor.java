package org.xsnake.remote.server;

public interface XSnakeInterceptor {

	void before(ServerInfo serverInfo,InvokeInfo invokeInfo);
	
	void after(ServerInfo serverInfo,InvokeInfo invokeInfo);
	
}

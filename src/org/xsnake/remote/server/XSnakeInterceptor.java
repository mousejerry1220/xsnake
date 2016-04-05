package org.xsnake.remote.server;

import java.lang.reflect.Method;

public interface XSnakeInterceptor {

	void before(ServerInfo serverInfo,Object target, Method method, Object[] args);
	
	void after(ServerInfo serverInfo,Object target, Method method, Object[] args,Object result);
	
}

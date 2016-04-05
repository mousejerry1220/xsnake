package org.xsnake.remote.server;

import java.lang.reflect.Method;

public interface XSnakeInterceptor {

	void before(String serverId,Object target, Method method, Object[] args);
	
	void after(String serverId,Object target, Method method, Object[] args,Object result);
	
}

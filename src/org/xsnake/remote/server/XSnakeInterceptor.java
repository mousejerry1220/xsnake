package org.xsnake.remote.server;

import java.lang.reflect.Method;

public interface XSnakeInterceptor {

	void before(Object target, Method method, Object[] args);
	
	void after(Object target, Method method, Object[] args,Object result);
	
}

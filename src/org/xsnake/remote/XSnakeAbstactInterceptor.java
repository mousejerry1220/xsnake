package org.xsnake.remote;

import java.lang.reflect.Method;

public class XSnakeAbstactInterceptor implements XSnakeInterceptor {

	@Override
	public void after(Object target, Method method, Object[] args,Object result) {
		//to do nothing 
	}
	
	@Override
	public void before(Object target, Method method, Object[] args) {
		//to do nothing 
	}
	
}

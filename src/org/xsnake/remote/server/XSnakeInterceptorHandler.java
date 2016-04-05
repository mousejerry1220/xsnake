package org.xsnake.remote.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class XSnakeInterceptorHandler implements InvocationHandler {

	List<XSnakeInterceptor> interceptorList = null;
	
	Object targetObject;
	
	String serverId;
	
	protected XSnakeInterceptorHandler(String serverId,List<XSnakeInterceptor> interceptorList){
		this.interceptorList = interceptorList;
		this.serverId = serverId;
	}

	public Object createProxy(Object targetObject) {
		this.targetObject = targetObject;
		return Proxy.newProxyInstance(targetObject.getClass().getClassLoader(),
				targetObject.getClass().getInterfaces(), this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)throws Throwable {
		
		if(interceptorList !=null){
			for(XSnakeInterceptor interceptor : interceptorList){
				interceptor.before(serverId,targetObject, method, args);
			}
		}
		
		Object result = method.invoke(targetObject, args);
		
		if(interceptorList !=null){
			for(XSnakeInterceptor interceptor : interceptorList){
				interceptor.after(serverId,targetObject, method, args,result);
			}
		}
		
		return result;
	}

}

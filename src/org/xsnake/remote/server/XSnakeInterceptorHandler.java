package org.xsnake.remote.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class XSnakeInterceptorHandler implements InvocationHandler {

	List<XSnakeInterceptor> interceptorList = null;
	
	Object targetObject;

	ServerInfo info;
	
	protected XSnakeInterceptorHandler(ServerInfo info,List<XSnakeInterceptor> interceptorList){
		this.interceptorList = interceptorList;
		this.info = info;
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
				interceptor.before(info,new InvokeInfo (targetObject, method, args,null,-1));
			}
		}
		long start = System.currentTimeMillis();
		Object result = method.invoke(targetObject, args);
		long useTime = System.currentTimeMillis() - start;
		
		if(interceptorList !=null){
			for(XSnakeInterceptor interceptor : interceptorList){
				interceptor.after(info,new InvokeInfo(targetObject, method, args,result,useTime));
			}
		}
		return result;
	}

}

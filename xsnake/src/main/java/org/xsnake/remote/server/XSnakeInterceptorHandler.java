package org.xsnake.remote.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class XSnakeInterceptorHandler implements InvocationHandler {
	
	Object targetObject;

	public Object createProxy(Object targetObject) {
		this.targetObject = targetObject;
		return Proxy.newProxyInstance(targetObject.getClass().getClassLoader(), targetObject.getClass().getInterfaces(), this);
	}

	public Object invoke(Object proxy, Method method, Object[] args)throws Throwable {
		
		Object result = null;
		List<XSnakeInterceptor> interceptorList = XSnakeContext.getInterceptorList();
		if(interceptorList !=null){
			for(XSnakeInterceptor interceptor : interceptorList){
				interceptor.before(XSnakeContext.getServerInfo(),new InvokeInfo (targetObject, method, args,null,-1));
			}
		}
		long start = System.currentTimeMillis();
		result = method.invoke(targetObject, args);
		long useTime = System.currentTimeMillis() - start;
		InvokeInfo invokeInfo = new InvokeInfo(targetObject, method, args,result,useTime);
		if(interceptorList !=null){
			for(XSnakeInterceptor interceptor : interceptorList){
				interceptor.after(XSnakeContext.getServerInfo(),invokeInfo);
			}
		}
		if(XSnakeContext.getLogger() !=null){
			XSnakeContext.getLogger().log4InvokeMethod(invokeInfo);
		}
		return result;
	}

}

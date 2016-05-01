package org.xsnake.remote.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.xsnake.remote.XSnakeException;

public class XSnakeInterceptorHandler implements InvocationHandler {
	
	Object targetObject;

	public Object createProxy(Object targetObject) {
		this.targetObject = targetObject;
		return Proxy.newProxyInstance(targetObject.getClass().getClassLoader(), targetObject.getClass().getInterfaces(), this);
	}

	public Object invoke(Object proxy, Method method, Object[] args)throws Throwable {
		 //服务器启动时候，spring 会调用对象的toString方法，导致会有一次无效的拦截，以及日志记录等
		boolean toStringMethod = "toString".equals(method.getName());
		Object result = null;
		if(!toStringMethod){
			List<XSnakeInterceptor> interceptorList = XSnakeContext.getInterceptorList();
			
			InvokeInfo invokeInfo = new InvokeInfo (targetObject, method, args,null,-1);
			
			if(interceptorList !=null){
				for(XSnakeInterceptor interceptor : interceptorList){
					interceptor.before(XSnakeContext.getServerInfo(),invokeInfo);
				}
			}
			
			long start = System.currentTimeMillis();
			
			try{
				result = method.invoke(targetObject, args);
			}catch(Exception e){
				if( !(e instanceof XSnakeException )){
					if(XSnakeContext.getLogger()!=null){
						XSnakeContext.getLogger().log4XSnakeException(invokeInfo,e);
					}
				}
				throw e;
			}
			
			long useTime = System.currentTimeMillis() - start;
			
			invokeInfo = new InvokeInfo(targetObject, method, args,result,useTime);
			
			if(interceptorList !=null){
				for(XSnakeInterceptor interceptor : interceptorList){
					interceptor.after(XSnakeContext.getServerInfo(),invokeInfo);
				}
			}
			
			if(XSnakeContext.getLogger() !=null){
				XSnakeContext.getLogger().log4InvokeMethod(invokeInfo);
			}
		}else{
			result = method.invoke(targetObject, args);
		}
		return result;
	}

}

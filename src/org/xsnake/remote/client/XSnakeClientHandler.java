package org.xsnake.remote.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.remoting.RemoteConnectFailureException;

public class XSnakeClientHandler implements InvocationHandler {
	
	Object targetObject;
	
	ClientAccessFactory factory =null;
	
	Class<?> interfaceService = null;
	
	int version =0;
	
	public XSnakeClientHandler(ClientAccessFactory factory,Class<?> interfaceService,int version){
		this.factory = factory;
		this.interfaceService = interfaceService;
		this.version = version;
	}

	public Object createProxy(Object targetObject) {
		this.targetObject = targetObject;
		return Proxy.newProxyInstance(targetObject.getClass().getClassLoader(),
				new Class[]{interfaceService}, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)throws Throwable {
		Object result = null;
		try{
			result = method.invoke(targetObject, args);
		}catch(RemoteConnectFailureException | InvocationTargetException e){
			Object obj = factory.getService(interfaceService,version);
			result = method.invoke(obj, args);
		}
		return result;
	}

}

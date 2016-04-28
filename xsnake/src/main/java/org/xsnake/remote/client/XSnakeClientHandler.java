package org.xsnake.remote.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.UnmarshalException;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.remoting.RemoteLookupFailureException;
import org.xsnake.remote.XSnakeException;

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

	public Object invoke(Object proxy, Method method, Object[] args)throws Throwable {
		Object result = null;
		try{
			result = method.invoke(targetObject, args);
		}catch(InvocationTargetException e){
			if(e.getTargetException() instanceof RemoteConnectFailureException && 
					e.getTargetException().getCause() instanceof java.rmi.ConnectIOException){
				throw new XSnakeException("身份验证失败");
			}
			if(e.getTargetException() instanceof UnmarshalException || //执行中断开连接时代码执行至此
					(e.getTargetException() instanceof RemoteConnectFailureException )){	//非执行中断开连接时代码执行至此
				Object obj = null;
				try{
					obj = getService();
				}catch(BeanCreationException ec){
					throw ec ;
				}
				result = method.invoke(obj, args);
				targetObject = obj;
			}else if (e.getTargetException() instanceof InvocationTargetException){
				throw ((InvocationTargetException)e.getTargetException()).getTargetException();
			}else if (e.getTargetException() instanceof UndeclaredThrowableException){
				UndeclaredThrowableException undeclaredThrowable = (UndeclaredThrowableException)e.getTargetException();
				if(undeclaredThrowable.getUndeclaredThrowable() instanceof InvocationTargetException){
					throw ((InvocationTargetException)undeclaredThrowable.getUndeclaredThrowable()).getTargetException();
					//TODO 在这里记录异常，通常为服务端编程人员所为考虑到的异常
				}
				throw undeclaredThrowable.getUndeclaredThrowable();
			}else {
				throw e.getTargetException();
			}
		}
		return result;
	}

	//因为在服务端宕机以后，没有及时清理ZK上的注册信息，有一定概率会又重新获得宕机的主机服务。
	//这时候如果遇见加载远程服务的错误时候，做递归调用知道取到可用对象，或者直到抛出其他问题。
	private Object getService() {
		Object obj = null;
		try{
			obj = factory.getService(interfaceService,version);
		}catch(RemoteLookupFailureException e){
			obj = getService();
		}
		return obj;
	}

}

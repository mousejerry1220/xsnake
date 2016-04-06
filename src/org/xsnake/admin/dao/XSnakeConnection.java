package org.xsnake.admin.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

public class XSnakeConnection implements InvocationHandler {

	//源对象
	Connection connection;
	
	//锁状态
	boolean lock = false;
	
	//代理对象
	Connection _connection = null;
	
	public XSnakeConnection(Connection connection){
		this.connection = connection;
	}
	
	public Connection getConnection() {
		if(_connection == null){
			_connection = (Connection)Proxy.newProxyInstance(connection.getClass().getClassLoader(),
				new Class[]{Connection.class}, this);
		}
		lock = true;
		return _connection;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)throws Throwable {
		//使用连接池，所以调用close方法时候忽略，释放锁
		if("close".equals(method.getName())){
			lock = false;
			System.out.println("--------------------close");
			return null;
		}
		Object result = method.invoke(connection, args);
		return result;
	}

	public boolean isLock() {
		return lock;
	}
	
}

package org.xsnake.admin.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.xsnake.remote.XSnakeException;

public class ConnectionPool {
	
	public List<XSnakeConnection> pool = new ArrayList<XSnakeConnection>();
	
	protected XSnakeAdminConfiguration config;
	
	private static ConnectionPool instance;
	
	public static ConnectionPool getInstance() {
		return instance;
	}

	public void destroy(){
		for(XSnakeConnection connection : pool){
			connection.close();	
		}
		instance = null;
	}
	
	public ConnectionPool(XSnakeAdminConfiguration config) throws ClassNotFoundException {
		if(instance !=null){
			throw new XSnakeException("连接池已经初始化过了");
		}
		this.config = config;
		Class.forName(config.getDriverClassName());
		for(int i=0 ;i<config.getInitSize() ; i++){
			createConnection();
		}
		instance = this;
	}
	
	private synchronized XSnakeConnection createConnection(){
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(config.getUrl(), config.getUsername(),config.getPassword());
		} catch (SQLException e) {
			throw new XSnakeException("数据库连接失败");
		}
		if(connection != null){
			XSnakeConnection _connection = new XSnakeConnection(connection);
			pool.add(_connection);
			return _connection;
		}
		return null;
		
	}
	
	public synchronized Connection getConnection() {
		while(true){
			for(XSnakeConnection connection : pool){
				if(!connection.isLock()){
					return connection.getConnection();
				}
			}
			if(pool.size() < config.maxSize){
				return createConnection().getConnection();
			}
			try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}

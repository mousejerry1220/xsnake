package org.xsnake.admin.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.xsnake.remote.XSnakeException;

public class ConnectionPool {
	
	public List<XSnakeConnection> pool = new ArrayList<XSnakeConnection>();
	
	private XSnakeAdminConfiguration config;
	
	private static ConnectionPool instance;
	
	public static ConnectionPool getInstance() {
		return instance;
	}

	public ConnectionPool(XSnakeAdminConfiguration config) throws ClassNotFoundException, SQLException {
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
	
	private synchronized XSnakeConnection createConnection() throws SQLException{
		Connection connection = DriverManager.getConnection(config.getUrl(), config.getUsername(),config.getPassword());
		XSnakeConnection _connection = new XSnakeConnection(connection);
		pool.add(_connection);
		return _connection;
	}
	
	public synchronized Connection getConnection() throws SQLException{
		while(true){
			for(XSnakeConnection connection : pool){
				if(!connection.isLock()){
					return connection.getConnection();
				}
			}
			
			if(pool.size() < config.maxSize){
				return createConnection().getConnection();
			}
		}
		
	}
	
}

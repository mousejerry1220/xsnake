package org.xsnake.admin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xsnake.remote.XSnakeException;

public class BaseDaoUtil {
	
	public static String getDatabase(){
		String driver = ConnectionPool.getInstance().config.getDriverClassName();
		driver = driver.toLowerCase();
		if(driver.indexOf("mysql")>-1){
			return "mysql";
		}else if(driver.indexOf("oracle") > -1 ){
			return "oracle";
		}
		return null;
	}
	
	public static int executeUpdate(String sql,Object[] args) throws SQLException{
		if (ConnectionPool.getInstance() == null) {
			throw new XSnakeException("连接池未初始化");
		}
		Connection connection = null;
		PreparedStatement statement = null;
		int result = 0;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			statement = connection.prepareStatement(sql);
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					statement.setObject((i + 1), args[i]);
				}
			}
			result = statement.executeUpdate();
		} finally {
			if (connection != null) {
				connection.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return result;
	}
	
	public static List<Map<String, Object>> query(String sql, Object[] args) throws SQLException {
		if (ConnectionPool.getInstance() == null) {
			throw new XSnakeException("连接池未初始化");
		}
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<Map<String, Object>> list = null;
		try {
			connection = ConnectionPool.getInstance().getConnection();
			statement = connection.prepareStatement(sql);
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					statement.setObject((i + 1), args[i]);
				}
			}
			rs = statement.executeQuery();
			list = formatResultSet(rs);
		} finally {
			if (connection != null) {
				connection.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return list;
	}

	
	private static List<Map<String, Object>> formatResultSet(ResultSet rs) throws SQLException {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		ResultSetMetaData rsmd = null;
		int columnCount = 0;
		rsmd = rs.getMetaData();
		columnCount = rsmd.getColumnCount();
		while (rs.next()) {
			Map<String, Object> map = new HashMap<String, Object>();
			for (int i = 1; i <= columnCount; i++) {
				map.put(rsmd.getColumnLabel(i), rs.getObject(i));
			}
			list.add(map);
		}
		return list;
	}

}

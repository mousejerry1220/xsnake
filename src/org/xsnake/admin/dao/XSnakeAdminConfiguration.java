package org.xsnake.admin.dao;

public class XSnakeAdminConfiguration {
	
	int maxSize = 20;
	
	int initSize = 5;
	
	int minSize = 5;

	String driverClassName = "com.mysql.jdbc.Driver";
	
	String url = "jdbc:mysql://127.0.0.1:3306/joker?useUnicode=true&amp;charaterEncoding=utf-8";
	
	String username = "root";
	
	String password = "root";
	
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	public int getInitSize() {
		return initSize;
	}
	public void setInitSize(int initSize) {
		this.initSize = initSize;
	}
	public int getMinSize() {
		return minSize;
	}
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}
	public String getDriverClassName() {
		return driverClassName;
	}
	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}

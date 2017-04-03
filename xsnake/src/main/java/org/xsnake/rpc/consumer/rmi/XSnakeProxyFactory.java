package org.xsnake.rpc.consumer.rmi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.BeanCreationException;
import org.xsnake.rpc.connector.ZooKeeperConnector;
import org.xsnake.rpc.connector.ZooKeeperExpiredCallBack;
import org.xsnake.rpc.connector.ZooKeeperWrapper;

public class XSnakeProxyFactory implements ZooKeeperExpiredCallBack{
	
	String environment;
	
	ZooKeeperWrapper zooKeeper;
	
	Map<String,String> propertyMap;

	List<XSnakeProxyHandler> handlerList = new ArrayList<XSnakeProxyHandler>();
	
	public XSnakeProxyFactory(Map<String,String> propertyMap) throws Exception{
		this.propertyMap = propertyMap;
		init();
	}

	private void init() {
		environment = propertyMap.get("environment");
		String _zooKeeper = propertyMap.get("zooKeeper");
		int timeout = 10;
		try{
			timeout = propertyMap.get("initTimeout") == null ? 10 : Integer.parseInt(propertyMap.get("initTimeout"));
		}catch (NumberFormatException e){
			throw new BeanCreationException("XSnake启动失败，配置参数 initTimeout 错误，必须为数字类型，以秒计算");
		}
		if (StringUtils.isEmpty(_zooKeeper)) {
			throw new BeanCreationException("XSnake启动失败，配置参数 zooKeeper不能为空");
		}
		try {
			zooKeeper = new ZooKeeperConnector(_zooKeeper, timeout,this);
		} catch (Exception e) {
			throw new BeanCreationException("XSnake启动失败，无法连接到ZooKeeper服务器。" + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> interfaceService){
		XSnakeProxyHandler handler = new XSnakeProxyHandler(zooKeeper,interfaceService,propertyMap);
		handlerList.add(handler);
		return (T)handler.createProxy();
	}

	public void destory(){
		if(zooKeeper !=null){
			try {
				zooKeeper.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void callback() {
		destory();
		init();
		for(XSnakeProxyHandler handler : handlerList){
			try {
				handler.initTarget(zooKeeper);
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}

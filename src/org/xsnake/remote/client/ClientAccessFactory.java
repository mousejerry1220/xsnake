package org.xsnake.remote.client;

import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.xsnake.remote.XSnakeException;
import org.xsnake.remote.connector.ZookeeperConnector;

/**
 * 
 * @author Jerry.Zhao
 * 
 * 客户端连接
 *
 */
public class ClientAccessFactory {

	private String zookeeperAddress;
	
	private int timeout = 5; //默认5秒超时
	
	public ClientAccessFactory(){}
	
	public ClientAccessFactory(String zookeeperAddress, int timeout ) {
		this.zookeeperAddress = zookeeperAddress;
		this.timeout = timeout;
	}
	
	public ClientAccessFactory(String zookeeperAddress){
		this.zookeeperAddress = zookeeperAddress;
	}

	/**
	 * 获得远程的服务对象，通过指定接口类型，获取远程最高版本的实现对象
	 * @param interfaceService 接口类型
	 * @return
	 */
	public <T> T getService(Class<T> interfaceService){
		return getService(interfaceService,0);
	}
	
	/**
	 * 获得远程的服务对象，通过指定接口类型，
	 * 获取该接口类型的远程代码实现，如果版本号=0，则默认获取远程最高版本的实现对象
	 * @param interfaceService 接口类型
	 * @param version 指定版本号
	 * @return
	 */
	public String getData(String node,int version) {
		ZookeeperConnector connector = ZookeeperConnector.getConnector(zookeeperAddress,timeout,null);
		try {
			String xsnakeNode = "/xsnake";
			String serviceNode = xsnakeNode +"/service";
			String rootNode = serviceNode+"/"+node;
			String maxVersionNode = rootNode + "/maxVersion";
			String maxVersion = connector.getStringData(maxVersionNode);
			String versionNode = ( version == 0 ? rootNode + "/" + maxVersion : rootNode + "/" + version);
			List<String> list = connector.getZooKeeper().getChildren(versionNode, null);
			if(list.size() == 0){
				return null;
			}
			String path = list.get(RandomUtils.nextInt(list.size()));
			String data = connector.getStringData(versionNode+"/"+path);
			return data;
		} catch (KeeperException | InterruptedException e) {
			e.printStackTrace();
			if( e instanceof KeeperException){
				throw new XSnakeException(" XSnake Client Error ! ");
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> interfaceService,int version){
		String path =  getData(interfaceService.getName(),version);
		if(path == null){
			throw new BeanCreationException("create remote object failed , no server in the service");
		}
		RmiProxyFactoryBean c = new RmiProxyFactoryBean();
		c.setServiceInterface(interfaceService);
		c.setServiceUrl(path);
		c.afterPropertiesSet();
		return (T)c.getObject();
	}

	public String getZookeeperAddress() {
		return zookeeperAddress;
	}
	public void setZookeeperAddress(String zookeeperAddress) {
		this.zookeeperAddress = zookeeperAddress;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
}

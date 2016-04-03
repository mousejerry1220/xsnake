package org.xsnake.remote;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
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
	public <T> T getServiceBean(Class<T> interfaceService){
		return getServiceBean(interfaceService,0);
	}
	
	/**
	 * 获得远程的服务对象，通过指定接口类型，
	 * 获取该接口类型的远程代码实现，如果版本号=0，则默认获取远程最高版本的实现对象
	 * @param interfaceService 接口类型
	 * @param version 指定版本号
	 * @return
	 */
	
	@SuppressWarnings("unchecked")
	public <T> T getServiceBean(Class<T> interfaceService,int version){
		ZookeeperConnector zc = ZookeeperConnector.getConnector(zookeeperAddress,timeout,null);
		String path = zc.getData(interfaceService.getName(),version);
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

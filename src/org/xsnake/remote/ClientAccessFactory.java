package org.xsnake.remote;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.xsnake.remote.connector.ZookeeperConnector;

import xmouse.IRemoteTest;
import xmouse.IWechatService;

public class ClientAccessFactory {
	
	public ClientAccessFactory(String zookeeperAddress,int timeout){
		
		this.zookeeperAddress = zookeeperAddress;
		
	}
	
	private String zookeeperAddress;
	
	private int timeout;

	public <T> T getServiceBean(Class<T> interfaceService){
		return getServiceBean(interfaceService, timeout);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getServiceBean(Class<T> interfaceService,int version){
		
		ZookeeperConnector zc = ZookeeperConnector.getConnector(zookeeperAddress,timeout);
		
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

}

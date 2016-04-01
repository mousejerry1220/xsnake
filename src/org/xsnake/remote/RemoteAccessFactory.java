package org.xsnake.remote;

import java.rmi.RemoteException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.xsnake.common.ReflectionUtil;
import org.xsnake.remote.connector.ZookeeperConnector;

/**
 * 
 * @author Jerry
 *
 *  等待解决的问题。
 *  1、连接多台zookeeper及测试
 *  2、考虑是否需要用自定义spring标签实现
 *  
 */
public class RemoteAccessFactory implements ApplicationContextAware{

	private boolean alwaysCreateRegistry = true;
	private String host;
	private int port = 12345;
	private int timeout = 10;
	private String zookeeper;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)throws BeansException {
		//获取到spring管理的所有bean
		String[] names = applicationContext.getBeanDefinitionNames();
		
		//循环所有的bean 找到符合要求的bean
		for (String name : names) {
			Object obj = applicationContext.getBean(name);
			Object target = ReflectionUtil.getTarget(obj);
			Remote remote = target.getClass().getAnnotation(Remote.class);
			if (remote != null) {
				
				if(remote.type() == Remote.Type.RMI){
					
					Class<?> clazz = exporterService(name, obj, target, remote);
					
					String url = String.format("rmi://%s:%d/%s", host, port, name);
					
					if(zookeeper == null){
						throw new BeanCreationException("zookeeper is null ");
					}
					
					ZookeeperConnector connector = ZookeeperConnector.getConnector(zookeeper,timeout);
					
					try {
						
						connector.publish(clazz.getName(),url,remote.version());
						
					} catch (Exception e) {
						
						e.printStackTrace();
						
						throw new BeanCreationException(e.getMessage());
						
					}
					
				}else{
					
					//暂时只支持RMI方式
				}
				
			}
		}
	}

	private Class<?> exporterService(String name, Object obj, Object target,Remote remote) {
		RmiServiceExporter se = new RmiServiceExporter();
		se.setServiceName(name);
		se.setService(obj);
		se.setAlwaysCreateRegistry(alwaysCreateRegistry);
		alwaysCreateRegistry = false;
		se.setRegistryPort(port);
		Class<?> clazz = remote.serviceInterface();
		if(clazz == Void.class){
			try{
				clazz = target.getClass().getInterfaces()[0];
			}catch(ArrayIndexOutOfBoundsException e){
				throw new BeanCreationException("RMI remote access bean [" + target.getClass().getName() + "] creation failed ! must implements a interface ");
			}
		}
		se.setServiceInterface(clazz);
		try {
			se.afterPropertiesSet();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new BeanCreationException("RMI remote access bean [" + target.getClass().getName() + "] creation failed !" + e.getMessage());
		}
		return clazz;
	}

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public String getZookeeper() {
		return zookeeper;
	}
	public void setZookeeper(String zookeeper) {
		this.zookeeper = zookeeper;
	}

}

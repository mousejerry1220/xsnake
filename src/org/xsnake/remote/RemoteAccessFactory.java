package org.xsnake.remote;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
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
 *  1、考虑是否需要用自定义spring标签实现
 *  
 */
public class RemoteAccessFactory implements ApplicationContextAware{

	private boolean alwaysCreateRegistry = true;
	private String host;
	private int port = 12345;
	private int timeout = 10;
	private String zookeeperAddress;
	
	List<RemoteServiceBean> serviceBeanList = new ArrayList<RemoteServiceBean>();
	
	ZookeeperConnector connector;
	
	public static class RemoteServiceBean{
		public RemoteServiceBean(Class<?> serviceInterface,String url,int version) {
			this.serviceInterface = serviceInterface;
			this.url = url;
			this.version = version;
		}
		Class<?> serviceInterface;
		String url;
		int version;
	}
	
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
					Class<?> clazz = getServiceInterface(target, remote);
					exporterService(name, obj, target, remote, clazz);
					String url = String.format("rmi://%s:%d/%s", host, port, name);
					if(zookeeperAddress == null){
						throw new BeanCreationException("zookeeper is null ");
					}
					try {
						RemoteServiceBean serviceBean = new RemoteServiceBean(clazz,url,remote.version());
						serviceBeanList.add(serviceBean);
					} catch (Exception e) {
						e.printStackTrace();
						throw new BeanCreationException(e.getMessage());
					}
				}else{
					//暂时只支持RMI方式
				}
			}
		}
		
		connector = ZookeeperConnector.getConnector(zookeeperAddress,timeout,new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				if (event.getState() == Event.KeeperState.SyncConnected) {
                   publish();
                }
			}
		});
	}

	public void publish(){
		for(RemoteServiceBean bean : serviceBeanList){
			try {
				connector.publish(bean.serviceInterface.getName(),bean.url,bean.version);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Class<?> getServiceInterface(Object target, Remote remote) {
		Class<?> clazz = remote.serviceInterface();
		if(clazz == Void.class){
			try{
				clazz = target.getClass().getInterfaces()[0];
			}catch(ArrayIndexOutOfBoundsException e){
				throw new BeanCreationException("RMI remote access bean [" + target.getClass().getName() + "] creation failed ! must implements a interface ");
			}
		}
		return clazz;
	}

	private Class<?> exporterService(String name, Object obj, Object target,Remote remote,Class<?> clazz) {
		RmiServiceExporter se = new RmiServiceExporter();
		se.setServiceName(name);
		se.setService(obj);
		se.setAlwaysCreateRegistry(alwaysCreateRegistry);
		alwaysCreateRegistry = false;
		se.setRegistryPort(port);
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
	public String getZookeeperAddress() {
		return zookeeperAddress;
	}
	public void setZookeeperAddress(String zookeeperAddress) {
		this.zookeeperAddress = zookeeperAddress;
	}

}

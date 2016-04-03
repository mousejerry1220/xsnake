package org.xsnake.remote;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.xsnake.common.ReflectionUtil;
import org.xsnake.remote.connector.ZookeeperConnector;

/**
 * 
 * @author Jerry.Zhao
 *
 *  等待解决的问题。
 *  1、考虑是否需要用自定义spring标签实现
 *  2、增加页面管理控制台、默认为随机分配模式，
 *        设置分配模式（权重、随机、顺序），
 *        记录所有服务节点、版本，
 *        通过控制台页面调用方法，
 *        调用历史查看
 *  *3、新增了将服务发布到外网（RMI 内外网的问题）需要配置每个服务器的外网IP
 *  *   需要新增了IP白名单功能。
 *  *   新增了服务端的拦截器链。
 */
public class RemoteAccessFactory implements ApplicationContextAware , Serializable{

	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(RemoteAccessFactory.class) ;
	private boolean alwaysCreateRegistry = true;
	private String host;
	private int port = 12345;
	private int timeout = 10;
	private String zookeeperAddress;
	List<RemoteServiceBean> serviceBeanList = new ArrayList<RemoteServiceBean>();
	ZookeeperConnector connector;
	private List<String> trustAddress; //信任的IP地址过滤列表
	private List<String> interceptors;
	
	
	List<XSnakeInterceptor> interceptorList = new ArrayList<XSnakeInterceptor>();
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)throws BeansException {
		//获取到spring管理的所有bean
		String[] names = applicationContext.getBeanDefinitionNames();

		//循环所有的bean 找到符合要求的bean
		findRemoteService(applicationContext, names);
		
		//初始化拦截器
		initInterceptors();
		
		//导出服务
		exportService();

		//初始化ZooKerper连接器
		initZooKeeper();
	}

	private void initZooKeeper() {
		if(zookeeperAddress !=null){
			connector = ZookeeperConnector.getConnector(zookeeperAddress,timeout,new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected || event.getState() == Event.KeeperState.Expired) {
						publish();
	                }
				}
			});
		}
	}

	private void findRemoteService(ApplicationContext applicationContext, String[] names) {
		for (String name : names) {
			Object obj = applicationContext.getBean(name);
			Object target = ReflectionUtil.getTarget(obj);
			Remote remote = target.getClass().getAnnotation(Remote.class);
			if (remote != null) {
				if(remote.type() == Remote.Type.RMI){
					serviceBeanList.add(RemoteServiceBean.createServiceBean(name,obj,target));
				}else{
					throw new BeanCreationException(" no support ["
							+ remote.type()
							+ "] in the version ! only support RMI type");
				}
			}
		}
	}

	private void initInterceptors() {
		if(interceptors!=null){
			for(String interceptor : interceptors){
				try {
					Object obj = Class.forName(interceptor).newInstance();
					if(obj instanceof XSnakeInterceptor){
						interceptorList.add(((XSnakeInterceptor)obj));
					}else{
						throw new BeanCreationException("拦截器必须实现接口 org.xsnake.remote.XSnakeInterceptor");
					}
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					e.printStackTrace();
					throw new BeanCreationException(e.getMessage());
				}
				
			}
		}
	}

	private void exportService(){
		RMIServerSocketFactory server = new XSnakeSocketFactory(trustAddress);
		RMIClientSocketFactory client = new XSnakeSocketFactory(trustAddress);
		for(RemoteServiceBean bean : serviceBeanList){
			exportService(bean,server,client);
		}
	}
	
	public void publish(){
		LOG.info(" xsnake publish service to zookeeper !");
		for(RemoteServiceBean bean : serviceBeanList){
			try {
				String url = String.format("rmi://%s:%d/%s", host, port, bean.name);
				connector.publish(bean.serviceInterface.getName(),url,bean.version);
				LOG.debug(String.format(" publish [%s] to [%s] version : [%d]",bean.serviceInterface.getName(),url,bean.version));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	private void exportService(RemoteServiceBean bean,RMIServerSocketFactory server,RMIClientSocketFactory client){//String name, Object obj, Object target,Remote remote,Class<?> clazz) {
		RmiServiceExporter se = new RmiServiceExporter();
		se.setServiceName(bean.name);
		Object obj = new XSnakeInterceptorHandler(interceptorList).createProxy(bean.proxy);
		se.setService(obj);
		se.setAlwaysCreateRegistry(alwaysCreateRegistry);
		alwaysCreateRegistry = false;
		se.setRegistryPort(port);
		se.setServiceInterface(bean.serviceInterface);
		se.setClientSocketFactory(client);
		se.setServerSocketFactory(server);
		
		try {
			se.afterPropertiesSet();
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new BeanCreationException("RMI remote access bean [" + bean.target.getClass().getName() + "] creation failed !" + e.getMessage());
		}
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
	public List<String> getTrustAddress() {
		return trustAddress;
	}
	public void setTrustAddress(List<String> trustAddress) {
		this.trustAddress = trustAddress;
	}
	public List<String> getInterceptors() {
		return interceptors;
	}
	public void setInterceptors(List<String> interceptors) {
		this.interceptors = interceptors;
	}

}

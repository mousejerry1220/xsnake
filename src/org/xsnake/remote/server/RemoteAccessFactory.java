package org.xsnake.remote.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.xsnake.common.ReflectionUtil;
import org.xsnake.remote.XSnakeClientSocketFactory;
import org.xsnake.remote.XSnakeRMIAuthentication;
import org.xsnake.remote.XSnakeServerSocketFactory;
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
 *  4、瞅瞅 RMI SOCKET 通讯问题 客户端发送验证信息到服务端
 *  5、考虑控制台单点发布服务，其他节点拷贝并执行
 *  6、缓存问题，ZooKeeper节点变化时候更新本地缓存，否则只拿本地缓存 
 *  7、把IP白名单的设置要放到ZooKeeper上
 */
public class RemoteAccessFactory implements ApplicationContextAware , Serializable{
	static final int DEFAULT_PORT = 1232;
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = LoggerFactory.getLogger(RemoteAccessFactory.class) ;
	private boolean alwaysCreateRegistry = true;
	private String host;
	private int port = 0;
	private int timeout = 10;
	private String zookeeperAddress;
	List<RemoteServiceBean> serviceBeanList = new ArrayList<RemoteServiceBean>();
	ZookeeperConnector connector;
	private List<String> trustAddress; //信任的IP地址过滤列表
	private List<String> interceptors;
	List<XSnakeInterceptor> interceptorList = new ArrayList<XSnakeInterceptor>();
	
	XSnakeRMIAuthentication authentication;
	String authenticationInterface;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)throws BeansException {
		
		//初始化RMI参数
		initRMI();
		
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

	private void initRMI() {
		if(host ==null){
			host = getLocalHost();
		}
		if(port == 0){
			port = getPort(DEFAULT_PORT); 
		}
		System.setProperty("java.rmi.server.hostname", host);
		
		//如果配置了身份验证接口
		if(authenticationInterface !=null){
			try {
				Object obj = Class.forName(authenticationInterface).newInstance();
				if(obj instanceof XSnakeRMIAuthentication){
					authentication = (XSnakeRMIAuthentication)obj;
				}else{
					throw new BeanCreationException("身份验证接口必须实现接口 org.xsnake.remote.XSnakeRMIAuthentication");
				}
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
				throw new BeanCreationException(e.getMessage());
			}
		}
	}

	private void initZooKeeper() {
		if(zookeeperAddress !=null){
			connector = ZookeeperConnector.getConnector(zookeeperAddress,timeout,new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected) {
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
					serviceBeanList.add(RemoteServiceBean.createServiceBean(obj,target));
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
		RMIServerSocketFactory server = new XSnakeServerSocketFactory(trustAddress,authentication);
		RMIClientSocketFactory client = new XSnakeClientSocketFactory();
		for(RemoteServiceBean bean : serviceBeanList){
			exportService(bean,server,client);
		}
	}
	
	public void publish(){
		LOG.info(" xsnake publish service to zookeeper !");
		for(RemoteServiceBean bean : serviceBeanList){
			try {
				String url = String.format("rmi://%s:%d/%s", host, port, bean.name);
				String node = bean.serviceInterface.getName();
				int version = bean.version;
				ZooKeeper zk = connector.getZooKeeper(); // 连接 ZooKeeper 服务器并获取 ZooKeeper 对象
				String xsnakeNode = "/xsnake";
				String serviceNode = xsnakeNode +"/service";
				String rootNode = serviceNode+ "/"+node;
				String versionNode = rootNode + "/"+version;
				String maxVersionNode = rootNode + "/maxVersion";
				String maxVersion = null;
				connector.createDirNode(xsnakeNode,null,CreateMode.PERSISTENT);
				connector.createDirNode(serviceNode,null,CreateMode.PERSISTENT);
				connector.createDirNode(rootNode,null,CreateMode.PERSISTENT);
				connector.createDirNode(versionNode,null,CreateMode.PERSISTENT);
				connector.createDirNode(maxVersionNode,String.valueOf(version).getBytes(),CreateMode.PERSISTENT);
				
				maxVersion = connector.getStringData(maxVersionNode);
				if(Integer.parseInt(maxVersion) < version){
					connector.createNode(maxVersionNode, String.valueOf(version).getBytes(), CreateMode.PERSISTENT);
				}
				zk.create(versionNode + "/"+UUID.randomUUID().toString(), url.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
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
	public String getAuthenticationInterface() {
		return authenticationInterface;
	}
	public void setAuthenticationInterface(String authenticationInterface) {
		this.authenticationInterface = authenticationInterface;
	}

	private String getLocalHost() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new BeanCreationException("创建对象失败，无法自动获取主机地址，请配置");
		}
		
	}
	
	public int getPort(int port) {
		ServerSocket ss = null;
		try{
			 ss = new ServerSocket(port);
		}catch(Exception e){
			port = port + 1;
			return getPort(port);
		}finally{
			try {
				if(ss!=null){
					ss.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return port;
	}

	
}

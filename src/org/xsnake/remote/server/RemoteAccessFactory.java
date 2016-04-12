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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.xsnake.admin.dao.ConnectionPool;
import org.xsnake.admin.dao.XSnakeAdminConfiguration;
import org.xsnake.common.ReflectionUtil;
import org.xsnake.remote.XSnakeClientSocketFactory;
import org.xsnake.remote.XSnakeRMIAuthentication;
import org.xsnake.remote.XSnakeServerSocketFactory;
import org.xsnake.remote.connector.ZookeeperConnector;

import com.google.gson.Gson;

/**
 * 
 * @author Jerry.Zhao
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
 *  5、考虑控制台单点发布服务，其他节点拷贝并执行
 *  6、缓存问题，ZooKeeper节点变化时候更新本地缓存，否则只拿本地缓存 
 *  7、把IP白名单的设置要放到ZooKeeper上
 *  8 控制台对远程服务器的jar包管理，如上传JAR包后，发送指令让远程服务器刷新服务
 *  9 控制台对远程服务器的jar文件浏览，删除
 *  10 扫描时候配合注解，开发人员在注解里添加方法，类的注释，在控制台里可以搜索，降低维护成本
 */
public class RemoteAccessFactory implements ApplicationContextAware , Serializable{
	
	private static RemoteAccessFactory instance = null;
	
	public RemoteAccessFactory(){
		instance = this;
	}
	
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
	
	XSnakeAdminConfiguration adminDatabaseConfig;
	
	private String serverId;
	
	private Date startupDate = null;
	
	private long startupUseTime = -1;
	
	ServerInfo info = new ServerInfo();
	
	List<RmiServiceExporter> rmiServiceExporterList = new ArrayList<RmiServiceExporter>();//存放所有的服务导出对象以便释放资源
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)throws BeansException {
		
		long start = System.currentTimeMillis();
		
		//初始化RMI参数
		initRMI();
		
		//获取到spring管理的所有bean
		String[] names = applicationContext.getBeanDefinitionNames();

		//循环所有的bean 找到符合要求的bean
		findRemoteService(applicationContext, names);
		
		//导出服务
		exportService();

		//初始化ZooKerper连接器
		initZooKeeper();

		//初始化管理配置
		initAdminConfig();
		
		startupUseTime = System.currentTimeMillis() - start;
		startupDate = new Date();
		
		//设置服务信息
		initServerInfo();
		
	}

	private void initAdminConfig() {
		if(adminDatabaseConfig!=null){
			try {
				//单例初始化
				new ConnectionPool(adminDatabaseConfig);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new BeanCreationException(e.getMessage());
			}
		}
	}

	private void initServerInfo() {
		info.serverId = serverId;
		info.host = host;
		info.port = port;
		info.startupDate = new Date(startupDate.getTime());
		info.startupUseTime = startupUseTime;
	}
	
	private void initRMI() {
		//如果没有配置host参数，那么将会使用获取的地址，
		//一般为内网IP地址，所以不配置此项会导致服务在外网无法访问。
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
		
		//如果serverId 为空，设置他的地址+端口为服务器标示
		if(serverId == null){
			serverId = host + ":" + port;
		}
		
		//初始化拦截器
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
			
//			interceptorList.add(null);//添加默认
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

	private void exportService(){
		RMIServerSocketFactory server = new XSnakeServerSocketFactory(trustAddress,authentication);
		RMIClientSocketFactory client = new XSnakeClientSocketFactory();
		for(RemoteServiceBean bean : serviceBeanList){
			exportService(bean,server,client);
		}
	}
	
	public void publish(){
		for(RemoteServiceBean bean : serviceBeanList){
			try {
				String url = String.format("rmi://%s:%d/%s", host, port, bean.name);
				String node = bean.serviceInterface.getName();
				int version = bean.version;
				ZooKeeper zk = connector.getZooKeeper(); // 连接 ZooKeeper 服务器并获取 ZooKeeper 对象
				String xsnakeNode = "/xsnake";
				String serviceNode = xsnakeNode +"/service";
				String rootNode = serviceNode+ "/"+node;
				final String versionNode = rootNode + "/"+version;
				Gson gson = new Gson();
				connector.createDirNode(xsnakeNode,null,CreateMode.PERSISTENT);
				connector.createDirNode(serviceNode,null,CreateMode.PERSISTENT);
				
				if(connector.getZooKeeper().exists(rootNode, null)==null){
					Map<String,Object> interfaceInfo = new HashMap<String,Object>(); 
					interfaceInfo.put("maxVersion", String.valueOf(version));
					interfaceInfo.put("startupDate", String.valueOf(new Date().getTime()));
					String interfaceInfoData = gson.toJson(interfaceInfo);
					connector.getZooKeeper().create(rootNode, interfaceInfoData.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}else{
					Map<String,Object> interfaceInfo = connector.getMapData(rootNode);
					interfaceInfo.put("startupDate", String.valueOf(new Date().getTime()));
					int v = Integer.parseInt(String.valueOf(interfaceInfo.get("maxVersion")));
					if(v<version){
						interfaceInfo.put("maxVersion", String.valueOf(version));
					}
					String interfaceInfoData = gson.toJson(interfaceInfo);
					connector.getZooKeeper().setData(rootNode, interfaceInfoData.getBytes(),-1);
				}
				
				connector.createDirNode(versionNode,null,CreateMode.PERSISTENT);
				
				String path = zk.create(versionNode + "/"+UUID.randomUUID().toString(), url.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
				LOG.info(String.format(" publish [%s] to [%s] >> rmi : [%s] version : [%d]",bean.serviceInterface.getName(),path,url,bean.version));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void exportService(RemoteServiceBean bean,RMIServerSocketFactory server,RMIClientSocketFactory client){
		RmiServiceExporter se = new RmiServiceExporter();
		se.setServiceName(bean.name);
		Object obj = new XSnakeInterceptorHandler(info,interceptorList).createProxy(bean.proxy);
		se.setService(obj);
		se.setAlwaysCreateRegistry(alwaysCreateRegistry);
		alwaysCreateRegistry = false;
		se.setRegistryPort(port);
		se.setServiceInterface(bean.serviceInterface);
		se.setClientSocketFactory(client);
		se.setServerSocketFactory(server);
		try {
			rmiServiceExporterList.add(se);
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
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public XSnakeAdminConfiguration getAdminDatabaseConfig() {
		return adminDatabaseConfig;
	}
	public void setAdminDatabaseConfig(XSnakeAdminConfiguration adminDatabaseConfig) {
		this.adminDatabaseConfig = adminDatabaseConfig;
	}

	public void destroy(){
		//关闭ZooKeeper链接资源
		if(connector!=null){
			try {
				connector.getZooKeeper().close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//释放RMI服务
		for(RmiServiceExporter rmi : rmiServiceExporterList){
			try {
				rmi.destroy();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		//释放连接池
		if(ConnectionPool.getInstance()!=null){
			ConnectionPool.getInstance().destroy();
		}
		
		instance = null;
		
	}

	public static RemoteAccessFactory getInstance() {
		return instance;
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

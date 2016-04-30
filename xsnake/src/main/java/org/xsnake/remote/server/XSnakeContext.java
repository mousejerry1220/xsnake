package org.xsnake.remote.server;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.xsnake.common.ReflectionUtil;
import org.xsnake.logs.XSnakeLogsInterface;
import org.xsnake.logs.mysql.MysqlLogsImpl;
import org.xsnake.remote.XSnakeRMIAuthentication;
import org.xsnake.remote.connector.ZookeeperConnector;

public abstract class XSnakeContext implements ApplicationContextAware , Serializable{

	private static final long serialVersionUID = 1L;
	
	private final static Logger LOG = LoggerFactory.getLogger(XSnakeContext.class) ;

	static XSnakeContext context = null;
	
	public void destroy(){
		//关闭ZooKeeper链接资源
		if(connector!=null){
			connector.close();
		}
	}
	
	protected XSnakeContext(){
		context = this;
	}
	
	//配置项
	protected String serverId;
	
	protected String host; //RMI的地址

	protected int port = 0; //RMI端口号
	
	protected int timeout = 10; //连接zooKeeper的超时时间
	
	protected String zookeeperAddress;
	
	protected List<String> trustAddress; //信任的IP地址过滤列表
	
	protected List<String> interceptors; //拦截器

	protected String authenticationInterface; //身份验证接口
	
	protected DataSource dataSource; //日志数据源

	//--------------------------
	Date startupDate = null;
	
	long startupUseTime = -1;
	
	ServerInfo info = new ServerInfo();
	
	ZookeeperConnector connector;
	
	List<XSnakeInterceptor> interceptorList = new ArrayList<XSnakeInterceptor>();
	
	XSnakeRMIAuthentication authentication;
	
	XSnakeLogsInterface logger = new MysqlLogsImpl();
	
	public final void setApplicationContext(ApplicationContext applicationContext)throws BeansException {
	
		LOG.info(" xsnake start !");
		
		long start = System.currentTimeMillis();
		
		initConfig();

		initServiceContext();
		
		findRemoteService(applicationContext);

		exportService(serviceBeanList);
		
		//初始化ZooKerper连接器
		initZooKeeper();
		
		startupUseTime = System.currentTimeMillis() - start;
		//设置服务信息
		initServerInfo();
		
		logger.log4ServerStartup();
	}

	private void initConfig() {
		//如果配置了身份验证接口
		if(authenticationInterface !=null){
			try {
				Object obj = Class.forName(authenticationInterface).newInstance();
				if(obj instanceof XSnakeRMIAuthentication){
					authentication = (XSnakeRMIAuthentication)obj;
				}else{
					throw new BeanCreationException("身份验证接口必须实现接口 org.xsnake.remote.XSnakeRMIAuthentication");
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
				throw new BeanCreationException(e.getMessage());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new BeanCreationException(e.getMessage());
			}catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new BeanCreationException(e.getMessage());
			}
			
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
				} catch (InstantiationException e) {
					e.printStackTrace();
					throw new BeanCreationException(e.getMessage());
				}catch (IllegalAccessException e) {
					e.printStackTrace();
					throw new BeanCreationException(e.getMessage());
				}catch (ClassNotFoundException e) {
					e.printStackTrace();
					throw new BeanCreationException(e.getMessage());
				}
				
			}
			
		}
	}
	
	//服务参数初始化
	abstract void initServiceContext();
	
	//导出服务
	abstract void exportService(List<RemoteServiceBean> serviceBeanList);
	
	//发布服务
	abstract void publish(List<RemoteServiceBean> serviceBeanList);
	
	private void initServerInfo() {
		info.serverId = serverId;
		info.host = host;
		info.port = port;
		info.startupDate = new Date();
		info.startupUseTime = startupUseTime;
	}
	
	private void initZooKeeper() {
		if(zookeeperAddress !=null){
			connector = ZookeeperConnector.getConnector(zookeeperAddress,timeout,new Watcher() {
				public void process(WatchedEvent event) {
					if (event.getState() == Event.KeeperState.SyncConnected) {
						publish(serviceBeanList);
	                }
				}
			});
		}
	}
	
	//寻找有Remote注解的Service
	protected List<RemoteServiceBean> serviceBeanList = new ArrayList<RemoteServiceBean>();
	
	private void findRemoteService(ApplicationContext applicationContext) {
		String[] names = applicationContext.getBeanDefinitionNames();
		for (String name : names) {
			Object obj = applicationContext.getBean(name);
			Object target = ReflectionUtil.getTarget(obj);
			Remote remote = target.getClass().getAnnotation(Remote.class);
			if (remote != null) {
				serviceBeanList.add(RemoteServiceBean.createServiceBean(obj,target));
			}
		}
	}
	
	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
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

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public static Connection getConnection() throws SQLException{
		if(context.dataSource == null){
			return null;
		}
		return context.dataSource.getConnection();
	}
	
	public static XSnakeLogsInterface getLogger(){
		return context.logger;
	}
	
	public static List<XSnakeInterceptor> getInterceptorList(){
		return context.interceptorList;
	}
	
	public static ServerInfo getServerInfo(){
		return context.info;
	}
	
}

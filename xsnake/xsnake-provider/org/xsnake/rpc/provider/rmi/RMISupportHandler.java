package org.xsnake.rpc.provider.rmi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.xsnake.rpc.annotation.Remote;
import org.xsnake.rpc.common.ReflectionUtil;
import org.xsnake.rpc.connector.ZooKeeperConnector;
import org.xsnake.rpc.connector.ZooKeeperExpiredCallBack;
import org.xsnake.rpc.connector.ZooKeeperWrapper;
import org.xsnake.rpc.provider.XSnakeProviderContext;

public class RMISupportHandler implements ZooKeeperExpiredCallBack{

	ZooKeeperWrapper zooKeeper = null;
	
	private int defaultPort = 16785;
	
	private String host = null;
	
	RMIServerSocketFactory server = new XSnakeServerSocketFactory();
	RMIClientSocketFactory client = new XSnakeClientSocketFactory();
	
	Semaphore maxThread;
	
	XSnakeProviderContext context;
	
	List<XSnakeInterceptorHandler> handlerList = new ArrayList<XSnakeInterceptorHandler>();
	
	List<RmiServiceExporter> rmiServiceExporterList = new ArrayList<RmiServiceExporter>();
	
	
	public RMISupportHandler(XSnakeProviderContext context) throws BeanCreationException {
		this.context = context;
		init();
	}
	
	public void init(){
		maxThread = new Semaphore(context.getRegistry().getMaxThread());
		
		// 连接ZooKeeper
		if (StringUtils.isEmpty(context.getRegistry().getZooKeeper())) {
			throw new BeanCreationException("XSnake启动失败，配置参数registry.zooKeeper不能为空");
		}
		
		try {
			zooKeeper = new ZooKeeperConnector(context.getRegistry().getZooKeeper(), context.getRegistry().getTimeout(),this);
		} catch (Exception e) {
			throw new BeanCreationException("XSnake启动失败，无法连接到ZooKeeper服务器。" + e.getMessage());
		}
		
		//设置RMI主机地址
		host = context.getRegistry().getRmiHost();
		if(host == null){
			host = getLocalHost();
		}
		System.setProperty("java.rmi.server.hostname", host);
		
		// 初始化XSNAKE主目录
		try {
			zooKeeper.dir("/XSNAKE");
			zooKeeper.dir(PATH_ROOT());
			zooKeeper.dir(PATH_ROOT_SERVICES());
			zooKeeper.dir(PATH_ROOT_SERVICESINFO());
			zooKeeper.dir(PATH_ROOT_APPLICATIONS());
			zooKeeper.dir(PATH_ROOT_APPLICATIONS_APP());
			zooKeeper.dir(PATH_ROOT_APPLICATIONS_APP()+"/SERVICES");
			
			zooKeeper.dir(PATH_ROOT_NODES());
			zooKeeper.dir(PATH_ROOT_NODES_HOST());
			zooKeeper.dir(PATH_ROOT_NODES_HOST_PORTS());
			zooKeeper.dir(PATH_ROOT_NODES_HOST_SERVICES());
			
		} catch (Exception e) {
			throw new BeanCreationException("XSnake启动失败，初始化数据失败。" + e.getMessage());
		}
		
		export(context);
		printLogo();
	}
	
	
	private void printLogo(){
		System.out.println("  ,@@     @!     @   #@@                               @");                                               
		System.out.println("   @@     #     @     .@                               @");                                               
		System.out.println("    @    ;     @       #                               @");                                               
		System.out.println("    @@   @     @        ,                              @");                                               
		System.out.println("     @  ;      @.                                      @");                                               
		System.out.println("     @# @      @@          -;@  ,@@:        ;@@;       @    ;;;;       $@!");                            
		System.out.println("     .@:        @@@          @ @:  #@     @!    @,     @     @*      @:   @* ");                           
		System.out.println("      @@         ;@@@        @@     @    !@     ,@     @    #       *=     @-");                           
		System.out.println("      :@           :@@@      @      @.    @     .@     @   =.       @      $@");                           
		System.out.println("      @@=            *@@     @      @.        ,-@@     @  !:       -@      :@");                           
		System.out.println("     ..;@              @$    @      @.     $@@  .@     @ !@@       #@@@@@@@@@");                           
		System.out.println("     @  @;             ~@    @      @.    @=    .@     @=; @#      #@        ");                           
		System.out.println("    -   $@     -        @    @      @.   @@     .@     @!   @.     *@        ");                           
		System.out.println("    @    @~    @        @    @      @.   @~     .@     @    *@      @        ");                           
		System.out.println("   .,    $@    @,      #!    @      @.   @~     .@     @     @#     @$      !");    
		System.out.println("   @      @-   @@-     @     @      @.   @@    .#@ -   @      @.     @;    @ ");                           
		System.out.println(" $@@@    @@@@  ~ .@@@@*    !@@@@  .@@@@   $@@@@  @@  ,@@@@   @@@@     @@@@# ");
	}

	protected String getLocalHost() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new BeanCreationException("创建对象失败，无法自动获取主机地址，请配置");
		}
		
	}
	
	/**
	 * 顺序获取可用的端口 
	 * @param port
	 * @return
	 */
	private int getPort(int port) {
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
	
	private void export(XSnakeProviderContext context) {
		handlerList.clear();
		rmiServiceExporterList.clear();
		boolean flag = true;
		ApplicationContext applicationContext = context.getApplicationContext();
		int port = getPort(defaultPort);
		String[] names = applicationContext.getBeanDefinitionNames();
		for (String name : names) {
			Object obj = applicationContext.getBean(name);
			Object target = ReflectionUtil.getTarget(obj);
			Class<?>[] interfaces = target.getClass().getInterfaces();
			for (Class<?> interFace : interfaces) {
				Remote remote = interFace.getAnnotation(Remote.class);
				if (remote != null && !ReflectionUtil.isProxy(target)) {
					try {
						RmiServiceExporter se = new RmiServiceExporter();
						String nodeName = UUID.randomUUID().toString();
						XSnakeInterceptorHandler handler = new XSnakeInterceptorHandler(interFace,target,nodeName,maxThread,host,port);
						se.setServiceName(nodeName);
						Object proxy = handler.createProxy();
						se.setService(proxy);
						se.setAlwaysCreateRegistry(flag);
						se.setRegistryPort(port);
						se.setServiceInterface(interFace);
						se.setClientSocketFactory(client);
						se.setServerSocketFactory(server);
						se.afterPropertiesSet();
						flag = false;
						String url = String.format("rmi://%s:%d/%s", host, port, nodeName);
						handlerList.add(handler);
						rmiServiceExporterList.add(se);
						try {
							
							zooKeeper.dir(PATH_ROOT_SERVICES()+"/"+interFace.getName());
							zooKeeper.tempDir(PATH_ROOT_SERVICES()+"/"+interFace.getName()+"/"+nodeName, url);

							//写入接口信息，当前每个接口所运行的节点实例信息
							zooKeeper.dir(PATH_ROOT_SERVICESINFO()+"/"+interFace.getName());
							zooKeeper.tempDir(PATH_ROOT_SERVICESINFO()+"/"+interFace.getName()+"/"+host+"_"+port);
							
							//应用下的接口列表
							zooKeeper.tempDir(PATH_ROOT_APPLICATIONS_APP()+"/"+interFace.getName());
							
							//节点端口信息
							zooKeeper.tempDir(PATH_ROOT_NODES_HOST_PORTS() + "/" + String.valueOf(port));
							
							//节点上所运行的接口
							zooKeeper.tempDir(PATH_ROOT_NODES_HOST_SERVICES() + "/" + interFace.getName());
						}  catch (Exception e) {
							e.printStackTrace();
							throw new BeanCreationException(e.getMessage());
						}
					} catch (RemoteException e) {
						e.printStackTrace();
						throw new BeanCreationException("RMI remote access bean [" + interFace.getName() + "] creation failed !" + e.getMessage());
					}
					
				}
			}
		}
	}

	public ZooKeeperWrapper getZooKeeper() {
		return zooKeeper;
	}
	
	public String PATH_ROOT(){
		return "/XSNAKE/" + context.getRegistry().getEnvironment();
	}

	public String PATH_ROOT_SERVICES(){
		return "/XSNAKE/" + context.getRegistry().getEnvironment()+ "/SERVICES";
	}
	
	public String PATH_ROOT_SERVICESINFO(){
		return "/XSNAKE/" + context.getRegistry().getEnvironment()+ "/SERVICESINFO";
	}
	
	public String PATH_ROOT_APPLICATIONS(){
		return "/XSNAKE/" + context.getRegistry().getEnvironment()+ "/APPLICATIONS";
	}
	
	public String PATH_ROOT_APPLICATIONS_APP(){
		return "/XSNAKE/" + context.getRegistry().getEnvironment()+ "/APPLICATIONS/"+ context.getRegistry().getApplication();
	}
	
	public String PATH_ROOT_INVOKEINFO(){
		return "/XSNAKE/" + context.getRegistry().getEnvironment()+ "/INVOKEINFO";
	}
	
	public String PATH_ROOT_NODES(){
		return "/XSNAKE/" + context.getRegistry().getEnvironment() + "/NODES";
	}
	
	public String PATH_ROOT_NODES_HOST(){
		return "/XSNAKE/" + context.getRegistry().getEnvironment() + "/NODES/" + host;
	}
	
	public String PATH_ROOT_NODES_HOST_PORTS(){
		return "/XSNAKE/" + context.getRegistry().getEnvironment() + "/NODES/" + host + "/PORTS";
	}
	
	public String PATH_ROOT_NODES_HOST_SERVICES(){
		return "/XSNAKE/" + context.getRegistry().getEnvironment() + "/NODES/" + host + "/SERVICES";
	}
	
	public List<XSnakeInterceptorHandler> getHandlerList() {
		return handlerList;
	}

	public void destory() throws RemoteException{
		for(RmiServiceExporter rmiServiceExporter : rmiServiceExporterList){
			rmiServiceExporter.destroy();
		}
		
		try {
			zooKeeper.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void callback() {
		try {
			destory();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		init();
	}
	
}

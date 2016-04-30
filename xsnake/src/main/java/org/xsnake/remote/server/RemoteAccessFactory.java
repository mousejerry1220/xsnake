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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.xsnake.remote.XSnakeClientSocketFactory;
import org.xsnake.remote.XSnakeServerSocketFactory;

import com.google.gson.Gson;

/**
 * RMI实现
 * @author Jerry.Zhao	
 */
public class RemoteAccessFactory extends XSnakeContext implements Serializable{

	private static final long serialVersionUID = 1L;

	private final static Logger LOG = LoggerFactory.getLogger(RemoteAccessFactory.class) ;

	private static final int DEFAULT_PORT = 1232;

	//内部变量
	boolean alwaysCreateRegistry = true;
	
	List<RmiServiceExporter> rmiServiceExporterList = new ArrayList<RmiServiceExporter>();//存放所有的服务导出对象以便释放资源

	protected void initServiceContext() {
		//如果没有配置host参数，那么将会使用获取的地址，
		//一般为内网IP地址，所以不配置此项会导致服务在外网无法访问。
		if(host ==null){
			host = getLocalHost();
		}
		
		if(port == 0){
			port = getPort(DEFAULT_PORT); 
		}
		System.setProperty("java.rmi.server.hostname", host);
		
		//如果serverId 为空，设置他的地址+端口为服务器标示
		if(serverId == null){
			serverId = host + ":" + port;
		}
	}

	protected void exportService(List<RemoteServiceBean> serviceBeanList){
		RMIServerSocketFactory server = new XSnakeServerSocketFactory(trustAddress,authentication);
		RMIClientSocketFactory client = new XSnakeClientSocketFactory();
		for(RemoteServiceBean bean : serviceBeanList){
			LOG.debug("export service >> "+ bean.serviceInterface.getName());
			exportService(bean,server,client);
		}
	}
	
	public void publish(List<RemoteServiceBean> serviceBeanList){
		for(RemoteServiceBean bean : serviceBeanList){
			try {
				String url = String.format("rmi://%s:%d/%s", host, port, bean.name);
				String node = bean.serviceInterface.getName();
				int version = bean.version;
				String xsnakeNode = "/xsnake";
				String serviceNode = xsnakeNode +"/service";
				String rootNode = serviceNode+ "/"+node;
				final String versionNode = rootNode + "/"+version;
				Gson gson = new Gson();
				connector.createDirNode(xsnakeNode,null,CreateMode.PERSISTENT);
				connector.createDirNode(serviceNode,null,CreateMode.PERSISTENT);
				if(!connector.exists(rootNode)){
					Map<String,Object> interfaceInfo = new HashMap<String,Object>(); 
					interfaceInfo.put("maxVersion", String.valueOf(version));
					interfaceInfo.put("startupDate", String.valueOf(new Date().getTime()));
					String interfaceInfoData = gson.toJson(interfaceInfo);
					connector.createDirNode(rootNode,interfaceInfoData.getBytes(),CreateMode.PERSISTENT);
				}else{
					Map<String,Object> interfaceInfo = connector.getMapData(rootNode);
					interfaceInfo.put("startupDate", String.valueOf(new Date().getTime()));
					int v = Integer.parseInt(String.valueOf(interfaceInfo.get("maxVersion")));
					if(v<version){
						interfaceInfo.put("maxVersion", String.valueOf(version));
					}
					String interfaceInfoData = gson.toJson(interfaceInfo);
					connector.updateDateData(rootNode,interfaceInfoData.getBytes());
				}
				connector.createDirNode(versionNode,null,CreateMode.PERSISTENT);
				String path = versionNode + "/"+UUID.randomUUID().toString();
				connector.createDirNode(path, url.getBytes(),CreateMode.EPHEMERAL);
				LOG.info(String.format(" publish [%s] to [%s] >> rmi : [%s] version : [%d]",bean.serviceInterface.getName(),path,url,bean.version));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void exportService(RemoteServiceBean bean,RMIServerSocketFactory server,RMIClientSocketFactory client){
		RmiServiceExporter se = new RmiServiceExporter();
		se.setServiceName(bean.name);
		Object obj = new XSnakeInterceptorHandler().createProxy(bean.proxy);
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

	public void destroy(){
		super.destroy();
		//释放RMI服务
		for(RmiServiceExporter rmi : rmiServiceExporterList){
			try {
				rmi.destroy();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	protected String getLocalHost() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new BeanCreationException("创建对象失败，无法自动获取主机地址，请配置");
		}
		
	}
	
	protected int getPort(int port) {
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

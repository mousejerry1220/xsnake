package org.xsnake.rpc.provider;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.xsnake.rpc.provider.rmi.RMISupportHandler;
import org.xsnake.rpc.provider.rmi.monitor.MethodMonitorUpdator;

public class XSnakeProviderContext implements ApplicationContextAware {

	RegistryConfig registry = new RegistryConfig();
	
	RMISupportHandler rmiSupportHandler ;
	
	ApplicationContext applicationContext = null;
	
	String localAddress;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		
		this.applicationContext = applicationContext;
		
		if (StringUtils.isEmpty(registry.application)) {
			throw new BeanCreationException("XSnake启动失败，配置参数registry.application不能为空");
		}

		//获取本机IP
		localAddress = getLocalHost();
		
		//读取远程配置
		/**
		try {
			remoteRegistryConfig.loadConfig(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(remoteRegistryConfig.isReady()){
			BeanUtils.copyProperties(remoteRegistryConfig, registry);
		}else{
			BeanUtils.copyProperties(registry, remoteRegistryConfig);
			try {
				remoteRegistryConfig.uploadConfig(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		**/
		
		//RMI远程调用模式
		rmiSupportHandler= new RMISupportHandler(this);
		
		//调用记录更新线程
		new MethodMonitorUpdator(this).start();
		
		System.out.println("=======初始化结束=======");
	}

	private String getLocalHost() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new BeanCreationException("创建对象失败，无法自动获取主机地址");
		}
	}

	public RegistryConfig getRegistry() {
		return registry;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public String getLocalAddress() {
		return localAddress;
	}

	public RMISupportHandler getRmiSupportHandler() {
		return rmiSupportHandler;
	}
	
}

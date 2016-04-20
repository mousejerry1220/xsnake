package org.xsnake.remote.server;

import java.util.UUID;

import org.springframework.beans.factory.BeanCreationException;

/**
 * 被扫描出来带有Remote注解的bean对象
 * @author Jerry.Zhao
 *
 */
public class RemoteServiceBean {

	Object proxy;
	Object target;
	Remote remote;
	String name;
	Class<?> serviceInterface;
	int version = 0;

	private RemoteServiceBean( Object proxyObject,Object targetObject) {
		this.target = targetObject;
		this.remote = target.getClass().getAnnotation(Remote.class);
		this.version = remote.version();
		this.name = UUID.randomUUID().toString();
		this.proxy = proxyObject;
		this.serviceInterface = getServiceInterface(target, remote);
	}
	
	//静态方法提供外部调用创建本对象
	public static RemoteServiceBean createServiceBean(Object proxyObject,Object targetObject){
		return new RemoteServiceBean(proxyObject, targetObject);
	}

	//获取目标对象的接口，如果在Remote注解中定义了父级接口优先取，未定义则取改对象的第一个接口
	private Class<?> getServiceInterface(Object target, Remote remote) {
		Class<?> clazz = remote.serviceInterface();
		if (clazz == Void.class) {
			try {
				clazz = target.getClass().getInterfaces()[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new BeanCreationException("RMI remote access bean ["
						+ target.getClass().getName()
						+ "] creation failed ! must implements a interface ");
			}
		}
		return clazz;
	}

}

package org.xsnake.remote;

import org.springframework.beans.factory.BeanCreationException;

public class RemoteServiceBean {

	Object proxy;
	Object target;
	Remote remote;
	String name;
	Class<?> serviceInterface;
	int version = 0;

	private RemoteServiceBean(String name, Object proxyObject,Object targetObject) {
		this.target = targetObject;
		this.remote = target.getClass().getAnnotation(Remote.class);
		this.version = remote.version();
		this.name = name;
		this.proxy = proxyObject;
		this.serviceInterface = getServiceInterface(target, remote);
	}
	
	public static RemoteServiceBean createServiceBean(String name, Object proxyObject,Object targetObject){
		return new RemoteServiceBean(name, proxyObject, targetObject);
	}

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

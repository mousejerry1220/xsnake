package org.xsnake.remote.server;

import java.util.UUID;

import org.springframework.beans.factory.BeanCreationException;

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
	
	public static RemoteServiceBean createServiceBean(Object proxyObject,Object targetObject){
		return new RemoteServiceBean(proxyObject, targetObject);
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

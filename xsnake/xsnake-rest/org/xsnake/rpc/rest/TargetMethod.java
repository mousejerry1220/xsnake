package org.xsnake.rpc.rest;

import java.lang.reflect.Method;

public class TargetMethod {

	public TargetMethod(String restPath,Class<?> clazz,Method method){
		this.restPath = restPath;
		this.clazz = clazz;
		this.method = method;
	}
	
	Class<?> clazz;
	
	Method method;
	
	String restPath;

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public String getRestPath() {
		return restPath;
	}

	public void setRestPath(String restPath) {
		this.restPath = restPath;
	}
	
}

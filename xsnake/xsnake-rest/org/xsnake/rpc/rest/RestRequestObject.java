package org.xsnake.rpc.rest;

import java.io.Serializable;
import java.util.Map;

public class RestRequestObject implements Serializable {

	private static final long serialVersionUID = 1L;

	String httpMethod;
	
	String path;
	
	Map<String,String> paramters;
	
	byte[] requestBody;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, String> getParamters() {
		return paramters;
	}

	public void setParamters(Map<String, String> paramters) {
		this.paramters = paramters;
	}

	public byte[] getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(byte[] requestBody) {
		this.requestBody = requestBody;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public static String createKey(String httpMethod, String path) {
		String restKey = "rest://" + httpMethod + "/" + path;
		return restKey;
	}
	
	@Override
	public String toString() {
		return createKey(httpMethod, path);
	}
	
	public String getParameter(String name){
		return paramters.get(name);
	}
	
}
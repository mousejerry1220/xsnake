package org.xsnake.rpc.rest;

import java.util.List;
import java.util.Map;

import org.springframework.util.AntPathMatcher;

public class RestService {

	private static RestService instance;
	
	public List<TargetMethod> targetList;
	
	public RestService(List<TargetMethod> targetList){
		this.targetList = targetList;
		instance = this;
		RestServer.run();
		System.out.println("=======REST初始化结束=======");
		System.out.println("=======REST服务端口："+RestServer.getRestServer().getPort()+"=======");
	}
	
	AntPathMatcher antPathMatcher = new AntPathMatcher();
	
	protected TargetMethod findTargetMethod(String path,Map<String,String> dataMap){
		for(TargetMethod tm : targetList){
			if(antPathMatcher.match(tm.getRestPath(), path)){
				Map<String,String> map = antPathMatcher.extractUriTemplateVariables(tm.getRestPath(), path);
				dataMap.putAll(map);
				return tm;
			}
		}
		return null;
	}

	public static RestService getInstance() {
		return instance;
	}
	
}

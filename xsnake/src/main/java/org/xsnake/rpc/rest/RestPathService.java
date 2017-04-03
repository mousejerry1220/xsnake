package org.xsnake.rpc.rest;

import java.util.List;
import java.util.Map;

import org.springframework.util.AntPathMatcher;

public class RestPathService {

	static RestPathService instance;
	
	public List<TargetMethod> targetList;
	
	public RestPathService(List<TargetMethod> targetList){
		this.targetList = targetList;
		instance = this;
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

	public static RestPathService getInstance() {
		return instance;
	}
	
}

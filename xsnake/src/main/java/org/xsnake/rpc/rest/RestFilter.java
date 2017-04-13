package org.xsnake.rpc.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xsnake.rpc.common.ParameterDiscoverer;
import org.xsnake.rpc.consumer.rmi.XSnakeProxyHandler;

@RestController
public class RestFilter {
	
	ParameterDiscoverer parameterDiscoverer = new ParameterDiscoverer();
	
	@ResponseBody
	@RequestMapping(value={"/**"},method={RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
	public Object restFilter(@RequestBody(required=false) String requestBody,HttpServletRequest request,HttpServletResponse response) throws Exception{
		Object result = null;
		Map<String,String> dataMap = new HashMap<String,String>();
		Enumeration<String> names = request.getParameterNames();
		while(names.hasMoreElements()){
			String key = names.nextElement();
			String value = request.getParameter(key);
			dataMap.put(key, value);
		}
		if(requestBody!=null){
			dataMap.put("requestBody", requestBody);
		}
		String uri = request.getRequestURI();
		
		RestRequest restRequest = new RestRequest();
		restRequest.setHttpMethod(request.getMethod().toUpperCase());
		restRequest.setParamters(dataMap);
		restRequest.setPath(uri);
		TargetMethod targetMethod = RestService.getInstance().findTargetMethod(restRequest.toString(), dataMap);
		if(targetMethod == null){
			response.sendError(404);
			return null;
		}else{
			Object obj = XSnakeProxyHandler.getBean(targetMethod.getClazz());//找到对应的接口，执行对应的方法
			Method method = obj.getClass().getMethod(targetMethod.getMethod().getName(), targetMethod.getMethod().getParameterTypes());
			List<Object> args = new ArrayList<Object>();
			parameterDiscoverer.getParameterNames(targetMethod.getClazz(),targetMethod.getMethod(),dataMap,args);
			result = method.invoke(obj, args.toArray());
		}
		return result;
	}
	
}

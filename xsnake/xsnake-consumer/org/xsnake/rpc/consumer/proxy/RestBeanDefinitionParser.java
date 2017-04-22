package org.xsnake.rpc.consumer.proxy;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.xsnake.rpc.annotation.RequestMethod;
import org.xsnake.rpc.annotation.Rest;
import org.xsnake.rpc.rest.RestPathService;
import org.xsnake.rpc.rest.RestRequestObject;
import org.xsnake.rpc.rest.RestServer;
import org.xsnake.rpc.rest.TargetMethod;

public class RestBeanDefinitionParser extends ClientBeanDefinitionParser implements BeanDefinitionParser {

	protected List<TargetMethod> targetList = new ArrayList<TargetMethod>();
	
	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		super.parse(element, parserContext);
		
		for(Class<?> interFace : interfaceList){
			initRestService(interFace);
		}

		
		RestServer r = new RestServer();
		r.run();
		new RestPathService(targetList);
		
		return null;
	}

	private void initRestService(Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			Rest rest = method.getAnnotation(Rest.class);
			if(rest !=null){
				RequestMethod[] httpMethods = rest.method();
				for(RequestMethod httpMethod : httpMethods){
					String restPath = RestRequestObject.createKey(httpMethod.toString(),rest.value());
					targetList.add(new TargetMethod(restPath, clazz, method));
				}
			}
		}
	}

}

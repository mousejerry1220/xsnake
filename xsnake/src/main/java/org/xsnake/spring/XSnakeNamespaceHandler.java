package org.xsnake.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class XSnakeNamespaceHandler extends NamespaceHandlerSupport{

	public void init() {
		 registerBeanDefinitionParser("client", new ClientServiceBeanDefinitionParser());
	}

}

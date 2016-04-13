package org.xsnake.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class XSnakeNamespaceHandler extends NamespaceHandlerSupport{

	@Override
	public void init() {
		 registerBeanDefinitionParser("client", new ClientServiceBeanDefinitionParser());
	}

}

package org.xsnake.remote.client;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class XSnakeNamespaceHandler extends NamespaceHandlerSupport{

	public void init() {
		 registerBeanDefinitionParser("client", new ClientServiceBeanDefinitionParser());
	}

}

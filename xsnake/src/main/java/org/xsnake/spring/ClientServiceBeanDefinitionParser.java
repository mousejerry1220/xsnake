package org.xsnake.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xsnake.remote.client.ClientAccessFactory;

public class ClientServiceBeanDefinitionParser implements BeanDefinitionParser{

	public BeanDefinition parse(Element element, ParserContext parserContext) {  
		String id = element.getAttribute("id");  
        String zookeeperAddress = element.getAttribute("zookeeperAddress");
        String _timeout = element.getAttribute("timeout");
        int timeout = 5;
        if(!StringUtils.isEmpty(_timeout)){
        	timeout = Integer.parseInt(_timeout);
        }
        
        RootBeanDefinition clientBeanDefinition = new RootBeanDefinition();
        clientBeanDefinition.setBeanClass(ClientAccessFactory.class);
        clientBeanDefinition.getPropertyValues().add("zookeeperAddress", zookeeperAddress);
        clientBeanDefinition.getPropertyValues().add("timeout", timeout);
        parserContext.getRegistry().registerBeanDefinition(id, clientBeanDefinition);
        
        NodeList list = element.getChildNodes();
        for(int i=0;i<list.getLength() ;i++){
        	Node node = list.item(i);
        	if("xsnake:service".equalsIgnoreCase(node.getNodeName())){
        		String interfaceClass = node.getAttributes().getNamedItem("interface").getNodeValue();
        		String serviceId = node.getAttributes().getNamedItem("id").getNodeValue();
        		
        		Node versionNode =  node.getAttributes().getNamedItem("version");
        		
        		int version = versionNode == null ? 0 : Integer.parseInt(versionNode.getNodeValue());
        		
        		if(StringUtils.isEmpty(interfaceClass)){
        			throw new IllegalArgumentException("xsnake:service node 'interface' attribute must be not null ");
        		}
        		
        		if(StringUtils.isEmpty(serviceId)){
        			throw new IllegalArgumentException("xsnake:service node 'id' attribute must be not null ");
        		}
        		
        		RootBeanDefinition beanDefinition = new RootBeanDefinition();
                beanDefinition.setFactoryMethodName("getService");
                beanDefinition.setFactoryBeanName(id);
                ConstructorArgumentValues values = new ConstructorArgumentValues();
                try {
					values.addIndexedArgumentValue(0, Class.forName(interfaceClass));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					throw new IllegalArgumentException(" class not found ! [" + interfaceClass + "]");
				}
                values.addIndexedArgumentValue(1, version);
                beanDefinition.setConstructorArgumentValues(values);
                parserContext.getRegistry().registerBeanDefinition(serviceId, beanDefinition);
        		
        	}
        }
        return clientBeanDefinition;  
	}
	
}

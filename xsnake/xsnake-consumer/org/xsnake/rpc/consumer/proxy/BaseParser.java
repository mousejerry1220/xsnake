package org.xsnake.rpc.consumer.proxy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.BeanCreationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BaseParser {

	protected Map<String,String> propertyMap = new HashMap<String,String>();
	
	protected void parseKevValueParamter(Element element) {
		//获取所有的参数
		NodeList paroertyList = element.getElementsByTagName("xsnake:property");
		for (int i = 0; i < paroertyList.getLength(); i++) {
			Node node = paroertyList.item(i);
			Node keyNode = node.getAttributes().getNamedItem("name");
			Node valueNode = node.getAttributes().getNamedItem("value");
			if (keyNode == null) {
				throw new BeanCreationException("xsnake:property 必须包含 name 属性");
			}
			String key = keyNode.getNodeValue();
			String value = valueNode == null ? value = node.getTextContent() : valueNode.getNodeValue();
			if (value == null) {
				throw new BeanCreationException("xsnake:property " + key + " 没有设置值");
			}
			propertyMap.put(key, value.trim());
		}
	}
	
}

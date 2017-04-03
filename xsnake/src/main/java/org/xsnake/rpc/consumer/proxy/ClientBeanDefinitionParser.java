package org.xsnake.rpc.consumer.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.xsnake.rpc.annotation.Remote;
import org.xsnake.rpc.consumer.rmi.XSnakeProxyFactory;

public class ClientBeanDefinitionParser extends BaseParser implements BeanDefinitionParser {
	
	protected List<Class<?>> interfaceList = new ArrayList<Class<?>>();

	private int scanResultCount = 0;
	
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		
		parseKevValueParamter(element);
		
		//扫描包，获得需要初始的远程接口
		String scanPackage = propertyMap.get("scanPackage");
		
		if(scanPackage == null){
			throw new BeanCreationException("xsnake:property scanPackage 必须设置接口所在包 ");
		}
		propertyMap.put("environment", StringUtils.isEmpty(propertyMap.get("environment")) ? "test" : propertyMap.get("environment"));
		
		scanPacket(scanPackage);
		
		if(scanResultCount == 0){
			throw new BeanCreationException("指定的包:"+scanPackage + " 没有扫描的符合条件的接口"); 
		}
		
		//初始化默认值
		RootBeanDefinition clientBeanDefinition = new RootBeanDefinition();
		clientBeanDefinition.setBeanClass(XSnakeProxyFactory.class);
		ConstructorArgumentValues values = new ConstructorArgumentValues();
		values.addIndexedArgumentValue(0,propertyMap);
		clientBeanDefinition.setConstructorArgumentValues(values);
		parserContext.getRegistry().registerBeanDefinition(XSnakeProxyFactory.class.getName(), clientBeanDefinition);

		for(Class<?> interFace : interfaceList){
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setFactoryBeanName(XSnakeProxyFactory.class.getName());
			beanDefinition.setFactoryMethodName("getService");
			values = new ConstructorArgumentValues();
			values.addIndexedArgumentValue(0, interFace);
			beanDefinition.setConstructorArgumentValues(values);
			parserContext.getRegistry().registerBeanDefinition(interFace.getName(), beanDefinition);
		}
		
		return null;
	}
	
	private void scanPacket(String scanPackage) {
		if(scanPackage == null){
			throw new BeanCreationException("没有指定要扫描的包位置");
		}
		
		String[] basePackages = scanPackage.split(";");
		
		for(String basePackage : basePackages){
			if(StringUtils.isEmpty(basePackage)){
				continue;
			}
			//扫描符合条件的接口
			ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
			String DEFAULT_RESOURCE_PATTERN = "**/*.class";
			MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + basePackage.replace('.', '/') + "/" + DEFAULT_RESOURCE_PATTERN;
			Resource[] resources = null;
			
			try {
				resources = resourcePatternResolver.getResources(packageSearchPath);
			} catch (IOException e) {
				e.printStackTrace();
				throw new BeanCreationException("包扫描失败:"+e.getMessage());
			}
			
			for (Resource resource : resources) {
				if (resource.isReadable()) {
					try {
						MetadataReader metadataReader =  metadataReaderFactory.getMetadataReader(resource);
						ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
						sbd.setResource(resource);
						sbd.setSource(resource);
						if(sbd.getMetadata().isInterface() && !sbd.getMetadata().isAnnotation()){
							String className = sbd.getMetadata().getClassName();
							Class<?> cls = Class.forName(className);
							Remote remote = cls.getAnnotation(Remote.class);
							if(remote!=null){
								scanResultCount++;
								interfaceList.add(cls);
							}
						}
					}
					catch (Throwable ex) {
						throw new BeanDefinitionStoreException(
								"Failed to read candidate component class: " + resource, ex);
					}
				}
			}
		}
	}
	
}

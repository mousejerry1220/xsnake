# xsnake

xsnake-1.12 下载 http://pan.baidu.com/s/1mhOcIQ8
#demo：
1、在application-context.xml中配置XSnake的启动类
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:mvc="http://www.springframework.org/schema/mvc"
	xsi:schemaLocation="http://www.springframework.org/schema/beans 
	http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
	http://www.springframework.org/schema/context 
	http://www.springframework.org/schema/context/spring-context-4.0.xsd
	http://www.springframework.org/schema/mvc 
	http://www.springframework.org/schema/mvc/spring-mvc-4.0.xsd" >
	<context:component-scan base-package="xmouse" >
		<context:include-filter type="annotation" expression="org.springframework.stereotype.Service" />
	</context:component-scan>
	
   	<bean id="remoteBeanFactory" class="org.xsnake.remote.server.RemoteAccessFactory" >
   		<property name="zookeeperAddress" value="127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183" />
   		<property name="timeout" value="15" />
   	</bean>
	
</beans>
 ```

  2、定义接口类
```
  package xmouse;
  public interface IRemoteTest {
  	String sayHello(String name);
  }
```
  3、实现接口类
 ``` 
  package xmouse;
  import org.springframework.stereotype.Service;
  import org.xsnake.remote.Remote;
  @Service
  @Remote//此处只要加上注解@Remote即可
  public class RemoteTest implements IRemoteTest {
  	public String sayHello(String name) {
  		System.out.println("hello " + name);
  		return "hello "+ name;
  	}
  }
```

4、服务端启动程序 （本机测试我们可以启动多次，每次都可以当做一个独立的服务器）
```
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
public class TestServer {
	public static void main(String[] args) {
		ApplicationContext ctx = new FileSystemXmlApplicationContext("classpath:application-context.xml");
	}
}
```

5、定义客户端调用方法
在客户端的spring的context代码中增加如下配置即可使用@Autowired注解动态注入
```
	<xsnake:client id="clientFactory" zookeeperAddress="127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183" timeout="15">
		<xsnake:service id="remoteTest" interface="xmouse.IRemoteTest" />
	</xsnake:client>
	
```	
6、或者使用以下方式
```	
import org.xsnake.remote.ClientAccessFactory;
public class TestClient {
	public static void main(String[] args) {
		ClientAccessFactory caf = new ClientAccessFactory("127.0.0.1:2181",15);
		IRemoteTest obj = caf.getServiceBean(IRemoteTest.class);
		obj.sayHello("[Jerry]" );
	}
}
```
PS：      
1、服务端我们只需要添加Remote标签，客户端可以使用配置的方式无需改动任何代码将原有系统修改成分布式系统。注意：参数与返回值需要实现序列化接口
2、如果新项目开发，建议服务端，客户端，API分为三个不同的项目。

如有疑问发送邮件：80324694@QQ.COM


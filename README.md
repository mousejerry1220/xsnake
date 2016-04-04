# xsnake

#源码打包下载包（含libs）   http://pan.baidu.com/s/1nvByXrb
#XSnake-1.01.jar下载    http://pan.baidu.com/s/1bpDLnAZ
#ZooKeeper3.4.5 下载    http://pan.baidu.com/s/1nv3r3ip
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
	
   	<bean id="remoteBeanFactory" class="org.xsnake.remote.RemoteAccessFactory" >
<!--    如果有外网地址请配置，否则可以省略 -->
<!--    <property name="host" value="127.0.0.1" />   --> 
<!-- 	如果不配置RMI端口，则启用递增分配，递增从1232开始	 -->
<!--    <property name="port" value="1234" /> -->
   		<property name="zookeeperAddress" value="127.0.0.1:2181" />
		<!-- 客户端与服务端在同一服务器时，过滤不生效 。IP可以是正则表达式-->
<!--    	<property name="trustAddress"> -->
<!-- 			<array> -->
<!-- 				<value>192.168.0.*</value>  -->
<!-- 			</array> -->
<!--    	</property> -->

<!-- 		服务拦截器 -->
<!-- 		<property name="interceptors"> -->
<!-- 			<array> -->
<!-- 				<value>xmouse.TestAop</value>  -->
<!-- 				<value>xmouse.TestAop2</value>  -->
<!-- 			</array> -->
<!-- 		</property> -->
   	</bean>
	
</beans>
 ```

  2、定义接口类
```
  public interface IRemoteTest {
  	String sayHello(String name);
  }
```
  3、实现接口类
 ``` 
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
4、定义客户端调用方法
```
import org.xsnake.remote.ClientAccessFactory;
public class TestClient {
	public static void main(String[] args) {
		ClientAccessFactory caf = new ClientAccessFactory("127.0.0.1:2181",10);
		for(int i =0;i<20;i++){ //循环20次
			IRemoteTest obj = caf.getServiceBean(IRemoteTest.class);
			obj.sayHello("[Jerry]" );
		}
	}
}
```
5、服务端启动程序 （本机测试我们可以启动多次，每次都可以当做一个独立的服务器）
```
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
public class TestServer {
	public static void main(String[] args) {
		ApplicationContext ctx = new FileSystemXmlApplicationContext("classpath:application-context.xml");
	}
}
```
6、看到20次的客户端调用分别被启动的多个服务运行。OK成功！
      
服务端我们只需要添加Remote标签  XSnake即可将一个普通的Service发布成RMI，通过ZooKeeper注册，将所有请求发放到集群中的各个服务端分别处理

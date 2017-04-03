## XSnake用途
XSnake可快速搭建高可用性的分布式系统及REST服务。
***
### 免费获取XSnake
```
<dependency>
	<groupId>org.xsnake</groupId>
	<artifactId>xsnake</artifactId>
	<version>1.90</version>
</dependency>
```
***
### XSnake原理
###### 服务端：基于Spring提供的bean管理，使用RMI做RPC基础，通过ZooKeeper做服务的注册与发现。
###### 客户端：通过JDK代理方式建立远程RMI连接
***
#### 必须的依赖：ZooKeeper(http://zookeeper.apache.org/)
***
### XSnake优势
- 接入便捷 对项目零入侵式，只要遵守MVC开发规范的项目都可以低成本快速接入，将其升级为分布式应用
- REST服务 只需要对接口进行注解，即可将原服务升级为REST方式的服务
- 环境分离 多个环境可以共存在一个集群中，互不干扰
- 服务监控 可以实时监控服务被调用的情况，以及每个节点的资源利用率，来合理分配硬件资源，提高硬件利用率
- 远程控制 可以通过控制台远程发布，单节点发布，全集群共享，对每个节点，每个服务的停用，重启操作
- 远程配置 可以动态修改远程配置参数来达到调优效果
- 日志跟踪 可以对异常服务进行预警
- 升级维护 服务升级或者例行维护都无需停止整个应用，保证24小时对外提供服务
- 同步方法 可以支持整个分布式应用中所有节点中被设置同步的方法，在同一时间只被单个线程执行
- 并发控制 可以控制服务实例的最大并发数，每个方法的最大并发数
- 高可用性 某一节点宕机，只要集群中还存在其他节点，不会影响客户端调用
- 高并发性 可以通过横向扩展，单节点上运行多个服务实例，提高硬件资源利用率达到高并发的需求
- 持续更新 
***
### XSnake服务端用法
- 服务端配置文件 xsnake-provider-context.xml
```
<!-- 扫描提供服务的Service包路径 -->
<context:component-scan base-package="test" >
	<context:include-filter type="annotation" expression="org.springframework.stereotype.Service" />
</context:component-scan>

<bean id="xsnake" class="org.xsnake.rpc.provider.XSnakeProviderContext">
	<!-- zooKeeper连接地址 -->
	<property name="registry.zooKeeper" value="127.0.0.1:2181" />
	<!-- 给服务起名用于监控 -->
	<property name="registry.application" value="MyPrjectA" />
	<!-- 最大并发数 -->
	<property name="registry.maxThread" value="100" />
	<!-- 监控间隔 -->
	<property name="registry.monitorInterval" value="60" />
	<!-- 服务运行的环境，用于区分开发，测试，生产等不同环境 -->
	<property name="registry.environment" value="test" />
</bean>
```
- 服务端JAVA接口
```
package test;

import org.xsnake.rpc.annotation.Remote;

@Remote
public interface IMyService {
	String hello (String name);
}
```
- 服务端JAVA实现
```
package test;

import org.springframework.stereotype.Service;

@Service
public class MyServiceImpl implements IMyService{
	@Override
	public String hello(String name) {
		return "hello , " + name;
	}
}
```
- 服务端 启动类
```
package test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
public class TestMain {
	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("xsnake-provider-context.xml");
	}
}
```
### 客户端用法 
###### 客户端必须有服务提供端的JAVA接口文件（本例中即：test.IMyService）
- 客户端配置文件 xsnake-consumer-context.xml
```
<xsnake:client>
	<xsnake:propertys>
		<!-- 提供服务的接口所在的包，多个可以使用英文分号做分隔 （必须的参数对应服务提供端的扫描包路径）-->
		<xsnake:property name="scanPackage" value="test" />
		<!-- 启动时初始连接，如果超时则启动失败 （秒）-->
		<xsnake:property name="initTimeout" value="5" />
		<!-- zooKeeper 所在的地址 （必须的参数）-->
		<xsnake:property name="zooKeeper" value="127.0.0.1:2181" />
		<!-- 环境，同一个接口可能存在多个环境中，指定连接的环境 （默认为"test"）-->
		<xsnake:property name="environment" value="test" />
		<!-- 失败后重试间隔次数-->
		<xsnake:property name="retry" value="10" />
		<!-- 失败后重试间隔时间（毫秒）-->
		<xsnake:property name="retryInterval" value="500" />
	</xsnake:propertys>
</xsnake:client>
```
- 客户端调用类
```
package test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestClient {
	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("xsnake-consumer-context.xml");
		final IMyService s = ctx.getBean(IMyService.class);
		System.out.println(s.hello("jerry"));
	}
}
```
### 以上即是提供方与消费方的全部代码示例
***
### REST服务
- JAVA接口
```
package test;

import org.xsnake.rpc.annotation.Remote;
import org.xsnake.rpc.annotation.RequestParam;
import org.xsnake.rpc.annotation.Rest;
@Remote
public interface IRestTest {
	@Rest(value="/test/{name}")
	String sayHello(@RequestParam(name="name") String inputName);
	
	@Rest(value="/test/{name}/{age}")
	String sayHello(TestParam testParam);
	
	@Rest(value="/test")
	String sayHello(@RequestParam(name="name") String inputName,@RequestParam(name="age") int age);
}
```
- JAVA实现
```
package test;
import org.springframework.stereotype.Service;

@Service
public class RestTestImpl implements IRestTest{
	public String sayHello(String inputName)  {
		return "  hello "+ inputName;
	}
	
	public String sayHello(TestParam test)  {
		return  " type:1, name: "+ test.getName() + "  ,   age :  "+test.getAge() ;
	}
	
	@Override
	public String sayHello(String inputName, int age) {
		return  " type:2, name: "+ inputName+ "  ,   age :  "+age ;
	}
}
```
- REST配置文件 xsnake-provider-rest-context.xml
###### 如果集群中已经存在了服务提供者，则可以不必配置服务端，REST端也是一个客户端消费者,我们可以看到和客户端的配置几乎一样
```
<!-- 服务提供者 -->
<context:component-scan base-package="test" >
	<context:include-filter type="annotation" expression="org.springframework.stereotype.Service" />
</context:component-scan>

<bean id="xsnake" class="org.xsnake.rpc.provider.XSnakeProviderContext">
	<!-- zooKeeper连接地址 -->
	<property name="registry.zooKeeper" value="127.0.0.1:2181" />
	<!-- 给服务起名用于监控 -->
	<property name="registry.application" value="MyPrjectA" />
	<!-- 最大并发数 -->
	<property name="registry.maxThread" value="100" />
	<!-- 监控间隔 -->
	<property name="registry.monitorInterval" value="60" />
	<!-- 服务运行的环境，用于区分开发，测试，生产等不同环境 -->
	<property name="registry.environment" value="test" />
</bean>
<!-- REST服务 -->
<xsnake:rest>
	<xsnake:propertys>
		<!-- 提供服务的接口所在的包，多个可以使用英文分号做分隔 （必须的参数）-->
		<xsnake:property name="scanPackage" value="test" />
		<!-- 启动时初始连接，如果超时则启动失败 （秒）-->
		<xsnake:property name="initTimeout" value="5" />
		<!-- zooKeeper 所在的地址 （必须的参数）-->
		<xsnake:property name="zooKeeper" value="127.0.0.1:2181" />
		<!-- 环境，同一个接口可能存在多个环境中，指定连接的环境 （默认为"test"）-->
		<xsnake:property name="environment" value="test" />
		<!-- 失败后重试间隔次数-->
		<xsnake:property name="retry" value="10" />
		<!-- 失败后重试间隔时间（毫秒）-->
		<xsnake:property name="retryInterval" value="500" />
	</xsnake:propertys>
</xsnake:rest>
```
- REST启动类
```
package test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestRest {
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("xsnake-provider-rest-context.xml");
	}
}
```
- REST测试
###### 打开浏览器输入：http://localhost:12345/test/jerry/18
###### 返回结果 type:1, name: jerry , age : 18
###### 打开浏览器输入：http://localhost:12345/test?name=jerry&age=18
###### 返回结果 type:2, name: jerry , age : 18
***
### 提示
- XSnake只需要对接口进行注解即可。完全不需要修改实现类
###### 技术讨论与建议意见请联系 mousejerry1220#gmail.com (#->@)

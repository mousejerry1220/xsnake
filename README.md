# xsnake

demo：
1、在application-context.xml中配置XSnake的启动类
```
<bean id="remoteBeanFactory" class="org.xsnake.remote.RemoteAccessFactory" >
   <property name="zookeeperAddress" value="127.0.0.1:2181" />
</bean>
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

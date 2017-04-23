package test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
/**
 * 以REST方式启动XSnake,配置文件xsnake-provider-rest-context.xml中包含服务提供端和REST消费端，两者组合为REST服务端。
 * 当然如果我们已经存在了服务端，只是想要一个纯粹的REST服务也是可以的，xsnake-rest-context.xml只需要REST的配置即可
 * @author Administrator
 *
 */
public class TestRest {

	public static void main(String[] args) {
		
		//ApplicationContext ctx = new ClassPathXmlApplicationContext("xsnake-provider-rest-context.xml");
		ApplicationContext ctx = new ClassPathXmlApplicationContext("xsnake-rest-context.xml");
	}
	
}

package test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication
public class TestClient {
	
	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("xsnake-consumer-context.xml");
		final IMyService s = (IMyService)ctx.getBean(IMyService.class.getName());
		System.out.println(s.hello("jerry"));
	}
	
}

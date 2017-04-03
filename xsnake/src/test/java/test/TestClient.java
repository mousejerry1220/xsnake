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

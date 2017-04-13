package test;

import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.qos.logback.core.util.TimeUtil;

public class TestRest {

	public static void main(String[] args) throws InterruptedException {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("xsnake-provider-context.xml");

		ApplicationContext ctx2 = new ClassPathXmlApplicationContext("xsnake-rest-context.xml");
	}
	
}
 
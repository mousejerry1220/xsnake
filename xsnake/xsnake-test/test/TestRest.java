package test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestRest {

	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("xsnake-rest-context.xml");
	}
	
}

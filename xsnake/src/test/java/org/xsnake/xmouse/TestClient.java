package org.xsnake.xmouse;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.xsnake.remote.client.ClientAccessFactory;

public class TestClient {

	private static ApplicationContext ctx;

	public static void main(String[] args) {
		IRemoteTest remoteTest = null;
		
		//方式一
		ctx = new FileSystemXmlApplicationContext("classpath:client-application-context.xml");
		remoteTest = (IRemoteTest) ctx.getBean("remoteTest");
		remoteTest.sayHello(" xsnake ");

		//方式二
		ClientAccessFactory caf = new ClientAccessFactory("127.0.0.1:2181",10);
		remoteTest = caf.getService(IRemoteTest.class);
		remoteTest.sayHello(" xsnake ");

	}

}

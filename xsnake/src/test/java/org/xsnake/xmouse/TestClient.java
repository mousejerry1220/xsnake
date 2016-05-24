package org.xsnake.xmouse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;
import org.xsnake.remote.client.ClientAccessFactory;

public class TestClient {

	private static ApplicationContext ctx;

	public static void main(String[] args) throws InterruptedException {
		
//		//方式一
//		ctx = new FileSystemXmlApplicationContext("classpath:client-application-context.xml");
//		remoteTest = (IRemoteTest) ctx.getBean("remoteTest");
//		remoteTest.sayHello(" xsnake ");
		ExecutorService service =  Executors.newFixedThreadPool(200);
		
		//方式二
		final ClientAccessFactory caf = new ClientAccessFactory("127.0.0.1:2181",10);
		
		List<Thread> list = new ArrayList<Thread>();
		
		for(int i=0;i<10;i++){
				IRemoteTest remoteTest = null;
				remoteTest = caf.getService(IRemoteTest.class);
				
				for(int k=0;k<1;k++){
					System.out.println(remoteTest.sayHello(" xsnake "));
				}
				TimeUnit.SECONDS.sleep(5);
			}
			
//		for(Thread t : list){
//			service.execute(t);
//		}
//		service.shutdown();
		
	}

}

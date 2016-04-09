package xmouse;

import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.xsnake.remote.server.RemoteAccessFactory;

public class TestServer {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ApplicationContext ctx = new FileSystemXmlApplicationContext("classpath:application-context.xml");
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		RemoteAccessFactory.getInstance().destroy();
		((AbstractXmlApplicationContext)ctx).refresh();
	}
}

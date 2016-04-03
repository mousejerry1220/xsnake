package xmouse;

import org.springframework.stereotype.Service;
import org.xsnake.remote.Remote;

@Service
@Remote(version = 3)
public class RemoteTest implements IRemoteTest {
	
	public String sayHello(String name) {
		System.out.println("hello " + name);
		return "hello "+ name;
	}

}

package org.xsnake.xmouse;


import org.springframework.stereotype.Service;
import org.xsnake.remote.server.Remote;

@Service
@Remote(version = 5)
public class RemoteTest implements IRemoteTest {
	
	public String sayHello(String name)  {
		System.out.println("hello " + name);
		return "hello "+ name;
	}

}

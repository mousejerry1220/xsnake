package org.xsnake.xmouse;


import org.springframework.stereotype.Service;
import org.xsnake.remote.server.Remote;

@Service
@Remote(version = 1)
public class RemoteTest implements IRemoteTest {
	
	public String sayHello(String name)  {
		return " 1 hello "+ name;
	}

}

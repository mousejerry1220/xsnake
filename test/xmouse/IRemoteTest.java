package xmouse;

import org.xsnake.remote.Remote;

@Remote(version = 2)
public interface IRemoteTest {

	void sayHello(String name);
	
}

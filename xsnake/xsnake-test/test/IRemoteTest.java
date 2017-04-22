package test;

import org.xsnake.rpc.annotation.Remote;
import org.xsnake.rpc.annotation.Rest;
@Remote
public interface IRemoteTest {

	@Rest(value="/test/{name}/{aaa}")
	String sayHello(TestParam test);
}

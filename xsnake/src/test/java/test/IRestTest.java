package test;

import org.xsnake.rpc.annotation.Remote;
import org.xsnake.rpc.annotation.RequestParam;
import org.xsnake.rpc.annotation.Rest;
@Remote
public interface IRestTest {

	@Rest(value="/test/{name}")
	String sayHello(@RequestParam(name="name") String inputName);
	
	@Rest(value="/test/{name}/{age}")
	String sayHello(TestParam testParam);
	
	@Rest(value="/test")
	String sayHello(@RequestParam(name="name") String inputName,@RequestParam(name="age") int age);
	
}

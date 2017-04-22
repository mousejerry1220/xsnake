package test;


import org.springframework.stereotype.Service;

@Service
public class RemoteTest implements IRemoteTest{
	
	public String sayHello(TestParam test)  {
		return " hello "+ test.getName() + "     ，    "+test.getAaa() ;
	}
	
}

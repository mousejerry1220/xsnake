package test;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.stereotype.Service;

@Service
public class RemoteTest implements IRemoteTest{
	
	public String sayHello(String inputName)  {
		return " service on : "+getLocalHost() +" hello "+ inputName;
	}
	
	public String sayHello(TestParam test)  {
		return "service on : "+getLocalHost() + " type:1, name: "+ test.getName() + "  ,   age :  "+test.getAge() ;
	}
	
	@Override
	public String sayHello(String inputName, int age) {
		return "service on : "+getLocalHost() + " type:2, name: "+ inputName+ "  ，   age :  "+age ;
	}
	
	protected String getLocalHost() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}

package test;
import org.springframework.stereotype.Service;

@Service
public class RestTestImpl implements IRestTest{
	
	public String sayHello(String inputName)  {
		return "  hello "+ inputName;
	}
	
	public String sayHello(TestParam test)  {
		return  " type:1, name: "+ test.getName() + "  ,   age :  "+test.getAge() ;
	}
	
	@Override
	public String sayHello(String inputName, int age) {
		return  " type:2, name: "+ inputName+ "  ，   age :  "+age ;
	}
	
}

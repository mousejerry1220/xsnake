package xmouse;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;

public class RMITest {

	public static void main(String[] args) {
		RmiProxyFactoryBean c = new RmiProxyFactoryBean();
    	c.setServiceInterface(IRemoteTest.class);
    	c.setServiceUrl("rmi://10.42.23.111:1233/remoteTest");
    	c.afterPropertiesSet();
    	Object o = c.getObject();
    	System.out.println(o);
    	((IRemoteTest)o).sayHello("Jerry111");

	}
	
}

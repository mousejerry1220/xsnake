package xmouse;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;

public class RMITest {

	public static void main(String[] args) {
		RmiProxyFactoryBean c = new RmiProxyFactoryBean();
    	c.setServiceInterface(IRemoteTest.class);
    	c.setServiceUrl("rmi://127.0.0.1:1232/remoteTest");
    	c.setLookupStubOnStartup(false);
    	c.setRefreshStubOnConnectFailure(true);
    	c.afterPropertiesSet();
    	Object o = c.getObject();
    	System.out.println(o);
    	try {
			((IRemoteTest)o).sayHello("Jerry111");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}

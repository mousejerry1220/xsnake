package xmouse;

import org.xsnake.remote.ClientAccessFactory;

public class TestClient {


	public static void main(String[] args) {
		
		ClientAccessFactory caf = new ClientAccessFactory("127.0.0.1:2181",10);
		
		IRemoteTest obj = caf.getServiceBean(IRemoteTest.class);
		obj.sayHello("aaaaa");
		
		obj = caf.getServiceBean(IRemoteTest.class);
		obj.sayHello("bbbb");
		
		caf.getServiceBean(IWechatService.class).xxx();
		
	}
	
}

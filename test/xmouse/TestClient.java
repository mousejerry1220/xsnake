package xmouse;

import org.xsnake.remote.ClientAccessFactory;

public class TestClient {

	public static void main(String[] args) {
		ClientAccessFactory caf = new ClientAccessFactory("127.0.0.1:2181",10);
		for(int i =0;i<20;i++){
			IRemoteTest obj = caf.getService(IRemoteTest.class);
			System.out.println(obj.sayHello("[Jerry]" + "    "));
		}
	}
	
}

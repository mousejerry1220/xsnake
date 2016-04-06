package xmouse;

import org.xsnake.remote.server.InvokeInfo;
import org.xsnake.remote.server.ServerInfo;
import org.xsnake.remote.server.XSnakeAbstactInterceptor;

public class TestAop2 extends XSnakeAbstactInterceptor{

	@Override
	public void after(ServerInfo info,InvokeInfo invokeInfo) {
		System.out.println("记录操作" + invokeInfo.getResult());
	}

}

package org.xsnake.xmouse;

import org.xsnake.remote.server.InvokeInfo;
import org.xsnake.remote.server.ServerInfo;
import org.xsnake.remote.server.XSnakeInterceptor;

public class TestAop implements XSnakeInterceptor {

	@Override
	public void after(ServerInfo info,InvokeInfo invokeInfo) {
		System.out.println(" 来自服务器：" + info.getServerId() + "记录日志" + invokeInfo.getResult());
	}

	@Override
	public void before(ServerInfo info,InvokeInfo invokeInfo) {
		System.out.println("=========================");
	}

}

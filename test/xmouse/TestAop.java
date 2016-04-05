package xmouse;

import java.lang.reflect.Method;

import org.xsnake.remote.server.ServerInfo;
import org.xsnake.remote.server.XSnakeInterceptor;

public class TestAop implements XSnakeInterceptor {

	@Override
	public void after(ServerInfo info,Object target, Method method, Object[] args,Object result) {
		System.out.println(" 来自服务器：" + info.getServerId() + "记录日志" + result);
	}

	@Override
	public void before(ServerInfo info,Object target, Method method, Object[] args) {
		System.out.println("=========================");
	}

}

package xmouse;

import java.lang.reflect.Method;

import org.xsnake.remote.server.XSnakeInterceptor;

public class TestAop implements XSnakeInterceptor {

	@Override
	public void after(String serverId,Object target, Method method, Object[] args,Object result) {
		System.out.println("来自服务器：" + serverId + "记录日志" + result);
	}

	@Override
	public void before(String serverId,Object target, Method method, Object[] args) {
		System.out.println("=========================");
	}

}

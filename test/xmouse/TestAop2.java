package xmouse;

import java.lang.reflect.Method;

import org.xsnake.remote.server.XSnakeAbstactInterceptor;

public class TestAop2 extends XSnakeAbstactInterceptor{

	@Override
	public void after(String serverId,Object target, Method method, Object[] args,Object result) {
		System.out.println("记录操作" + result);
	}

}

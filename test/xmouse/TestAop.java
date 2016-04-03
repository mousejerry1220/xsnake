package xmouse;

import java.lang.reflect.Method;

import org.xsnake.remote.XSnakeInterceptor;

public class TestAop implements XSnakeInterceptor {

	@Override
	public void after(Object target, Method method, Object[] args,Object result) {
		System.out.println("记录日志" + result);
	}

	@Override
	public void before(Object target, Method method, Object[] args) {
		System.out.println("=========================");
	}

}

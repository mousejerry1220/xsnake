package org.xsnake.rpc.common;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

public class ReflectionUtil extends ReflectionUtils {

	private static Object getCglibProxyTargetObject(Object proxy) {
		try {
			Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
			h.setAccessible(true);
			Object dynamicAdvisedInterceptor = h.get(proxy);
			Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
			advised.setAccessible(true);
			Object target = ((AdvisedSupport) advised
					.get(dynamicAdvisedInterceptor)).getTargetSource()
					.getTarget();
			return target;
		} catch (Exception e) {
			return proxy;
		}
	}

	private static Object getJdkDynamicProxyTargetObject(Object proxy) {
		try {
			Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
			h.setAccessible(true);
			AopProxy aopProxy = (AopProxy) h.get(proxy);
			Field advised = aopProxy.getClass().getDeclaredField("advised");
			advised.setAccessible(true);
			Object target = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
			return target;
		} catch (Exception e) {
			return proxy;
		}
	}

	public static Object getTarget(Object proxy) {
		if (!AopUtils.isAopProxy(proxy)) {
			return proxy;
		}
		if (AopUtils.isJdkDynamicProxy(proxy)) {
			return getJdkDynamicProxyTargetObject(proxy);
		} else {
			return getCglibProxyTargetObject(proxy);
		}
	}

	public static boolean isProxy(Object obj){
		if(Proxy.isProxyClass(obj.getClass())){
			return true;
		}
		if (AopUtils.isAopProxy(obj)) {
			return true;
		}
		if (AopUtils.isJdkDynamicProxy(obj)) {
			return true;
		}
		if(AopUtils.isCglibProxy(obj)){
			return true;
		}
		return false;
	}
	
}

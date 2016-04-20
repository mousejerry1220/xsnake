package org.xsnake.remote.server;

import java.lang.reflect.Method;

/**
 * 服务调用信息
 * @author Jerry.Zhao
 *
 */
public class InvokeInfo {

	Object target; //被调用的服务对象
	
	Method method; //被调用的方法
	
	Object[] args; //被调用时的参数
	
	Object result; //调用执行后返回的结果
	
	long useTime;  //调用服务的执行时长
	
	public InvokeInfo(Object target, Method method, Object[] args, Object result, long useTime) {
		this.target = target;
		this.method = method;
		this.args = args;
		this.result = result;
		this.useTime = useTime;
	}
	
	public Object getTarget() {
		return target;
	}
	public void setTarget(Object target) {
		this.target = target;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public Object[] getArgs() {
		return args;
	}
	public void setArgs(Object[] args) {
		this.args = args;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public long getUseTime() {
		return useTime;
	}
	public void setUseTime(long useTime) {
		this.useTime = useTime;
	}
	
}

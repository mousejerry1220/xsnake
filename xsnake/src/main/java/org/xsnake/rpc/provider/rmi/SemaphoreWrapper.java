package org.xsnake.rpc.provider.rmi;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.Semaphore;

public class SemaphoreWrapper {
	
	Semaphore semaphore;
	
	int maxThread;
	
	//被缓存的调用次数，记录后被重置
	Integer times = 0;
	
	Class<?> interFace;
	
	Method method;
	
	//最大并发调用次数
	int maxCallNum = 0;
	
	//最大调用发生时间
	Date maxCallNumDate = new Date();
	
	public SemaphoreWrapper(Class<?> interFace,Method method, int maxThread){
		this.maxThread = maxThread;
		semaphore = new Semaphore(maxThread);
		this.interFace = interFace;
		this.method = method;
	}

	public Semaphore getSemaphore() {
		return semaphore;
	}

	public void setSemaphore(Semaphore semaphore) {
		this.semaphore = semaphore;
	}

	public int getMaxThread() {
		return maxThread;
	}

	public void setMaxThread(int maxThread) {
		this.maxThread = maxThread;
	}
	
	public void acquire() throws InterruptedException{
		updateTimes();
		semaphore.acquire();
	}
	
	public void release(){
		maxCall();
		semaphore.release();
	}
	
	synchronized public void updateTimes(){
		times = (times + 1);
	}
	
	synchronized public int resetTimes(){
		int _times = times;
		times = 0;
		return _times;
	}
	
	public void availablePermits(){
		semaphore.availablePermits();
	} 
	
	synchronized public int maxCall(){
		int _maxCallNum = maxThread - semaphore.availablePermits();
		if(_maxCallNum > maxCallNum){
			maxCallNum = _maxCallNum;
			maxCallNumDate = new Date();
		}
		return _maxCallNum;
	}

	public Class<?> getInterFace() {
		return interFace;
	}

	public Method getMethod() {
		return method;
	}

	public int getMaxCallNum() {
		return maxCallNum;
	}

	public Date getMaxCallNumDate() {
		return maxCallNumDate;
	}
	
}

package org.xsnake.rpc.provider.rmi.monitor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.KeeperException;
import org.xsnake.rpc.common.MessageHandler;
import org.xsnake.rpc.provider.XSnakeProviderContext;
import org.xsnake.rpc.provider.rmi.RMISupportHandler;
import org.xsnake.rpc.provider.rmi.SemaphoreWrapper;
import org.xsnake.rpc.provider.rmi.XSnakeInterceptorHandler;

public class MethodMonitorUpdator extends Thread{ 
	public void run() {
		while(true){
			List<XSnakeInterceptorHandler> list = context.getRmiSupportHandler().getHandlerList();
			for(XSnakeInterceptorHandler handler : list){
				List<SemaphoreWrapper> methodInfoList = handler.getMethodSemaphoreList();
				for(SemaphoreWrapper methodInfo : methodInfoList){
					try {
						record(methodInfo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			try {
				TimeUnit.SECONDS.sleep(context.getRegistry().getMonitorInterval());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};
	
	XSnakeProviderContext context;
	
	public MethodMonitorUpdator(XSnakeProviderContext context){
		this.context = context;
	}
	
	public void record(SemaphoreWrapper methodInfo) throws KeeperException, InterruptedException, IOException{
		int times = methodInfo.resetTimes();//调用次数，记录后清空重置为0
		Method method = methodInfo.getMethod();
		Class<?> interFace = methodInfo.getInterFace();
		Date now = new Date();
		String methodName = method.getName() + "("+Arrays.toString(method.getParameterTypes())+")";
		final String PATH_ROOT_INVOKEINFO = context.getRmiSupportHandler().PATH_ROOT_INVOKEINFO();
		final String PATH_ROOT_INVOKEINFO_INTERFACE = PATH_ROOT_INVOKEINFO+"/"+interFace.getName(); 
		final String PATH_ROOT_INVOKEINFO_INTERFACE_METHOD = PATH_ROOT_INVOKEINFO_INTERFACE+"/"+methodName;
		RMISupportHandler handler = context.getRmiSupportHandler();
		handler.getZooKeeper().dir(PATH_ROOT_INVOKEINFO);
		handler.getZooKeeper().dir(PATH_ROOT_INVOKEINFO_INTERFACE);
		handler.getZooKeeper().dir(PATH_ROOT_INVOKEINFO_INTERFACE_METHOD);
		
		String date = new SimpleDateFormat("yyyyMMdd").format(now);
		
		final String PATH_ROOT_INVOKEINFO_INTERFACE_METHOD_DATE = PATH_ROOT_INVOKEINFO_INTERFACE_METHOD + "/" + date;
		
		if(handler.getZooKeeper().exists(PATH_ROOT_INVOKEINFO_INTERFACE_METHOD_DATE)){
			
			byte[] methodMonitorInfoData = handler.getZooKeeper().dirData(PATH_ROOT_INVOKEINFO_INTERFACE_METHOD_DATE);
			MethodMonitorInfo methodMonitorInfo = (MethodMonitorInfo) MessageHandler.bytesToObject(methodMonitorInfoData);
			methodMonitorInfo.setMaxCallNum(methodInfo.getMaxCallNum());
			methodMonitorInfo.setMaxCallNumDate(methodInfo.getMaxCallNumDate());
			methodMonitorInfo.setAllTimes(methodMonitorInfo.getAllTimes()+times);
			MonitorInfo monitorInfo = new MonitorInfo();
			monitorInfo.setDatetime(now);
			monitorInfo.setTimes(times);
			methodMonitorInfo.getList().add(monitorInfo);
			handler.getZooKeeper().dir(PATH_ROOT_INVOKEINFO_INTERFACE_METHOD_DATE,MessageHandler.objectToBytes(methodMonitorInfo));
		}else{
			handler.getZooKeeper().dir(PATH_ROOT_INVOKEINFO_INTERFACE_METHOD_DATE,MessageHandler.objectToBytes(new MethodMonitorInfo()));
		}
	}

}

package org.xsnake.rpc.test;


import org.springframework.stereotype.Service;

@Service
public class RemoteTest implements IRemoteTest{
	
	public String sayHello(TestParam test)  {
		return "v99 hello "+ test.getName() + "     :     "+test.getAaa() ;
	}


//	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
//		Method method = IMyService.class.getMethod("todo", String.class);
//		Annotation[][] annotations = method.getParameterAnnotations();
//
//		for(Annotation[] a : annotations){
//			for(Annotation b : a){
//				System.out.println(b);
//			}
//		}
//	}
	
}

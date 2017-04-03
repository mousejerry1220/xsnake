package org.xsnake.rpc.provider;

/**
 * 被扫描出来带有Remote注解的bean对象
 * 
 * @author Jerry.Zhao
 *
 */
public class RemoteServiceBean {

	Object target;
	Class<?> interFace;

	private RemoteServiceBean(Object targetObject, Class<?> interFace) {
		this.target = targetObject;
		this.interFace = interFace;
	}

	public static RemoteServiceBean createServiceBean(Object targetObject, Class<?> interFace) {
		return new RemoteServiceBean(targetObject, interFace);
	}

	public Object getTarget() {
		return target;
	}

	public Class<?> getInterFace() {
		return interFace;
	}

}

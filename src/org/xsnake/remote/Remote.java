package org.xsnake.remote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Remote {

	public static enum Type{
		RMI{
			@Override
			public String toString() {
				return "RMI";
			}
		},WebService{
			@Override
			public String toString() {
				return "WebService";
			}
		}
	}
	
	Class<?> serviceInterface() default Void.class;
	
	int version() default 1; 
	
	Type type() default Type.RMI;
}

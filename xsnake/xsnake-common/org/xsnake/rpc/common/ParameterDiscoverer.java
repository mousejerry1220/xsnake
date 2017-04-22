package org.xsnake.rpc.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.xsnake.rpc.annotation.RequestBody;
import org.xsnake.rpc.annotation.RequestParam;
import org.xsnake.rpc.rest.converter.BooleanConverter;
import org.xsnake.rpc.rest.converter.ConverterRegister;
import org.xsnake.rpc.rest.converter.DateConverter;
import org.xsnake.rpc.rest.converter.DoubleConverter;
import org.xsnake.rpc.rest.converter.FloatConverter;
import org.xsnake.rpc.rest.converter.IConverter;
import org.xsnake.rpc.rest.converter.IntegerConverter;
import org.xsnake.rpc.rest.converter.LongConverter;
import org.xsnake.rpc.rest.converter.ShortConverter;
import org.xsnake.rpc.rest.converter.StringConverter;

public class ParameterDiscoverer {

	public ParameterDiscoverer(){
		initConverterRegister();
	}
	
	public void getParameterNames(Class<?> clazz, Method method, Map<String, String> dataMap, List<Object> args) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException {
		Method interfaceMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
		Annotation[][] annotationss = interfaceMethod.getParameterAnnotations();
		Class<?>[] types = interfaceMethod.getParameterTypes();
		for(int i=0;i<annotationss.length;i++){
			Class<?> type = types[i];
			Annotation[] annotations = annotationss[i];
			if(annotations.length == 0){
				try {
					Object obj = createParamterObject(type, dataMap);
					args.add(obj);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				return;
			}
			
			for(Annotation annotation : annotations){
				if(annotation instanceof RequestParam){
					String name = ((RequestParam)annotation).name();
					String defaultValue=((RequestParam)annotation).defaultValue();
					if(dataMap.get(name) == null && !StringUtils.isEmpty(defaultValue)){
						dataMap.put(name, defaultValue);
					};
					castValue(args, type, name, dataMap);
					break;
				}
				
				if(annotation instanceof RequestBody){
					castValue(args, type, "requestBody", dataMap);
					break;
				}
				
				//普通的java bean
				try {
					Object obj = createParamterObject(type, dataMap);
					args.add(obj);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	ConverterRegister converterRegister = new ConverterRegister();
	
	private void initConverterRegister() {
		converterRegister.register(String.class, new StringConverter());
		converterRegister.register(Date.class, new DateConverter());
		converterRegister.register(int.class, new IntegerConverter());
		converterRegister.register(Integer.class, new IntegerConverter());
		converterRegister.register(float.class, new FloatConverter());
		converterRegister.register(Float.class, new FloatConverter());
		converterRegister.register(Double.class, new DoubleConverter());
		converterRegister.register(double.class, new DoubleConverter());
		converterRegister.register(Long.class, new LongConverter());
		converterRegister.register(long.class, new LongConverter());
		converterRegister.register(Short.class, new ShortConverter());
		converterRegister.register(short.class, new ShortConverter());
		converterRegister.register(Boolean.class, new BooleanConverter());
		converterRegister.register(boolean.class, new BooleanConverter());
	}
	
	private void castValue(List<Object> args, Class<?> type, String name,Map<String, String> paramters) {
		String value = paramters.get(name);
		//查找注册的转换器
		IConverter<?> converter = converterRegister.getConverter(type);
		if(converter != null){
			args.add(converter.converter(value));
			return;
		}
		//没有找到任何
		args.add(null);
	}
	
	private Object createParamterObject(Class<?> type, Map<String, String> paramters) throws InstantiationException, IllegalAccessException {
		Object obj = type.newInstance();
		for(Map.Entry<String, String> entry : paramters.entrySet()){
			String k = entry.getKey();
			String v = entry.getValue();
			try {
				Field field = type.getDeclaredField(k);
				field.setAccessible(true);
				Class<?> clazz = field.getType();
				IConverter<?> converter = converterRegister.getConverter(clazz);
				if(converter !=null){
					Object r =converter.converter(v);
					field.set(obj, r);
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	return obj;
}

}

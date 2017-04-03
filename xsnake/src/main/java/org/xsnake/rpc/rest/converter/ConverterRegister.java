package org.xsnake.rpc.rest.converter;

import java.util.HashMap;
import java.util.Map;

public class ConverterRegister {

	Map<Class<?>,IConverter<?>> map = new HashMap<Class<?>,IConverter<?>>();
	
	public <T> void register(Class<T> clazz,IConverter<T> converter){
		map.put(clazz, converter);
	}
	
	public IConverter<?> getConverter(Class<?> clazz){
		return map.get(clazz);
	}
	
}

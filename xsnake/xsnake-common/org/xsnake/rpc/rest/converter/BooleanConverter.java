package org.xsnake.rpc.rest.converter;

public class BooleanConverter implements IConverter<Boolean>{

	@Override
	public Boolean converter(String str) throws ConverterException {
		try{
			return Boolean.parseBoolean(str);
		}catch(Exception e){
			throw new ConverterException(e.getMessage());
		}
	}
}

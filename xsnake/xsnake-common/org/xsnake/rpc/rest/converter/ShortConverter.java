package org.xsnake.rpc.rest.converter;

public class ShortConverter implements IConverter<Short>{

	@Override
	public Short converter(String str) throws ConverterException {
		try{
			return Short.parseShort(str);
		}catch(Exception e){
			throw new ConverterException(e.getMessage());
		}
	}
}
